//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.syncdb;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import react.RMap;
import react.RSet;
import react.Value;

import playn.core.Asserts;
import playn.core.Platform;
import playn.core.Storage;

import static tripleplay.syncdb.Log.log;

/**
 * A database of key/value pairs that is synced (via a server) across multiple devices. The
 * suggested usage pattern is to subclass {@code SyncDB} and define your properties like so:
 * <pre>{@code
 * public class FooDB extends SyncDB {
 *   public final Value<String> name = value("name", (String)null, Codec.STRING, Resolver.SERVER);
 *   public final Value<Integer> xp = value("xp", 0, Codec.INT, Resolver.INTMAX);
 *   public final Value<Integer> difficultyLevel = value("diff", 0, Codec.INT, Resolver.SERVER);
 *   public final RSet<String> badges = set("badges", Codec.STRING, SetResolver.UNION);
 *   public final RMap<String,Integer> items = map("it", Codec.STRING, Codec.INT, Resolver.INTMAX);
 *   // etc.
 * }
 * }</pre>
 */
public abstract class SyncDB
{
    /**
     * Returns the version at which this database was last synced.
     */
    public int version () {
        return _version;
    }

    /**
     * Returns true if this database contains changes that have not been synced.
     */
    public boolean hasUnsyncedChanges () {
        return !_mods.isEmpty();
    }

    /**
     * Returns a map of properties that have changed since our last sync. These can be sent to the
     * server (along with our current version) to sync our state with the server.
     */
    public Map<String,String> getDelta () {
        Map<String,String> delta = new HashMap<String,String>();
        for (String name : _mods.keySet()) delta.put(name, _storage.getItem(name));
        return delta;
    }

    /**
     * Returns a snapshot of our current mapping of modified properties to modification count. This
     * must be taken at the same time as a call to {@link #getDelta}, and then provided to the call
     * to {@link #noteSync} if the delta was successfully used in a sync.
     */
    public Map<String,Integer> getMods () {
        return new HashMap<String,Integer>(_mods);
    }

    /**
     * Notes that we synced cleanly with the server. Updates our local version to the latest sync
     * version and notes that we no longer have unsynced modifications.
     */
    public void noteSync (int version, Map<String,Integer> syncedMods) {
        // clear out the synced properties from our set of currently modified properties
        for (Map.Entry<String,Integer> entry : syncedMods.entrySet()) {
            String prop = entry.getKey();
            Integer mcount = _mods.get(prop);
            if (mcount == null) {
                log.warning("Have no mod count for synced property?", "prop", prop);
                continue;
            }

            int syncedMC = entry.getValue().intValue(), curMC = mcount.intValue();
            if (syncedMC > curMC) {
                log.warning("Synced mod count is greater than current?", "prop", prop, "curMC", curMC,
                            "syncedMC", syncedMC);
                // leave it as modified and we'll sync again just in case
            } else if (syncedMC == curMC) {
                _mods.remove(prop);
            }
            // otherwise the curMC is greater than syncedMC, meaning the property was modified
            // while this sync was taking place
        }
        flushMods();
        updateVersion(version);
    }

    /**
     * Returns whether the supplied delta contains changes to any properties for which unsynced
     * changes also exist.
     */
    public boolean containsMerges (Map<String,String> delta) {
        for (String key : delta.keySet()) if (_mods.containsKey(key)) return true;
        return false;
    }

    /**
     * Applies the supplied changes to this database. Any conflicts will be resolved according to
     * the configured policies. If the resolved value differs from the supplied value, the property
     * will remain marked as locally modified. Thus changes may need to be flushed again after
     * calling this method. After applying the delta and resolving conflicts, the local version
     * will be updated to the supplied version.
     *
     * @param version the latest version.
     * @param delta the modifications from our local version to the latest version.
     */
    public void applyDelta (int version, Map<String,String> delta) {
        // resolve all of the subdbs that are needed to apply these properties
        Set<String> subDBs = new HashSet<String>();
        for (String key : delta.keySet()) {
            String sdb = DBUtil.subDB(key);
            if (sdb != null) subDBs.add(sdb);
        }
        if (!subDBs.isEmpty()) for (String subdb : subDBs) getSubDB(subdb);

        // now apply the delta to the appropriate properties
        for (Map.Entry<String,String> entry : delta.entrySet()) {
            String name = entry.getKey(), value = entry.getValue();
            Property prop;
            int pidx = name.indexOf(DBUtil.MAP_KEY_SEP);
            if (pidx == -1) prop = _props.get(name);
            else prop = _props.get(name.substring(0, pidx));
            if (prop == null) {
                log.warning("No local property defined", "name", name);
            } else if (_mods.containsKey(name)) {
                try {
                    if (prop.merge(name, value)) _mods.remove(name);
                } catch (Exception e) {
                    log.warning("Property merge fail", "name", name, "value", value, e);
                }
            } else {
                try {
                    prop.update(name, value);
                } catch (Exception e) {
                    log.warning("Property update fail", "name", name, "value", value, e);
                }
                _mods.remove(name); // updating will cause the property to be marked as locally
                                    // changed, but it's not really locally changed, it's been set
                                    // to the latest synced value, so clear the mod flag
            }
        }

        flushMods();
        updateVersion(version);
    }

