//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import react.Slot;
import react.Value;

import playn.core.Storage;
import static playn.core.PlayN.log;

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
     * Returns whether the specified key is mapped to some value.
     */
    public boolean contains (String key) {
        return _storage.getItem(key) != null;
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
            log().warn("Failed to parse int prop [key=" + key + ", value=" + value + "]", e);
            return defval;
        }
    }

    /**
     * Sets the specified property to the supplied int value.
     */
    public void set (String key, int value) {
        _storage.setItem(key, String.valueOf(value));
    }

    /**
     * Returns the specified property as a long. If the property does not exist, the default value
     * will be returned. If the property cannot be parsed as a long, an error will be logged and
     * the default value will be returned.
     */
    public long get (String key, long defval) {
        String value = null;
        try {
            value = _storage.getItem(key);
            return (value == null) ? defval : Long.parseLong(value);
        } catch (Exception e) {
            log().warn("Failed to parse long prop [key=" + key + ", value=" + value + "]", e);
            return defval;
        }
    }

    /**
     * Sets the specified property to the supplied long value.
     */
    public void set (String key, long value) {
        _storage.setItem(key, String.valueOf(value));
    }

    /**
     * Returns the specified property as a double. If the property does not exist, the default
     * value will be returned. If the property cannot be parsed as a double, an error will be
     * logged and the default value will be returned.
     */
    public double get (String key, double defval) {
        String value = null;
        try {
            value = _storage.getItem(key);
            return (value == null) ? defval : Double.parseDouble(value);
        } catch (Exception e) {
            log().warn("Failed to parse double prop [key=" + key + ", value=" + value + "]", e);
            return defval;
        }
    }

    /**
     * Sets the specified property to the supplied double value.
     */
    public void set (String key, double value) {
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
     * Sets the specified property to the supplied boolean value.
     */
    public void set (String key, boolean value) {
        _storage.setItem(key, value ? "t" : "f");
    }

    /**
     * Returns the specified property as an enum. If the property does not exist, the default value
     * will be returned.
     */
    public <E extends Enum<E>> E get (String key, E defval) {
        @SuppressWarnings("unchecked") Class<E> eclass = (Class<E>)defval.getClass();
        String value = null;
        try {
            value = _storage.getItem(key);
            return (value == null) ? defval : Enum.valueOf(eclass, value);
        } catch (Exception e) {
            log().warn("Failed to parse enum prop [key=" + key + ", value=" + value + "]", e);
            return defval;
        }
    }

    /**
     * Sets the specified property to the supplied enum value.
     */
    public void set (String key, Enum<?> value) {
        _storage.setItem(key, value.name());
    }

    /**
     * Removes the specified key (and its value) from storage.
     */
    public void remove (String key) {
        _storage.removeItem(key);
    }

    /**
     * Exposes the specified property as a {@link Value}. The supplied default value will be used
     * if the property has no current value. Updates to the value will be written back to the
     * storage system. Note that each call to this method yields a new {@link Value} and those
     * values will not coordinate with one another, so the caller must be sure to only call this
     * method once for a given property and share that value properly.
     */
    public Value<String> valueFor (final String key, String defval) {
        Value<String> value = Value.create(get(key, defval));
        value.connect(new Slot<String>() {
            public void onEmit (String value) {
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
    public Value<Long> valueFor (final String key, long defval) {
        Value<Long> value = Value.create(get(key, defval));
        value.connect(new Slot<Long>() {
            public void onEmit (Long value) {
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
    public Value<Double> valueFor (final String key, double defval) {
        Value<Double> value = Value.create(get(key, defval));
        value.connect(new Slot<Double>() {
            public void onEmit (Double value) {
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

    /**
     * Exposes the specified property as a {@link Value}. The supplied default value will be used
     * if the property has no current value. Updates to the value will be written back to the
     * storage system. Note that each call to this method yields a new {@link Value} and those
     * values will not coordinate with one another, so the caller must be sure to only call this
     * method once for a given property and share that value properly.
     */
    public <E extends Enum<E>> Value<E> valueFor (final String key, E defval) {
        Value<E> value = Value.create(get(key, defval));
        value.connect(new Slot<E>() {
            public void onEmit (E value) {
                set(key, value);
            }
        });
        return value;
    }

    protected final Storage _storage;
}
