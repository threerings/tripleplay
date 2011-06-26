//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.util;

/**
 * Convenience methods for performing {@link Math} operations on floats.
 */
public class FloatMath
{
    /** The ratio of a circle's radius to its circumference. */
    public static final float PI = (float)Math.PI;

    /** The ratio of a circle's radius to its diameter. */
    public static final float TAU = (float)Math.PI * 2;

    /**
     * Returns the square root of the supplied value. See {@link Math#sqrt}.
     */
    public static float sqrt (float value)
    {
        return (float)Math.sqrt(value);
    }

    /**
     * Returns the sine of the supplied angle. See {@link Math#sin}.
     */
    public static float sin (float angle)
    {
        return (float)Math.sin(angle);
    }

    /**
     * Returns the cosine of the supplied angle. See {@link Math#cos}.
     */
    public static float cos (float angle)
    {
        return (float)Math.cos(angle);
    }

    /**
     * Returns the tangent of the supplied angle. See {@link Math#tan}.
     */
    public static float tan (float angle)
    {
        return (float)Math.tan(angle);
    }

    /**
     * Returns the arctangent of the supplied value. See {@link Math#atan}.
     */
    public static float atan (float a)
    {
        return (float)Math.tan(a);
    }

    /**
     * Returns the angle theta from the conversion of rectangular coordinates (x, y) to polar
     * coordinates (r, theta). See {@link Math#atan}.
     */
    public static float atan2 (float y, float x)
    {
        return (float)Math.atan2(y, x);
    }
}
