//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.Iterator;

import playn.core.Json;

/**
 * Facilities for parsing JSON data
 */
public class JsonUtil
{
    /**
     * @return the Enum whose name corresponds to string for the given key,
     * or <code>defaultVal</code> if the key doesn't exist.
     */
    public static <T extends Enum<T>> T getEnum (Json.Object json, String key, Class<T> enumType,
        T defaultVal)
    {
        return Enum.valueOf(enumType, getString(json, key, defaultVal.toString()));
    }

    /**
     * @return the Enum whose name corresponds to string for the given key.
     * Throws a RuntimeException if the key doesn't exist.
     */
    public static <T extends Enum<T>> T requireEnum (Json.Object json, String key,
        Class<T> enumType)
    {
        return Enum.valueOf(enumType, requireString(json, key));
    }

    /**
     * @return the boolean value at the given key, or <code>defaultVal</code> if the key
     * doesn't exist.
     */
    public static boolean getBoolean (Json.Object json, String key, boolean defaultVal)
    {
        return (json.containsKey(key) ? json.getBoolean(key) : defaultVal);
    }

    /**
     * @return the boolean value at the given key.
     * @throws a RuntimeException if the key doesn't exist.
     */
    public static boolean requireBoolean (Json.Object json, String key)
    {
        requireKey(json, key);
        return json.getBoolean(key);
    }

    /**
     * @return the double value at the given key, or <code>defaultVal</code> if the key
     * doesn't exist.
     */
    public static double getNumber (Json.Object json, String key, double defaultVal)
    {
        return (json.containsKey(key) ? json.getNumber(key) : defaultVal);
    }

    /**
     * @return the double value at the given key.
     * @throws a RuntimeException if the key doesn't exist.
     */
    public static double requireNumber (Json.Object json, String key)
    {
        requireKey(json, key);
        return json.getNumber(key);
    }

    /**
     * @return the float value at the given key, or <code>defaultVal</code> if the key
     * doesn't exist.
     */
    public static float getFloat (Json.Object json, String key, float defaultVal)
    {
        return (float) getNumber(json, key, defaultVal);
    }

    /**
     * @return the float value at the given key.
     * @throws a RuntimeException if the key doesn't exist.
     */
    public static float requireFloat (Json.Object json, String key)
    {
        return (float) requireNumber(json, key);
    }

    /**
     * @return the int value at the given key, or <code>defaultVal</code> if the key
     * doesn't exist.
     */
    public static int getInt (Json.Object json, String key, int defaultVal)
    {
        return (json.containsKey(key) ? json.getInt(key) : defaultVal);
    }

    /**
     * @return the int value at the given key.
     * @throws a RuntimeException if the key doesn't exist.
     */
    public static int requireInt (Json.Object json, String key)
    {
        requireKey(json, key);
        return json.getInt(key);
    }

    /**
     * @return the String value at the given key, or <code>defaultVal</code> if the key
     * doesn't exist.
     */
    public static String getString (Json.Object json, String key, String defaultVal)
    {
        return (json.containsKey(key) ? json.getString(key) : defaultVal);
    }

    /**
     * @return the String value at the given key.
     * @throws a RuntimeException if the key doesn't exist.
     */
    public static String requireString (Json.Object json, String key)
    {
        requireKey(json, key);
        return json.getString(key);
    }

    /**
     * @return the Json.Object value at the given key, or <code>defaultVal</code> if the key
     * doesn't exist.
     */
    public static Json.Object getObject (Json.Object json, String key, Json.Object defaultVal)
    {
        return (json.containsKey(key) ? json.getObject(key) : defaultVal);
    }

    /**
     * @return the Json.Object at the given key.
     * @throws a RuntimeException if the key doesn't exist.
     */
    public static Json.Object requireObject (Json.Object json, String key)
    {
        requireKey(json, key);
        return json.getObject(key);
    }

    /**
     * @return the Json.Object value at the given key, or <code>defaultVal</code> if the key
     * doesn't exist.
     */
    public static Json.Array getArray (Json.Object json, String key, Json.Array defaultVal)
    {
        return (json.containsKey(key) ? json.getArray(key) : defaultVal);
    }

    /**
     * @return the Json.Array at the given key.
     * @throws a RuntimeException if the key doesn't exist.
     */
    public static Json.Array requireArray (Json.Object json, String key)
    {
        requireKey(json, key);
        return json.getArray(key);
    }

    /**
     * @return a Iterable<String> containing the keys for the given Json.Object
     * (Json.Object.getKeys() returns a less-useful Json.Array)
     */
    public static Iterable<String> getKeys (Json.Object json)
    {
        final Json.Array array = json.getKeys();
        return new Iterable<String>() {
            @Override public Iterator<String> iterator () {
                return new Iterator<String>() {
                    @Override public boolean hasNext () {
                        return (array != null && _index < array.length());
                    }
                    @Override public String next () {
                        String str = array.getString(_index);
                        if (str == null) {
                            throw new RuntimeException(
                                "Json.Object.getKeys() returned a null key...?");
                        }
                        ++_index;
                        return str;
                    }
                    @Override public void remove () {
                        throw new UnsupportedOperationException();
                    }

                    protected int _index;
                };
            }
        };
    }

    /**
     * @return an Iterable<Json.Object> that iterates the Objects in the Array at the given key,
     * or null if there's no Array at that key.
     *
     * The Iterable will throw an error, during iteration, if any of the items in the Array are
     * not Json.Objects.
     */
    public static Iterable<Json.Object> getArrayObjects (Json.Object json, String key)
    {
        final Json.Array array = json.getArray(key);
        if (array == null) {
            return null;

        } else {
            return new Iterable<Json.Object>() {
                @Override public Iterator<Json.Object> iterator () {
                    return new Iterator<Json.Object>() {
                        @Override public boolean hasNext () {
                            return _index < array.length();
                        }
                        @Override public Json.Object next () {
                            Json.Object obj = array.getObject(_index);
                            if (obj == null) {
                                throw new RuntimeException("There's no Json.Object at the given " +
                                        "index [index=" + _index + "]");
                            }
                            ++_index;
                            return obj;
                        }
                        @Override public void remove () {
                            throw new UnsupportedOperationException();
                        }

                        protected int _index;
                    };
                }
            };
        }
    }

    /**
     * @return an Iterable<Json.Object> that iterates the Objects in the Array at the given key.
     * Throws a RuntimeException if there's no Array at that key.
     *
     * The Iterable will throw an error, during iteration, if any of the items in the Array are
     * not Json.Objects.
     */
    public static Iterable<Json.Object> requireArrayObjects (Json.Object json, String key)
    {
        Iterable<Json.Object> iter = getArrayObjects(json, key);
        if (iter == null) {
            throw new RuntimeException("No Array for the given key [name=" + key + "]");
        }
        return iter;
    }

    protected static void requireKey (Json.Object json, String key)
    {
        if (!json.containsKey(key)) {
            throw new RuntimeException("Missing required key [name=" + key + "]");
        }
    }
}
