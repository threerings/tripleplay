//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import playn.core.Json;

/**
 * Facilities for parsing JSON data
 */
public class JsonUtil
{
    /**
     * @return the Enum whose name corresponds to string for the given key
     */
    public static <T extends Enum<T>> T getEnum (Json.Object json, String key, Class<T> enumType)
    {
        return Enum.valueOf(enumType, json.getString(key));
    }

    /**
     * @return a Set<String> containing the keys for the given Json.Object
     * (Json.Object.getKeys() returns a less-useful Json.Array)
     */
    public static Set<String> getKeys (Json.Object json)
    {
        Json.Array keysArray = null;

        // The Java implementation of Json.Object.getKeys() blows up on objects with no keys.
        // TODO: remove this try-catch when the issue is fixed.
        try {
            keysArray = json.getKeys();
        } catch (Exception e) {
            return new HashSet<String>();
        }

        int ll = keysArray.length();
        Set<String> keys = new HashSet<String>(ll);
        for (int ii = 0; ii < ll; ++ii) {
            String key = keysArray.getString(ii);
            if (key == null) {
                throw new RuntimeException("Json.Object.getKeys() returned a null key...?");
            }
            keys.add(key);
        }
        return keys;
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
}
