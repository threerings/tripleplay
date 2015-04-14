//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import java.util.ArrayList;
import java.util.List;

import react.Signal;
import react.Slot;
import react.UnitSlot;

import playn.core.Clock;
import playn.core.Image;
import playn.core.QuadBatch;
import playn.core.Surface;
import playn.core.Tile;
import playn.scene.Layer;
import playn.scene.LayerUtil;

/**
 * Emits and updates particles according to a particle system configuration.
 */
public class Emitter
{
    /** The layer to which this emitter is attached. */
    public final Layer layer;

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
     * Creates an emitter that uses {@code batch} to render its particles. When this emitter's
     * layer is added to the scene graph, it will connect itself to {@code paint} to drive the
     * particle animations, and when its layer is removed, it will disconnect from {@code paint}.
     *
     * @param batch the particle batch to use when rendering our particles.
     * @param paint the paint signal which will drive this emitter.
     * @param maxParticles the maximum number of active particles.
     * @param tile the texture to use when rendering particles.
     */
    public Emitter (final ParticleBatch batch, final Signal<Clock> paint, final int maxParticles,
                    final Tile tile) {
        this.layer = new Layer() {
            @Override protected void paintImpl (Surface surface) {
                QuadBatch obatch = surface.pushBatch(batch);
                _buffer.render(batch.prepare(tile, maxParticles), tile.width(), tile.height());
                surface.popBatch(obatch);
            }
        };
        _buffer = new ParticleBuffer(maxParticles);

        LayerUtil.bind(layer, paint, new Slot<Clock>() {
            public void onEmit (Clock clock) { paint(clock); }
        });
    }

    /**
     * Adds the specified number of particles. One usually does not call this manually, but rather
     * configures {@link #generator} with a generator that adds particles as desired.
     */
    public void addParticles (int count) {
        if (_buffer.isFull()) return;
        for (int ii = 0, ll = initters.size(); ii < ll; ii++) initters.get(ii).willInit(count);
        _buffer.add(count, _time, initters);
    }

    /**
     * Configures this emitter to destroy its layer when it runs out of particles.
     */
    public void destroyOnEmpty () {
        onEmpty.connect(new UnitSlot() { @Override public void onEmit () { layer.close(); }});
    }

    protected void paint (Clock clock) {
        float dt = clock.dt/1000f, now = _time + dt;
        _time = now;

        // TODO: update and cache our layer's local transform?
        if (generator != null && generator.generate(this, now, dt)) {
            generator = null;
            onExhausted.emit(this);
        }
        if (_buffer.apply(effectors, now, dt) == 0 && generator == null) {
            onEmpty.emit(this);
        }
    }

    protected final ParticleBuffer _buffer;
    protected float _time;
}
