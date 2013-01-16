//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle.init;

import pythagoras.f.FloatMath;
import pythagoras.f.Vector;

import static playn.core.PlayN.graphics;

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
        return new VelocityInitializer() {
            protected void initVelocity (Vector vel) {
                vel.set(velocity);
            }
        };
    }

    /**
     * Returns an initializer that provides a uniformly distributed random velocity.
     *
     * @param xRange x velocity will range from -xRange/2 to xRange/2.
     * @param yRange y velocity will range from -yRange/2 to yRange/2.
     */
    public static Initializer randomSquare (Randoms rando, float xRange, float yRange) {
        return randomSquare(rando, -xRange/2, xRange/2, -yRange/2, yRange/2);
    }

    /**
     * Returns an initializer that provides a uniformly distribted random velocity in the range
     * {@code minX} to {@link maxX} and similarly for the y direction.
     */
    public static Initializer randomSquare (final Randoms rando,
                                            final float minX, final float maxX,
                                            final float minY, final float maxY) {
        return new VelocityInitializer() {
            protected void initVelocity (Vector vel) {
                vel.set(rando.getInRange(minX, maxX), rando.getInRange(minY, maxY));
            }
        };
    }

    /**
     * Returns an initializer that provides a normally distributed random velocity with the
     * specified mean and standard deviation parameters.
     */
    public static Initializer randomNormal (Randoms rando, float mean, float dev) {
        return randomNormal(rando, mean, dev, mean, dev);
    }

    /**
     * Returns an initializer that provides a normally distributed random velocity with the
     * specified mean and standard deviation parameters.
     */
    public static Initializer randomNormal (final Randoms rando,
                                            final float xMean, final float xDev,
                                            final float yMean, final float yDev) {
        return new VelocityInitializer() {
            protected void initVelocity (Vector vel) {
                vel.set(rando.getNormal(xMean, xDev), rando.getNormal(yMean, yDev));
            }
        };
    }

    /**
     * Returns an initializer that provides a velocity in a random direction with the specified
     * maximum magnitude.
     */
    public static Initializer randomCircle (Randoms rando, float maximum) {
        return randomCircle(rando, 0, maximum);
    }

    /**
     * Returns an initializer that provides a velocity in a random direction with the specified
     * minimum and maximum magnitude.
     */
    public static Initializer randomCircle (final Randoms rando, final float min, final float max) {
        return new VelocityInitializer() {
            protected void initVelocity (Vector vel) {
                float angle = rando.getFloat(FloatMath.TWO_PI);
                float magnitude = min + rando.getFloat(max-min);
                vel.set(FloatMath.sin(angle)*magnitude, FloatMath.cos(angle)*magnitude);
            }
        };
    }

    /**
     * Returns an initializer that increments the previously assigned velocity by the specified
     * amounts.
     */
    public static Initializer increment (final float dx, final float dy) {
        return new Initializer() {
            @Override public void init (int index, float[] data, int start) {
                float scale = graphics().ctx().scale.factor;
                data[start + ParticleBuffer.VEL_X] += dx * scale;
                data[start + ParticleBuffer.VEL_Y] += dy * scale;
            }
        };
    }

    protected static abstract class VelocityInitializer extends Initializer {
        @Override public void init (int index, float[] data, int start) {
            initVelocity(_vel);
            float scale = graphics().ctx().scale.factor;
            // TODO: account for device orientation
            data[start + ParticleBuffer.VEL_X] = _vel.x * scale;
            data[start + ParticleBuffer.VEL_Y] = _vel.y * scale;
        }
        protected abstract void initVelocity (Vector vel);
        protected final Vector _vel = new Vector();
    }
}
