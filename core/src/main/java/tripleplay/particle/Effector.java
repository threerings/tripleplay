//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

/**
 * Encapsulates a single effect on a particle, for example, moving it based on its current
 * velocity.
 */
public abstract class Effector
{
    /**
     * Applies this effector to the {@code index}th particle in the supplied buffer. The effector
     * must use the {@link ParticleBuffer} offsets to extract fields from {@code data}, for
     * example: {@code float x = data[start+ParticleBuffer.POS_X]}.
     *
     * @param index the index of the particle, which can be used to index into other per-particle
     * data arrays.
     * @param data the particle field data.
     * @param start the offset into {@code data} at which the particle's fields start.
     * @param now the number of seconds elapsed since the emitter came into being. Can be used to
     * compute a particle's age.
     * @param dt the amount of time (in fractions of a second) that has elapsed since the last
     * update.
     */
    public abstract void apply (int index, float[] data, int start, float now, float dt);
}
