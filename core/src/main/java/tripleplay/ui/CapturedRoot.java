//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.Point;

import react.Closeable;
import react.Slot;
import react.Value;
import react.ValueView;

import playn.core.QuadBatch;
import playn.core.Texture;
import playn.core.TextureSurface;
import playn.scene.Layer;
import playn.scene.ImageLayer;

/**
 * A root that renders everything into a single texture. Takes care of hooking into the layout
 * system and updating the image size appropriately. This trades off real-time rendering
 * performance (which is much improved because the entire UI is one texture), with memory use (a
 * backing texture is needed for the whole UI) and the expense of re-rendering the entire UI
 * whenever anything changes.
 */
public class CapturedRoot extends Root
{
    /**
     * Creates a new captured root with the given values.
     *
     * @param defaultBatch the quad batch to use when capturing the UI scene graph. This is
     * usually your game's default quad batch.
     */
    public CapturedRoot (Interface iface, Layout layout, Stylesheet sheet, QuadBatch defaultBatch) {
        super(iface, layout, sheet);
        _defaultBatch = defaultBatch;
    }

    /**
     * Gets the texture into which the root is rendered. This may be null if no validation has yet
     * occurred and may change value when the root's size changes.
     */
    public ValueView<Texture> texture () {
        return _texture;
    }

    /**
     * Creates a widget that will display this root in an image layer. The computed size of the
     * returned widget will be the size of this root, but the widget's layout will not affect the
     * root.
     */
    public Element<?> createWidget () {
        return new Embedded();
    }

    @Override public Root setSize (float width, float height) {
        super.setSize(width, height);
        // update the image to the new size, if it's changed
        Texture old = _texture.get();
        if (old == null || old.displayWidth != width || old.displayHeight != height) {
            _texture.update(iface.plat.graphics().createTexture(width, height, textureConfig()));
        }
        return this;
    }

    @Override public void layout () {
        super.layout();
        Texture texture = _texture.get();
        TextureSurface surf = new TextureSurface(iface.plat.graphics(), _defaultBatch, texture);
        surf.begin().clear();
        layer.paint(surf);
        surf.end().close();
    }

    /**
     * Returns the configuration to use when creating our backing texture.
     */
    protected Texture.Config textureConfig () {
        return Texture.Config.DEFAULT;
    }

    /**
     * Wraps this captured root in a Widget, using the root's image for size computation and
     * displaying the root's image on its layer.
     */
    protected class Embedded extends Widget<Embedded> {

        protected Embedded() {
            layer.setHitTester(new Layer.HitTester() {
                @Override public Layer hitTest(Layer layer, Point point) {
                    return CapturedRoot.this.layer.hitTest(point);
                }
            });
            layer.setInteractive(true);
        }

        @Override protected Class<?> getStyleClass () {
            return Embedded.class;
        }

        @Override protected LayoutData createLayoutData (float hintX, float hintY) {
            return new LayoutData() {
                @Override public Dimension computeSize (float hintX, float hintY) {
                    Texture tex = _texture.get();
                    return tex == null ? new Dimension(0, 0) : new Dimension(
                        tex.displayWidth, tex.displayHeight);
                }
            };
        }

        @Override protected void wasAdded () {
            super.wasAdded();
            // update our layer when the texture is regenerated
            _conn = _texture.connectNotify(new Slot<Texture>() {
                @Override public void onEmit (Texture tex) {
                    update(tex);
                    invalidate();
                }
            });
        }

        @Override protected void wasRemoved () {
            super.wasRemoved();
            update(null);
            _conn = Closeable.close(_conn);
        }

        protected void update (Texture tex) {
            if (tex == null) {
                // we should never be going back to null but handle it anyway
                if (_ilayer != null) _ilayer.close();
                _ilayer = null;
                return;
            }
            if (_ilayer == null) layer.add(_ilayer = new ImageLayer());
            _ilayer.setTile(tex);
        }

        /** The captured root image layer, if set. */
        protected ImageLayer _ilayer;

        /** The connection to the captured root's image, or null if we're not added. */
        protected Closeable _conn = Closeable.NOOP;
    }

    protected final QuadBatch _defaultBatch;

    /** The texure to with the layer is rendered. */
    protected Value<Texture> _texture = Value.create(null);
}
