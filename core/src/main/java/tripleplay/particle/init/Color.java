//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle.init;

import tripleplay.particle.Initializer;
import tripleplay.particle.ParticleBuffer;

/**
 * Initializes the color value of a particle.
 */
public class Color
{
    /**
     * Returns an initializer that initializes the particle's color to the supplied constant value.
     */
    public static Initializer constant (int argb) {
        return constant(((argb >> 16) & 0xFF) / 255f,
                        ((argb >>  8) & 0xFF) / 255f,
                        ((argb >>  0) & 0xFF) / 255f,
                        ((argb >> 24) & 0xFF) / 255f);
    }

    /**
     * Returns an initializer that initializes the particle's color to the supplied constant value.
     */
    public static Initializer constant (final float r, final float g, final float b, final float a) {
        return new Initializer() {
            @Override public void init (int index, float[] data, int start) {
                data[start+ParticleBuffer.ALPHA_RED]  = playn.core.Color.encode(a, r);
                data[start+ParticleBuffer.GREEN_BLUE] = playn.core.Color.encode(g, b);
            }
        };
    }
}
