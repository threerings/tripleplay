//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

/**
 * Used to initialize a new particle.
 */
public abstract class Initializer
{
    /**
     * Called just before an initializer is used to initialize one or more particles. Gives the
     * initializer a chance to do any computation that will be shared across all particles
     * initialized on this frame.
     */
    public void willInit (int count) {} // nada by default

    /**
     * Applies this initializer to the {@code index}th particle in the supplied buffer. The
     * initializer must use the {@link ParticleBuffer} offsets to write fields into {@code data},
     * for example: {@code data[start+ParticleBuffer.POS_X] = x}.
     *
     * @param index the index of the particle, which can be used to index into other per-particle
     * data arrays.
     * @param data the particle field data.
     * @param start the offset into {@code data} at which the particle's fields start.
     */
    public abstract void init (int index, float[] data, int start);
}
