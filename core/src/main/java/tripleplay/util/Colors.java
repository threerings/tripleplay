//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Color;

/**
 * Utilities and constants for colors.
 */
public class Colors
{
    /** Named versions of commonly used colors. */
    public final static int
        WHITE = Color.rgb(255, 255, 255),
        LIGHT_GRAY = Color.rgb(192, 192, 192),
        GRAY = Color.rgb(128, 128, 128),
        DARK_GRAY = Color.rgb(64, 64, 64),
        BLACK = Color.rgb(0, 0, 0),
        RED = Color.rgb(255, 0, 0),
        PINK = Color.rgb(255, 175, 175),
        ORANGE = Color.rgb(255, 200, 0),
        YELLOW = Color.rgb(255, 255, 0),
        GREEN = Color.rgb(0, 255, 0),
        MAGENTA = Color.rgb(255, 0, 255),
        CYAN = Color.rgb(0, 255, 255),
        BLUE = Color.rgb(0, 0, 255);

    /**
     * Blends two colors.
     * @return a color halfway between the two colors.
     */
    public static int blend (int c1, int c2) {
        return Color.rgb((Color.red(c1) + Color.red(c2)) >> 1,
                         (Color.green(c1) + Color.green(c2)) >> 1,
                         (Color.blue(c1) + Color.blue(c2)) >> 1);
    }

    /**
     * Blends two colors proportionally.
     * @param p1 The percentage of the first color to use, from 0.0f to 1.0f inclusive.
     */
    public static int blend (int c1, int c2, float p1) {
        float p2 = 1 - p1;
        return Color.rgb((int)(Color.red(c1) * p1 + Color.red(c2) * p2),
                         (int)(Color.green(c1) * p1 + Color.green(c2) * p2),
                         (int)(Color.blue(c1) * p1 + Color.blue(c2) * p2));
    }

    /**
     * Creates a new darkened version of the given color. This is implemented by composing a new
     * color consisting of the components of the original color, each multiplied by the dark factor.
     * The alpha channel is copied from the original.
     */
    public static int darker (int color, float darkFactor) {
        return Color.argb(Color.alpha(color),
            Math.max((int)(Color.red(color) * darkFactor), 0),
            Math.max((int)(Color.green(color) * darkFactor), 0),
            Math.max((int)(Color.blue(color) * darkFactor), 0));
    }

    /**
     * Creates a new darkened version of the given color with the default DARK_FACTOR.
     */
    public static int darker (int color) {
        return darker(color, DARK_FACTOR);
    }

    /**
     * Creates a new brightened version of the given color. This is implemented by composing a new
     * color consisting of the components of the original color, each multiplied by 10/7, with
     * exceptions for zero-valued components. The alpha channel is copied from the original.
     */
    public static int brighter (int color) {
        int a = Color.alpha(color);
        int r = Color.red(color), g = Color.green(color), b = Color.blue(color);

        // black is a special case the just goes to dark gray
        if (r == 0 && g == 0 && b == 0) return Color.argb(a, MIN_BRIGHT, MIN_BRIGHT, MIN_BRIGHT);

        // bump each component up to the minumum, unless it is absent
        if (r != 0) r = Math.max(MIN_BRIGHT, r);
        if (g != 0) g = Math.max(MIN_BRIGHT, g);
        if (b != 0) b = Math.max(MIN_BRIGHT, b);

        // scale
        return Color.argb(a,
            Math.min((int)(r*BRIGHT_FACTOR), 255),
            Math.min((int)(g*BRIGHT_FACTOR), 255),
            Math.min((int)(b*BRIGHT_FACTOR), 255));
    }

    private static final float DARK_FACTOR = 0.7f;
    private static final float BRIGHT_FACTOR = 1/DARK_FACTOR;
    private static final int MIN_BRIGHT = 3; // (int)(1.0 / (1.0 - DARK_FACTOR));
}
