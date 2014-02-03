//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.shaders;

/**
 * Shader related utility methods.
 */
public class ShaderUtil
{
    /**
     * Formats a floating point value for inclusion in a shader program. Ensures that the value
     * always contains a '.' and a trailing '0' if needed.
     */
    public static String format (float value) {
        String fmt = String.valueOf(value);
        return fmt.indexOf('.') == -1 ? (fmt + ".0") : fmt;
    }
}
