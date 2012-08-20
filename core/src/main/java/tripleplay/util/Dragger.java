//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Pointer;

import pythagoras.f.IPoint;
import pythagoras.f.Point;

/**
 * A helpful class for implementing drag and drop behavior. Use in conjunction with an input region
 * like so:
 * <pre>{@code
 * input.register(new SizedLayerRegion(layer) {
 *   public boolean hitTest (IPoint point) {
 *     return can_start_drag && super.hitTest(point);
 *   }
 *   // also override hasExpired if you desire custom expiry
 * }, new Dragger() {
 *   public void onDragStart (IPoint start) {
 *     // ..drag bits..
 *   }
 *   // etc.
 * });
 * }</pre>
 */
public abstract class Dragger
    implements Pointer.Listener
{
    /**
     * Called when a drag has started.
     * @param start the point at which the drag started.
     */
    public void onDragStart (IPoint start) {
    }

    /**
     * Called when a drag is in progress.
     * @param current the current pointer position.
     * @param start the point at which the drag started.
     */
    public void onDragged (IPoint current, IPoint start) {
    }

    /**
     * Called when the pointer was released during a drag.
     * @param current the current (and final) pointer position.
     * @param start the point at which the drag started.
     */
    public void onDragEnd (IPoint current, IPoint start) {
    }

    /**
     * Called when the pointer interaction was canceled during a drag.
     * @param start the point at which the drag started.
     */
    public void onDragCancel (IPoint start) {
    }

    @Override public void onPointerStart (Pointer.Event event) {
        onDragStart(_start.set(event.x(), event.y()));
    }

    @Override public void onPointerDrag (Pointer.Event event) {
        onDragged(_current.set(event.x(), event.y()), _start);
    }

    @Override public void onPointerEnd (Pointer.Event event) {
        onDragEnd(_current.set(event.x(), event.y()), _start);
    }

    @Override public void onPointerCancel (Pointer.Event event) {
        onDragCancel(_start);
    }

    protected Point _start = new Point(), _current = new Point();
}
