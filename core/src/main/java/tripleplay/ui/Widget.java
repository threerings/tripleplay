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
        layer.addListener(new Pointer.Listener() {
            public void onPointerStart (Pointer.Event event) {
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
     * Extends base Glyph to automatically wire up to this Widget's {{@link #layer}.
     */
    protected class Glyph extends tripleplay.util.Glyph {
        public Glyph () {
            super(layer);
        }
    }
}
