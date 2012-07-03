//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.syncdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import react.AbstractValue;
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
 *   public final RMap<String,Integer> items = set("items", Codec.INT, Resolver.INTMAX);
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
            Property prop = _props.get(name);
            if (prop == null) {
                log.warning("No local property defined", "name", name);
            } else if (_mods.contains(name)) {
                if (prop.merge(entry.getValue())) _mods.remove(name);
            } else {
                prop.update(entry.getValue());
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
        _mods = toSet(_storage.getItem(SYNC_MODS_KEY), Codec.STRING);
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
            public boolean merge (String data) {
                T svalue = codec.decode(data), nvalue = resolver.resolve(value.get(), svalue);
                value.update(nvalue);
                return nvalue.equals(svalue);
            }
            public void update (String data) {
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
        final RSet<E> rset = new RSet<E>(toSet(_storage.getItem(name), codec)) {
            @Override protected void emitAdd (E elem) {
                super.emitAdd(elem);
                set(name, _impl, codec);
                noteModified(name);
            }
            @Override protected void emitRemove (E elem) {
                super.emitRemove(elem);
                set(name, _impl, codec);
                noteModified(name);
            }
        };
        _props.put(name, new Property() {
            public boolean merge (String data) {
                Set<E> sset = toSet(data, codec);
                resolver.resolve(rset, sset);
                return rset.equals(sset);
            }
            public void update (String data) {
                Set<E> sset = toSet(data, codec);
                rset.retainAll(sset);
                rset.addAll(sset);
            }
        });
        return rset;
    }

    protected <T> T get (String name, T defval, Codec<T> codec) {
        String data = _storage.getItem(name);
        return (data == null) ? defval : codec.decode(data);
    }

    protected <T> void set (String name, T value, Codec<T> codec) {
        _storage.setItem(name, codec.encode(value));
    }

    protected <E> void set (String name, Set<E> set, Codec<E> codec) {
        StringBuilder buf = new StringBuilder();
        for (E elem : set) {
            if (buf.length() > 0) buf.append("\t");
            buf.append(codec.encode(elem));
        }
        _storage.setItem(name, buf.toString());
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

    protected interface Property {
        boolean merge (String data);
        void update (String data);
    }

    protected final Storage _storage;
    protected final Map<String,Property> _props = new HashMap<String,Property>();
    protected final Set<String> _mods;
    protected int _version;

    protected static final String SYNC_VERS_KEY = "syncv";
    protected static final String SYNC_MODS_KEY = "syncm";
    protected static final Set<String> SYNC_KEYS = new HashSet<String>(); static {
        SYNC_KEYS.add(SYNC_VERS_KEY);
        SYNC_KEYS.add(SYNC_MODS_KEY);
    }
}
