//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import react.Slot;
import react.Value;

import playn.core.PlayN;
import playn.core.Storage;

/**
 * Makes using PlayN {@link Storage} more civilized. Provides getting and setting of typed values
 * (ints, booleans, etc.). Provides support for default values. Provides {@link Value} interface to
 * storage items.
 */
public class TypedStorage
{
    public TypedStorage (Storage storage) {
        _storage = storage;
    }

    /**
     * Returns the specified property as a string, returning null if the property does not exist.
     */
    public String get (String key) {
        return _storage.getItem(key);
    }

    /**
     * Returns the specified property as a string, returning the supplied defautl value if the
     * property does not exist.
     */
    public String get (String key, String defval) {
        String value = _storage.getItem(key);
        return (value == null) ? defval : value;
    }

    /**
     * Sets the specified property to the supplied string value.
     */
    public void set (String key, String value) {
        _storage.setItem(key, value);
    }

    /**
     * Returns the specified property as an int. If the property does not exist, the default value
     * will be returned. If the property cannot be parsed as an int, an error will be logged and
     * the default value will be returned.
     */
    public int get (String key, int defval) {
        String value = null;
        try {
            value = _storage.getItem(key);
            return (value == null) ? defval : Integer.parseInt(value);
        } catch (Exception e) {
            PlayN.log().warn("Failed to parse int prop [key=" + key + ", value=" + value + "]", e);
            return defval;
        }
    }

    /**
     * Removes the specified key (and its value) from storage.
     */
    public void remove (String key) {
        _storage.removeItem(key);
    }

    /**
     * Sets the specified property to the supplied int value.
     */
    public void set (String key, int value) {
        _storage.setItem(key, String.valueOf(value));
    }

    /**
     * Returns the specified property as a boolean. If the property does not exist, the default
     * value will be returned. Any existing value equal to {@code t} (ignoring case) will be
     * considered true; all others, false.
     */
    public boolean get (String key, boolean defval) {
        String value = _storage.getItem(key);
        return (value == null) ? defval : value.equalsIgnoreCase("t");
    }

    /**
     * Sets the specified property to the supplied int value.
     */
    public void set (String key, boolean value) {
        _storage.setItem(key, value ? "t" : "f");
    }

    /**
     * Exposes the specified property as a {@link Value}. The supplied default value will be used
     * if the property has no current value. Updates to the value will be written back to the
     * storage system. Note that each call to this method yields a new {@link Value} and those
     * values will not coordinate with one another, so the caller must be sure to only call this
     * method once for a given property and share that value properly.
     */
    public Value<Integer> valueFor (final String key, int defval) {
        Value<Integer> value = Value.create(get(key, defval));
        value.connect(new Slot<Integer>() {
            public void onEmit (Integer value) {
                set(key, value);
            }
        });
        return value;
    }

    /**
     * Exposes the specified property as a {@link Value}. The supplied default value will be used
     * if the property has no current value. Updates to the value will be written back to the
     * storage system. Note that each call to this method yields a new {@link Value} and those
     * values will not coordinate with one another, so the caller must be sure to only call this
     * method once for a given property and share that value properly.
     */
    public Value<Boolean> valueFor (final String key, boolean defval) {
        Value<Boolean> value = Value.create(get(key, defval));
        value.connect(new Slot<Boolean>() {
            public void onEmit (Boolean value) {
                set(key, value);
            }
        });
        return value;
    }

    protected final Storage _storage;
}
