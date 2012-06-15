//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle.init;

import pythagoras.f.Vector;

import tripleplay.util.Randoms;

import tripleplay.particle.Initializer;
import tripleplay.particle.ParticleBuffer;

/**
 * Initializers for a particle's velocity.
 */
public class Velocity
{
    /**
     * Returns an initializer that provides a constant velocity.
     */
    public static Initializer constant (final Vector velocity) {
        return new Initializer() {
            public void init (int index, float[] data, int start) {
                data[start + ParticleBuffer.VEL_X] = velocity.x;
                data[start + ParticleBuffer.VEL_Y] = velocity.y;
            }
        };
    }

    /**
     * Returns an initializer that provides a random velocity.
     *
     * @param xRange x velocity will range from -xRange/2 to xRange/2.
     * @param yRange y velocity will range from -yRange/2 to yRange/2.
     */
    public static Initializer random (Randoms rando, float xRange, float yRange) {
        return random(rando, -xRange/2, xRange/2, -yRange/2, yRange/2);
    }

    /**
     * Returns an initializer that provides a random velocity in the range {@code minX} to {@link
     * maxX} and similarly for the y direction.
     */
    public static Initializer random (final Randoms rando,
                                      final float minX, final float maxX,
                                      final float minY, final float maxY) {
        return new Initializer() {
            @Override public void init (int index, float[] data, int start) {
                data[start + ParticleBuffer.VEL_X] = rando.getInRange(minX, maxX);
                data[start + ParticleBuffer.VEL_Y] = rando.getInRange(minY, maxY);
            }
        };
    }
}
