//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

/**
 * Adds particles to an emitter according to some policy.
 */
public abstract class Generator
{
    /** A generator that emits no particles and is never exhausted. */
    public static Generator NOOP = new Generator() {
        @Override public boolean generate (Emitter emitter, float now, float dt) {
            return false;
        }
    };

    /**
     * Returns a generator that emits the specified number of particles all at once, and is then
     * immediately exhausted.
     */
    public static Generator impulse (final int particles) {
        return new Generator() {
            @Override public boolean generate (Emitter emitter, float now, float dt) {
                emitter.addParticles(particles);
                return true;
            }
        };
    }

    /**
     * Returns a generator that emits the specified number of particles per second, and is never
     * exhausted. The number of particles may be fractional if you wish to emit one particle every
     * N seconds where N is greater than 1.
     */
    public static Generator constant (final float particlesPerSecond) {
        return new Generator() {
            @Override public boolean generate (Emitter emitter, float now, float dt) {
                _accum += dt;
                int particles = (int)(_accum / _secondsPerParticle);
                _accum -= particles * _secondsPerParticle;
                emitter.addParticles(particles);
                return false;
            }
            protected final float _secondsPerParticle = 1 / particlesPerSecond;
            protected float _accum;
        };
    }

    /**
     * Requests that particles be generated, if appropriate.
     *
     * @param emitter the emitter for which the particles should be generated.
     * @param now the current time stamp (in seconds).
     * @param dt the elapsed time since the last frame (in seconds).
     *
     * @return true if this generator is exhausted, false if it has more particles to generate.
     */
    public abstract boolean generate (Emitter emitter, float now, float dt);
}
