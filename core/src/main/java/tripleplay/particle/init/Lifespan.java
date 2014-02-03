//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle.init;

import tripleplay.util.Randoms;

import tripleplay.particle.Initializer;
import tripleplay.particle.ParticleBuffer;

/**
 * Initializers for a particle's lifespan.
 */
public class Lifespan
{
    /**
     * Returns an initializer that provides a constant lifespan.
     */
    public static Initializer constant (final float lifespan) {
        return new Initializer() {
            @Override public void init (int index, float[] data, int start) {
                data[start+ParticleBuffer.LIFESPAN] = lifespan;
            }
        };
    }

    /**
     * Returns an initializer that provides a random lifespan between {@code min} and {@code max}.
     */
    public static Initializer random (final Randoms rando, final float min, final float max) {
        return new Initializer() {
            @Override public void init (int index, float[] data, int start) {
                data[start+ParticleBuffer.LIFESPAN] = rando.getInRange(min, max);
            }
        };
    }
}
