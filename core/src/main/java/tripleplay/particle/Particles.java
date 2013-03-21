//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import react.Signal;
import react.Slot;

import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.util.Clock;
import static playn.core.PlayN.graphics;

import tripleplay.particle.Emitter;

/**
 * Manages all particle emitters.
 */
public class Particles
{
    /**
     * Creates an emitter that supports up to {@code maxParticles} particles at any one time. The
     * emitter is added to the root layer.
     *
     * @param image the image to use for each particle.
     */
    public Emitter createEmitter (int maxParticles, Image image) {
        return createEmitter(maxParticles, image, graphics().rootLayer());
    }

    /**
     * Creates an emitter that supports up to {@code maxParticles} particles at any one time.
     *
     * @param image the image to use for each particle.
     * @param onLayer the layer to which to add the layer which will render the particles.
     */
    public Emitter createEmitter (int maxParticles, Image image, GroupLayer onLayer) {
        final Emitter emitter = new Emitter(this, maxParticles, image);
        emitter._conn = _onPaint.connect(new Slot<Now>() { public void onEmit (Now now) {
            emitter.update(now.time, now.dt);
        }});
        onLayer.add(emitter.layer);
        return emitter;
    }

    /**
     * Updates all registered emitters. Call this from {@code Game.paint} or similar.
     *
     * @param clock an up-to-date frame clock.
     */
    public void paint (Clock clock) {
        _onPaint.emit(_now.update(clock.dt()));
    }

    /**
     * Clears the resources used by the custom shader that renders particles.
     */
    public void clear () {
        _shader.clearProgram();
    }

    float now () {
        return _now.time;
    }

    protected static class Now {
        public float time;
        public float dt;
        public Now update (float delta) {
            dt = delta/1000f;
            time += dt;
            return this;
        }
    }

    protected final Now _now = new Now();
    protected final Signal<Now> _onPaint = Signal.create();
    protected final ParticleShader _shader = new ParticleShader(graphics().ctx());
}
