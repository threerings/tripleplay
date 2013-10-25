//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.ImageLayer;
import pythagoras.f.Dimension;
import react.Connection;
import react.Slot;
import react.Value;
import react.ValueView;
import tripleplay.util.Layers;

import static playn.core.PlayN.graphics;

/**
 * A root that renders its layer into an image. See {@link Layers#capture(
 * playn.core.Layer, float, float)}. Takes care of hooking into the layout system and updating
 * the image size appropriately.
 */
public class CapturedRoot extends Root
{
    /**
     * Creates a new captured root with the given values.
     */
    public CapturedRoot (Interface iface, Layout layout, Stylesheet sheet) {
        super(iface, layout, sheet);
    }

    /**
     * Gets the image value onto which the root is rendered. This may be null if no validation
     * has yet occurred and may change value when the root's size changes.
     */
    public ValueView<CanvasImage> image () {
        return _image;
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
        CanvasImage old = _image.get();
        if (old == null || old.width() != width || old.height() != height) {
            _image.update(graphics().createImage(width, height));
        }
        return this;
    }

    @Override public void layout () {
        super.layout();
        // capture the new layout
        Canvas canvas = _image.get().canvas();
        canvas.clear();
        Layers.capture(layer, canvas);
    }

    /**
     * Wraps this captured root in a Widget, using the root's image for size computation and
     * displaying the root's image on its layer.
     */
    protected class Embedded extends Widget<Embedded> {
        @Override protected Class<?> getStyleClass () {
            return Embedded.class;
        }

        @Override protected LayoutData createLayoutData (float hintX, float hintY) {
            return new LayoutData() {
                @Override public Dimension computeSize (float hintX, float hintY) {
                    CanvasImage image = _image.get();
                    return image == null ? new Dimension(0, 0) :
                        new Dimension(image.width(), image.height());
                }
            };
        }

        @Override protected void wasAdded () {
            super.wasAdded();
            // connect to the root's image and update our layer
            _conn = image().connectNotify(new Slot<CanvasImage>() {
                @Override public void onEmit (CanvasImage event) {
                    updateImage(event);
                    invalidate();
                }
            });
        }

        @Override protected void wasRemoved () {
            super.wasRemoved();
            updateImage(null);
            _conn.disconnect();
            _conn = null;
        }

        protected void updateImage (CanvasImage image) {
            if (image == null) {
                // we should never be going back to null but handle it anyway
                if (_ilayer != null) _ilayer.destroy();
                _ilayer = null;
                return;
            }
            if (_ilayer == null) layer.add(_ilayer = graphics().createImageLayer());
            _ilayer.setImage(image);
        }

        /** The captured root image layer, if set. */
        protected ImageLayer _ilayer;

        /** The connection to the captured root's image, or null if we're not added. */
        protected Connection _conn;
    }

    /** The image to with the layer is rendered. */
    protected Value<CanvasImage> _image = Value.create(null);
}
