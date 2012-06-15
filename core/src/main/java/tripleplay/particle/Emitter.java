//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import java.util.ArrayList;
import java.util.List;

import playn.core.Image;
import playn.core.ImmediateLayer;
import playn.core.Surface;
import playn.core.gl.GLContext;
import static playn.core.PlayN.graphics;
import playn.core.gl.IndexedTrisShader;

/**
 * Emits and updates particles according to a particle system configuration.
 */
public class Emitter
{
    /** The layer to which this emitter is attached. */
    public final ImmediateLayer layer;

    /** The generator that adds new particles to this emitter. */
    public Generator generator = Generator.NOOP;

    /** The initializers used by this emitter. */
    public final List<Initializer> initters = new ArrayList<Initializer>();

    /** The effectors used by this emitter. */
    public final List<Effector> effectors = new ArrayList<Effector>();

    /**
     * Creates an emitter that supports up to {@code maxParticles} particles at any one time.
     *
     * @param image the image to use for each particle.
     */
    public Emitter (final int maxParticles, Image image) {
        this.layer = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            @Override public void render (Surface surface) {
                int tex = _image.ensureTexture(false, false);
                _buffer.render(_shader.prepareTexture(tex, 1), _image.width(), _image.height());
            }
        });
        _buffer = new ParticleBuffer(maxParticles);
        _image = image;
        _shader = new IndexedTrisShader(graphics().ctx()) {
            @Override protected int startVerts () { return maxParticles * 4; }
        };
    }

    /**
     * Updates this emitter. Call this from {@link Game#update} or thereabouts.
     *
     * @param delta the time that has elapsed since the last frame, in milliseconds.
     */
    public void update (float delta) {
        float secs = delta/1000;
        _now += secs;
        // TODO: update and cache our layer's local transform?
        if (generator.generate(this, _now, secs)) {
            generator = Generator.NOOP;
            // TODO: emit generator exhausted event
        }
        _buffer.apply(effectors, _now, secs);
    }

    /**
     * Adds the specified number of particles.
     */
    public void addParticles (int count) {
        for (int ii = 0, ll = initters.size(); ii < ll; ii++) initters.get(ii).willInit(count);
        _buffer.add(count, _now, initters);
    }

    protected final ParticleBuffer _buffer;
    protected final Image _image;
    protected final IndexedTrisShader _shader;

    protected float _now;
}
