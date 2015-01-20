//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle.effect;

import tripleplay.particle.Effector;
import tripleplay.particle.ParticleBuffer;

/**
 * Applies uniform gravity to particles.
 */
public class Gravity extends Effector
{
    /** Earth gravity. */
    public static final float EARTH_G = 9.81f;

    /** Creates a gravity effector with earth equivalent gravity. */
    public Gravity () {
        this(EARTH_G);
    }

    /**
     * Creates a gravity effector.
     *
     * @param accel the constant acceleration to apply to the y velocity of a particle. Positive
     * accelerates the particle toward the bottom of the screen.
     */
    public Gravity (float accel) {
        _accel = accel;
    }

    // TODO: account for device orientation in willInit (will need vel vector)

    @Override public void apply (int index, float[] data, int start, float now, float dt) {
        data[start + ParticleBuffer.VEL_Y] += _accel * dt;
    }

    protected final float _accel;
}