    /**
     * Prepares this database to be melded into a pre-existing database. Resets this database's
     * version to zero and marks all properties as modified. The database can then be synced with
     * another database which will merge the other database into this one and then push remaining
     * changes back up to the server. This is used to merge the database between two clients.
     */
    public void prepareToMeld () {
        updateVersion(0);
        for (Property prop : _props.values()) prop.prepareToMeld();
    }

    /**
     * Processes any subdbs that have been queued for purging. This requires a pass over all keys
     * in the storage system, and thus benefits from aggregating purges prior to performing them.
     */
    public void processPurges () {
        purgeDBs(sget(SYNC_PURGE_KEY, Codec.STRING));
        _storage.removeItem(SYNC_PURGE_KEY);
    }

    protected SyncDB (Platform platform) {
        _platform = platform;
        _storage = platform.storage();
        _version = get(SYNC_VERS_KEY, 0, Codec.INT);
        // read the current unsynced key set
        for (String mod : sget(SYNC_MODS_KEY, Codec.STRING)) _mods.put(mod, 1);
    }

    /**
     * Creates a synced value with the specified configuration.
     *
     * @param name the name to use for the persistent property, must not conflict with any other
     * value, set or map name.
     * @param defval the default value to use until a value has been configured.
     * @param codec the codec to use when converting the value to and from a string for storage.
     * @param resolver the conflict resolution policy to use when local modifications conflict with
     * server modifications.
     */
    protected <T> Value<T> value (final String name, final T defval, final Codec<T> codec,
                                  final Resolver<? super T> resolver) {
        Asserts.checkArgument(!SYNC_KEYS.contains(name), name + " is a reserved name.");
        // create a value that reads/writes directly from/to the persistent store
        final Value<T> value = new Value<T>(null) {
            @Override public T get () {
                return SyncDB.this.get(name, defval, codec);
            }
            @Override protected T updateLocal (T value) {
                T oldValue = get();
                SyncDB.this.set(name, value, codec);
                return oldValue;
            }
            @Override protected void emitChange (T value, T ovalue) {
                super.emitChange(value, ovalue);
                noteModified(name);
            }
        };
        _props.put(name, new Property() {
            public boolean merge (String name, String data) {
                T svalue = codec.decode(data, defval);
                T nvalue = resolver.resolve(value.get(), svalue);
                value.update(nvalue);
                return nvalue.equals(svalue);
            }
            public void update (String name, String data) {
                value.update(codec.decode(data, defval));
            }
            public void prepareToMeld () {
                noteModified(name);
            }
        });
        return value;
    }

    /**
     * Creates a synced set with the specified configuration.
     *
     * @param name the name to use for the persistent property, must not conflict with any other
     * value, set or map name.
     * @param codec the codec to use when converting an element to and from a string for storage.
     * @param resolver the conflict resolution policy to use when local modifications conflict with
     * server modifications.
     */
    protected <E> RSet<E> set (final String name, final Codec<E> codec, final SetResolver resolver) {
        Asserts.checkArgument(!SYNC_KEYS.contains(name), name + " is a reserved name.");
        final RSet<E> rset = new RSet<E>(sget(name, codec)) {
            @Override protected void emitAdd (E elem) {
                super.emitAdd(elem);
                sset(name, _impl, codec);
                noteModified(name);
            }
            @Override protected void emitRemove (E elem) {
                super.emitRemove(elem);
                sset(name, _impl, codec);
                noteModified(name);
            }
        };
        _props.put(name, new Property() {
            public boolean merge (String name, String data) {
                Set<E> sset = (data == null) ? Collections.<E>emptySet() :
                    DBUtil.decodeSet(data, codec);
                resolver.resolve(rset, sset);
                return rset.equals(sset);
            }
            public void update (String name, String data) {
                if (data == null) rset.clear();
                else {
                    Set<E> sset = DBUtil.decodeSet(data, codec);
                    rset.retainAll(sset);
                    rset.addAll(sset);
                }
            }
            public void prepareToMeld () {
                noteModified(name);
            }
        });
        return rset;
    }

