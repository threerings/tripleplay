//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.FloatMath;
import pythagoras.f.Point;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.Pointer;

/**
 * The base class for all user interface widgets. Provides helper methods for managing a canvas
 * into which a widget is rendered when its state changes.
 */
public abstract class Widget<T extends Widget<T>> extends Element<T>
{
    /**
     * Widgets that are interactive can call this method to wire up the appropriate listeners.
     */
    protected void enableInteraction () {
        // we receive all pointer events for a root in that root and then dispatch events via our
        // custom mechanism from there on down
        layer.setHitTester(new Layer.HitTester() {
            public Layer hitTest (Layer layer, Point p) {
                return (isVisible() && contains(p.x, p.y)) ? layer : null;
            }
        });

        // add a pointer listener for handling mouse events
        PlayN.pointer().addListener(layer, new Pointer.Listener() {
            public void onPointerStart (Pointer.Event event) {
                // clear focus; if the click is on the focused item, it'll get focus again
                root()._iface.clearFocus();
                Widget.this.onPointerStart(event, event.localX(), event.localY());
            }
            public void onPointerDrag (Pointer.Event event) {
                Widget.this.onPointerDrag(event, event.localX(), event.localY());
            }
            public void onPointerEnd (Pointer.Event event) {
                Widget.this.onPointerEnd(event, event.localX(), event.localY());
            }
        });
    }

    /**
     * Called when the a touch/drag is started within the bounds of this component.
     *
     * @param event the pointer event that triggered this call.
     * @param x the x-coordinate of the event, translated into this element's coordinates.
     * @param y the y-coordinate of the event, translated into this element's coordinates.
     */
    protected void onPointerStart (Pointer.Event event, float x, float y) {
    }

    /**
     * Called when a touch that started within the bounds of this component is dragged. The drag
     * may progress outside the bounds of this component, but the events will still be dispatched
     * to this component until the touch is released.
     *
     * @param event the pointer event that triggered this call.
     * @param x the x-coordinate of the event, translated into this element's coordinates.
     * @param y the y-coordinate of the event, translated into this element's coordinates.
     */
    protected void onPointerDrag (Pointer.Event event, float x, float y) {
    }

    /**
     * Called when a touch that started within the bounds of this component is released. The
     * coordinates may be outside the bounds of this component, but the touch in question started
     * inside this component's bounds.
     *
     * @param event the pointer event that triggered this call.
     * @param x the x-coordinate of the event, translated into this element's coordinates.
     * @param y the y-coordinate of the event, translated into this element's coordinates.
     */
    protected void onPointerEnd (Pointer.Event event, float x, float y) {
    }

    /**
     * Handles the maintenance of a canvas image and layer for displaying a chunk of pre-rendered
     * graphics.
     */
    protected class Glyph {
        public Glyph () {
        }
        /** Ensures that the canvas image is at least the specified dimensions and cleared to all
         * transparent pixels. Also creates and adds the image layer to the containing widget if
         * needed. */
        public void prepare (float width, float height) {
            // recreate our canvas if we need more room than we have (TODO: should we ever shrink it?)
            int cwidth = FloatMath.iceil(width), cheight = FloatMath.iceil(height);
            if (_image == null || _image.width() < cwidth || _image.height() < cheight) {
                _image = PlayN.graphics().createImage(cwidth, cheight);
                if (_layer != null) _layer.setImage(_image);
            } else {
                _image.canvas().clear();
            }
            if (_layer == null) layer.add(_layer = PlayN.graphics().createImageLayer(_image));
        }

        /** Returns the layer that contains our glyph image. Valid after {@link #prepare}. */
        public ImageLayer layer () {
            return _layer;
        }

        /** Returns the canvas into which drawing may be done. Valid after {@link #prepare}. */
        public Canvas canvas () {
            return _image.canvas();
        }

        /** Destroys the layer and image, removing them from the containing widget. */
        public void destroy () {
            if (_layer != null) {
                _layer.destroy();
                _layer = null;
            }
            _image = null;
        }

        protected CanvasImage _image;
        protected ImageLayer _layer;
    }
}
