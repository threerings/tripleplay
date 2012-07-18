//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.syncdb;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import react.AbstractValue;
import react.RMap;
import react.RSet;
import react.Slot;
import react.Value;

import playn.core.Asserts;
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
    /** The separator used in a map key. This character must not appear in a normal key. */
    public static final String MAP_KEY_SEP = ".";

    /** The separator used in a subdb key. This character must not appear in a normal key. */
    public static final String SUBDB_KEY_SEP = ":";

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
        for (String name : _mods) delta.put(name, _storage.getItem(name));
        return delta;
    }

    /**
     * Notes that we synced cleanly with the server. Updates our local version to the latest sync
     * version and notes that we no longer have unsynced modifications.
     */
    public void noteSync (int version) {
        _mods.clear();
        updateVersion(version);
    }

    /**
     * Returns whether the supplied delta contains changes to any properties for which unsynced
     * changes also exist.
     */
    public boolean containsMerges (Map<String,String> delta) {
        for (String key : delta.keySet()) if (_mods.contains(key)) return true;
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
        for (Map.Entry<String,String> entry : delta.entrySet()) {
            String name = entry.getKey();
            Property prop;
            int pidx = name.indexOf(MAP_KEY_SEP);
            if (pidx == -1) prop = _props.get(name);
            else prop = _props.get(name.substring(0, pidx));
            if (prop == null) {
                log.warning("No local property defined", "name", name);
            } else if (_mods.contains(name)) {
                if (prop.merge(name, entry.getValue())) _mods.remove(name);
            } else {
                prop.update(name, entry.getValue());
                _mods.remove(name); // updating will cause the property to be marked as locally
                                    // changed, but it's not really locally changed, it's been set
                                    // to the latest synced value, so clear the mod flag
            }
        }
        updateVersion(version);
    }

    protected SyncDB (Storage storage) {
        _storage = storage;
        _version = get(SYNC_VERS_KEY, 0, Codec.INT);
        // read the current unsynced key set
        _mods = sget(SYNC_MODS_KEY, Codec.STRING);
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
                T svalue = codec.decode(data), nvalue = resolver.resolve(value.get(), svalue);
                value.update(nvalue);
                return nvalue.equals(svalue);
            }
            public void update (String name, String data) {
                value.update(codec.decode(data));
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
                Set<E> sset = toSet(data, codec);
                resolver.resolve(rset, sset);
                return rset.equals(sset);
            }
            public void update (String name, String data) {
                Set<E> sset = toSet(data, codec);
                rset.retainAll(sset);
                rset.addAll(sset);
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
                String value = _storage.getItem(skey(rawKey));
                return value == null ? null : valCodec.decode(value);
            }

            @Override public V put (K key, V value) {
                _keys.add(key);
                String skey = skey(key);
                String ovalstr = _storage.getItem(skey);
                _storage.setItem(skey, valCodec.encode(value));
                noteModified(skey);
                return ovalstr == null ? null : valCodec.decode(ovalstr);
            }
            @Override public V remove (Object rawKey) {
                String ovalue = _storage.getItem(skey(rawKey));
                _keys.remove(rawKey);
                return (ovalue == null) ? null : valCodec.decode(ovalue);
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
                return prefix + MAP_KEY_SEP + keyCodec.encode(key);
            }

            protected final Set<K> _keys = new HashSet<K>(sget(prefix + "_keys", keyCodec)) {
                @Override public boolean add (K elem) {
                    if (!super.add(elem)) return false;
                    sset(prefix + "_keys", this, keyCodec);
                    return true;
                }
                @Override public boolean remove (Object elem) {
                    if (!super.remove(elem)) return false;
                    @SuppressWarnings("unchecked") K key = (K)elem;
                    removeStorage(key);
                    sset(prefix + "_keys", this, keyCodec);
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
        });
        return map;
    }

    /** Returns the subdb associated with the supplied prefix. The subdb will be created if it has
     * not already been created, and will then be cached for the lifetime of the game. */
    protected SubDB getSubDB (String prefix) {
        SubDB db = _subdbs.get(prefix);
        if (db == null) _subdbs.put(prefix, createSubDB(prefix));
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
        String data = _storage.getItem(name);
        return (data == null) ? defval : codec.decode(data);
    }

    protected <T> void set (String name, T value, Codec<T> codec) {
        _storage.setItem(name, codec.encode(value));
    }

    protected <E> void sset (String name, Set<E> set, Codec<E> codec) {
        StringBuilder buf = new StringBuilder();
        for (E elem : set) {
            if (buf.length() > 0) buf.append("\t");
            buf.append(codec.encode(elem));
        }
        _storage.setItem(name, buf.toString());
    }

    protected <E> Set<E> sget (String name, Codec<E> codec) {
        return toSet(_storage.getItem(name), codec);
    }

    protected <E> Set<E> toSet (String data, Codec<E> codec) {
        Set<E> set = new HashSet<E>();
        if (data != null) {
            for (String edata : data.split("\t")) set.add(codec.decode(edata));
        }
        return set;
    }

    protected void updateVersion (int version) {
        set(SYNC_VERS_KEY, _version = version, Codec.INT);
    }

    protected void noteModified (String name) {
        _mods.add(name);
    }

    /** Manages merges and updates to database properties. */
    protected interface Property {
        boolean merge (String name, String data);
        void update (String name, String data);
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
            return _dbpre + SUBDB_KEY_SEP + name;
        }

        protected String _dbpre;
    }

    protected final Storage _storage;
    protected final Map<String,Property> _props = new HashMap<String,Property>();
    protected final Map<String,SubDB> _subdbs = new HashMap<String,SubDB>();
    protected final Set<String> _mods;
    protected int _version;

    protected static final String SYNC_VERS_KEY = "syncv";
    protected static final String SYNC_MODS_KEY = "syncm";
    protected static final Set<String> SYNC_KEYS = new HashSet<String>(); static {
        SYNC_KEYS.add(SYNC_VERS_KEY);
        SYNC_KEYS.add(SYNC_MODS_KEY);
    }
}