    /**
     * Creates a synced map with the specified configuration. Note that deletions take precedence
     * over additions or modifications. If any client deletes a key, the deletion will be
     * propagated to all clients, regardless of whether another client updates or reinstates the
     * mapping in the meanwhile. Thus, you must avoid structuring your storage such that keys are
     * deleted and later re-added. Instead use a "deleted" sentinal value for keys that may once
     * again map to valid values.
     *
     * <em>Note:</em> the returned map <em>will not</em> tolerate ill-typed keys. Passing a key
     * that is not an instance of {@code K} to any method that accepts a key will result in a
     * ClassCastException. Also {@link Map#containsValue} is not supported by the returned map.
     *
     * @param prefix a string prefix prepended to the keys to create the storage key for each
     * individual map entry. A '.' will be placed in between the prefix and the string value of the
     * map key. For example: a prefix of {@code foo} and a map key of {@code 1} will result in a
     * storage key of {@code foo.1}. Additionally, the {@code prefix_keys} storage cell will be used
     * to track the current set of keys in the map.
     * @param keyCodec the codec to use when converting a key to/from string.
     * @param valCodec the codec to use when converting a value to/from string for storage.
     * @param resolver the conflict resolution policy to use when conflicting changes have been
     * made to a single mapping.
     */
    protected <K,V> RMap<K,V> map (final String prefix, final Codec<K> keyCodec,
                                   final Codec<V> valCodec, final Resolver<? super V> resolver) {
        class StorageMap extends AbstractMap<K,V> {
            @Override public int size () {
                return _keys.size();
            }

            @Override public boolean containsKey (Object key) {
                return _keys.contains(key);
            }
            @Override public V get (Object rawKey) {
                return valCodec.decode(_storage.getItem(skey(rawKey)), null);
            }

            @Override public V put (K key, V value) {
                _keys.add(key);
                String skey = skey(key);
                String valstr = valCodec.encode(value);
                String ovalstr = _storage.getItem(skey);
                _storage.setItem(skey, valstr);
                if (!valstr.equals(ovalstr)) noteModified(skey);
                return valCodec.decode(ovalstr, null);
            }
            @Override public V remove (Object rawKey) {
                String ovalue = _storage.getItem(skey(rawKey));
                _keys.remove(rawKey); // this triggers a noteModified
                return valCodec.decode(ovalue, null);
            }

            @Override public Set<K> keySet () {
                return Collections.unmodifiableSet(_keys);
            }
            @Override public Set<Map.Entry<K,V>> entrySet () {
                return new AbstractSet<Map.Entry<K,V>>() {
                    @Override public Iterator<Map.Entry<K,V>> iterator () {
                        return new Iterator<Map.Entry<K,V>>() {
                            public boolean hasNext () {
                                return _keysIter.hasNext();
                            }
                            public Map.Entry<K,V> next () {
                                final K key = _keysIter.next();
                                return new Map.Entry<K,V>() {
                                    @Override public K getKey () { return key; }
                                    @Override public V getValue () {
                                        return StorageMap.this.get(key);
                                    }
                                    @Override public V setValue (V value) {
                                        return StorageMap.this.put(key, value);
                                    }
                                };
                            }
                            public void remove () {
                                _keysIter.remove();
                            }
                            protected Iterator<K> _keysIter = _keys.iterator();
                        };
                    }
                    @Override public int size () {
                        return _keys.size();
                    }
                };
            }

            protected String skey (Object rawKey) {
                @SuppressWarnings("unchecked") K key = (K)rawKey;
                return DBUtil.mapKey(prefix, key, keyCodec);
            }

            protected final Set<K> _keys = new HashSet<K>(sget(mapKeysKey(prefix), keyCodec)) {
                @Override public boolean add (K elem) {
                    // our super constructor will call add() with keys we loaded and passed to it;
                    // we don't want the addition of those keys to trigger a restore of the keyset
                    if (!_superctordone) return super.add(elem);
                    if (!super.add(elem)) return false;
                    sset(mapKeysKey(prefix), this, keyCodec);
                    return true;
                }
                @Override public boolean remove (Object elem) {
                    if (!super.remove(elem)) return false;
                    @SuppressWarnings("unchecked") K key = (K)elem;
                    removeStorage(key);
                    sset(mapKeysKey(prefix), this, keyCodec);
                    return true;
                }
                @Override public Iterator<K> iterator () {
                    final Iterator<K> iter = super.iterator();
                    return new Iterator<K>() {
                        @Override public boolean hasNext () { return iter.hasNext(); }
                        @Override public K next () { return _current = iter.next(); }
                        @Override public void remove () {
                            iter.remove();
                            removeStorage(_current);
                        }
                        protected K _current;
                    };
                }
                protected void removeStorage (K key) {
                    String skey = skey(key);
                    _storage.removeItem(skey);
                    noteModified(skey);
                }
                // we're taking advantage of object initialization order here; before our super
                // constructor is finished running, this field will be false; then it will be
                // initialized to true during our (synthetic) constructor; we use this value in
                // add() to distinguish between the add() calls made by our super constructor and
                // all subsequent add calls made once this map is live
                protected boolean _superctordone = true;
            };
        }

        final RMap<K,V> map = new RMap<K,V>(new StorageMap());
        _props.put(prefix, new Property() {
            public boolean merge (String name, String data) {
                K skey = keyCodec.decode(name.substring(prefix.length()+1));
                if (data == null) {
                    map.remove(skey);
                    return true;
                }
                V svalue = valCodec.decode(data), nvalue = resolver.resolve(map.get(skey), svalue);
                map.put(skey, nvalue);
                return nvalue.equals(svalue);
            }
            public void update (String name, String data) {
                K skey = keyCodec.decode(name.substring(prefix.length()+1));
                if (data == null) map.remove(skey);
                else map.put(skey, valCodec.decode(data));
            }
            public void prepareToMeld () {
                for (K key : map.keySet()) noteModified(DBUtil.mapKey(prefix, key, keyCodec));
            }
        });
        // register a property for our keys set which is a NOOP, as updates to our values will
        // automatically keep our keys in sync
        _props.put(mapKeysKey(prefix), new Property() {
            public boolean merge (String name, String data) { return true; } // noop
            public void update (String name, String data) {} // noop
            public void prepareToMeld () {} // noop
        });
        return map;
    }

