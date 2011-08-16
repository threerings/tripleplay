//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

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
}
