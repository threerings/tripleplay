//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import react.Function;
import react.RSet;
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
     * @throws NullPointerException if {@code defval} is null.
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
            @Override public void onEmit (String value) {
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
            @Override public void onEmit (Integer value) {
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
            @Override public void onEmit (Long value) {
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
            @Override public void onEmit (Double value) {
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
            @Override public void onEmit (Boolean value) {
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
            @Override public void onEmit (E value) {
                set(key, value);
            }
        });
        return value;
    }

    /**
     * Exposes the specified property as an {@link RSet}. The contents of the set will be encoded
     * as a comma separated string and the supplied {@code toFunc} and {@code fromFunc} will be
     * used to convert an individual set item to and from a string. The to and from functions
     * should perform escaping and unescaping of commas if the encoded representation of the items
     * might naturally contain commas.
     *
     * <p>Any modifications to the set will be immediately persisted back to storage. Note that
     * each call to this method yields a new {@link RSet} and those sets will not coordinate with
     * one another, so the caller must be sure to only call this method once for a given property
     * and share that set properly. Changes to the underlying persistent value that do not take
     * place through the returned set will <em>not</em> be reflected in the set and will be
     * overwritten if the set changes.</p>
     */
    public <E> RSet<E> setFor (final String key, Function<String,E> toFunc,
                               final Function<E,String> fromFunc) {
        final RSet<E> rset = RSet.create();
        String data = get(key, (String)null);
        if (data != null) {
            for (String value : data.split(",")) {
                try {
                    rset.add(toFunc.apply(value));
                } catch (Exception e) {
                    log().warn("Invalid value (key=" + key + "): " + value, e);
                }
            }
        }
        rset.connect(new RSet.Listener<E>() {
            @Override public void onAdd (E unused) { save(); }
            @Override public void onRemove (E unused) { save(); }
            protected void save () {
                if (rset.isEmpty()) remove(key);
                else {
                    StringBuilder buf = new StringBuilder();
                    int ii = 0;
                    for (E value : rset) {
                        if (ii++ > 0) buf.append(",");
                        buf.append(fromFunc.apply(value));
                    }
                    set(key, buf.toString());
                }
            }
        });
        return rset;
    }

    protected final Storage _storage;
}