    /** Returns the subdb associated with the supplied prefix. The subdb will be created if it has
     * not already been created, and will then be cached for the lifetime of the game. */
    protected SubDB getSubDB (String prefix) {
        SubDB db = _subdbs.get(prefix);
        if (db == null) _subdbs.put(prefix, db = createSubDB(prefix));
        return db;
    }

    /** Called to create the subdb for the supplied subdb prefix. This happens on demand when
     * properties are seen that belong to a subdb. This allows subdbs to be created on-demand, only
     * when their properties are needed to apply updates or resolve conflicts. A game can of course
     * also trigger the resolution of a subdb by calling {@link #getSubDB}. */
    protected SubDB createSubDB (String prefix) {
        throw new IllegalArgumentException("Unknown subdb prefix: " + prefix);
    }

    protected <T> T get (String name, T defval, Codec<T> codec) {
        return codec.decode(_storage.getItem(name), defval);
    }

    protected <T> void set (String name, T value, Codec<T> codec) {
        _storage.setItem(name, codec.encode(value));
    }

    protected <E> void sset (String name, Set<E> set, Codec<E> codec) {
        _storage.setItem(name, DBUtil.encodeSet(set, codec));
    }

    protected <E> Set<E> sget (String name, Codec<E> codec) {
        return DBUtil.decodeSet(_storage.getItem(name), codec);
    }

    protected void updateVersion (int version) {
        set(SYNC_VERS_KEY, _version = version, Codec.INT);
    }

    protected void noteModified (String name) {
        Integer omods = _mods.get(name);
        _mods.put(name, (omods == null) ? 1 : omods+1);
        if (omods == null) queueFlushMods();
    }

    protected void flushMods () {
        sset(SYNC_MODS_KEY, _mods.keySet(), Codec.STRING);
    }

