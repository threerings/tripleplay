//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

/**
 * Utilities that apply to all objects. This will mostly reflect ideas originally consolidated
 * in com.google.common.base, but not roping in the whole of guava.
 */
public class Objects
{
    /**
     * Tests if two objects match according to reference equality, or {@link Object#equals(Object)}
     * if both are non-null.
     */
    public static boolean equal (Object o1, Object o2) {
        return o1 == o2 || (o1 != null && o1.equals(o2));
    }
}
