//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import java.util.ArrayList;
import java.util.List;

import react.Connection;
import react.Signal;
import react.UnitSlot;

import playn.core.Image;
import playn.core.ImmediateLayer;
import playn.core.Surface;
import static playn.core.PlayN.graphics;

import tripleplay.util.Destroyable;

/**
 * Emits and updates particles according to a particle system configuration.
 */
public class Emitter
    implements Destroyable
{
    /** The layer to which this emitter is attached. */
    public final ImmediateLayer layer;

    /** The generator that adds new particles to this emitter. */
    public Generator generator;

    /** The initializers used by this emitter. */
    public final List<Initializer> initters = new ArrayList<Initializer>();

    /** The effectors used by this emitter. */
    public final List<Effector> effectors = new ArrayList<Effector>();

    /** A signal emitted when the generator for this emitter is exhausted. */
    public final Signal<Emitter> onExhausted = Signal.create();

    /** A signal emitted when this emitter has no live particles and no generator. */
    public final Signal<Emitter> onEmpty = Signal.create();

    /**
     * Adds the specified number of particles. One usually does not call this manually, but rather
     * configures {@link #generator} with a generator that adds particles as desired.
     */
    public void addParticles (int count) {
        if (_buffer.isFull()) return;
        for (int ii = 0, ll = initters.size(); ii < ll; ii++) initters.get(ii).willInit(count);
        _buffer.add(count, _parts.now(), initters);
    }

    /**
     * Unregisters this emitter from the particles manager.
     */
    @Override public void destroy () {
        layer.destroy();
        _conn.disconnect();
    }

    /**
     * Configures this emitter to self-destruct when it runs out of particles.
     */
    public void destroyOnEmpty () {
        onEmpty.connect(new UnitSlot() { @Override public void onEmit () { destroy(); }});
    }

    Emitter (Particles parts, final int maxParticles, final Image image) {
        this.layer = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            @Override public void render (Surface surface) {
                int tex = image.ensureTexture();
                _buffer.render(_parts._shader.prepare(tex, maxParticles),
                               image.width(), image.height());
            }
        });
        _parts = parts;
        _buffer = new ParticleBuffer(maxParticles);
    }

    void update (float now, float dt) {
        // TODO: update and cache our layer's local transform?
        if (generator != null && generator.generate(this, now, dt)) {
            generator = null;
            onExhausted.emit(this);
        }
        if (_buffer.apply(effectors, now, dt) == 0 && generator == null) {
            onEmpty.emit(this);
        }
    }

    protected final Particles _parts;
    protected final ParticleBuffer _buffer;

    /** Our connection to our {@link Particles} (filled in by same). */
    protected Connection _conn;
}