    protected void queueFlushMods () {
        if (_flushQueued) return;
        _flushQueued = true;
        _platform.invokeLater(new Runnable() { public void run () {
            flushMods();
            _flushQueued = false;
        }});
    }

    protected void purgeDBs (Set<String> dbs) {
        if (dbs.isEmpty()) return; // NOOP!
        log.info("Purging", "dbs", dbs);

        for (String key : _storage.keys()) {
            int sdbidx = key.indexOf(DBUtil.SUBDB_KEY_SEP);
            if (sdbidx == -1 || !dbs.contains(key.substring(0, sdbidx))) continue;
            // log.info("Purging property " + key);
            _storage.removeItem(key);
            _mods.remove(key);
        }
        flushMods();
    }

    /** Manages merges and updates to database properties. */
    protected interface Property {
        boolean merge (String name, String data);
        void update (String name, String data);
        void prepareToMeld ();
    }

    /** Returns the key used to store the keys for a map with the specified prefix. */
    protected static String mapKeysKey (String mapPrefix) {
        return mapPrefix + "_keys";
    }

    /** Used to encapsulate a collection of properties associated with a particular prefix. For
     * example a chess game could create a subdb for each active game using some generated game id
     * as a prefix, and when the game was complete, the entire subdb could be removed.
     * Additionally, conflict resolution could be adjusted for all elements in a subdb (e.g. using
     * the server's values for an entire subdb, or the client's). */
    protected abstract class SubDB {
        protected SubDB (String prefix) {
            _dbpre = prefix;
        }

        protected <T> Value<T> value (String name, T defval, Codec<T> codec,
                                      Resolver<? super T> resolver) {
            return SyncDB.this.value(key(name), defval, codec, resolver);
        }

        protected <E> RSet<E> set (String name, Codec<E> codec, SetResolver resolver) {
            return SyncDB.this.set(key(name), codec, resolver);
        }

        protected <K,V> RMap<K,V> map (String prefix, Codec<K> keyCodec, Codec<V> valCodec,
                                       Resolver<? super V> resolver) {
            return SyncDB.this.map(key(prefix), keyCodec, valCodec, resolver);
        }

        protected String key (String name) {
            return DBUtil.subDBKey(_dbpre, name);
        }

        /** Removes all of this subdb's properties from the client's persistent storage. Removes
         * any pending sync requests for properties of this subdb. Clears the subdb object. */
        protected void purge () {
            purgeDBs(Collections.singleton(_dbpre));
            _subdbs.remove(_dbpre);
        }

        /** Notes that this subdb should be purged in the next call to {@link #processPurges}.
         * NOTE: this subdb will immediately be cleared from the subdbs table; it should not be
         * accessed again after calling this method. */
        protected void queuePurge () {
            Set<String> pendingPurges = sget(SYNC_PURGE_KEY, Codec.STRING);
            pendingPurges.add(_dbpre);
            sset(SYNC_PURGE_KEY, pendingPurges, Codec.STRING);
            log.info("Queued purge", "subdb", _dbpre, "penders", pendingPurges);
            _subdbs.remove(_dbpre);
        }

        /** Cancels a queued purge for this subdb. */
        protected void cancelQueuedPurge () {
            Set<String> pendingPurges = sget(SYNC_PURGE_KEY, Codec.STRING);
            if (pendingPurges.remove(_dbpre)) {
                sset(SYNC_PURGE_KEY, pendingPurges, Codec.STRING);
                log.info("Canceled queued purge", "subdb", _dbpre, "penders", pendingPurges);
            }
        }

        protected String _dbpre;
    }

    protected final Platform _platform;
    protected final Storage _storage;

    protected final Map<String,Property> _props = new HashMap<String,Property>();
    protected final Map<String,SubDB> _subdbs = new HashMap<String,SubDB>();
    protected final Map<String,Integer> _mods = new HashMap<String,Integer>();
    protected int _version;
    protected boolean _flushQueued;

    protected static final String SYNC_VERS_KEY  = "syncv";
    protected static final String SYNC_MODS_KEY  = "syncm";
    protected static final String SYNC_PURGE_KEY = "syncp";
    protected static final Set<String> SYNC_KEYS = new HashSet<String>(); static {
        SYNC_KEYS.add(SYNC_VERS_KEY);
        SYNC_KEYS.add(SYNC_MODS_KEY);
    }
}
