//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Layer;
import playn.core.Pointer;

import pythagoras.f.IPoint;
import pythagoras.f.Point;

/**
 * A helpful class for implementing drag and drop behavior.
 */
public abstract class Dragger
    implements Input.Region, Pointer.Listener
{
    /**
     * Called when a pointer click triggers our start region to check whether we are allowed to
     * start a drag right now or not.
     * @return true if a drag is allowed, false otherwise.
     */
    public boolean canDrag () {
        return true;
    }

    /**
     * Called when a drag has started.
     * @param startLoc the point at which the drag started.
     */
    public void onDragStart (IPoint startLoc) {
    }

    /**
     * Called when a drag is in progress.
     * @param curLoc the current pointer position.
     * @param startLoc the point at which the drag started.
     */
    public void onDragged (IPoint curLoc, IPoint startLoc) {
    }

    /**
     * Called when the pointer was released during a drag.
     * @param curLoc the current (and final) pointer position.
     * @param startLoc the point at which the drag started.
     */
    public void onDragEnd (IPoint curLoc, IPoint startLoc) {
    }

    public Dragger (Input.Region region) {
        _region = region;
    }

    @Override public boolean hasExpired () {
        return _region.hasExpired();
    }

    @Override public boolean hitTest (IPoint p) {
        return canDrag() && _region.hitTest(p);
    }

    @Override public void onPointerStart (Pointer.Event event) {
        _startLoc = new Point(event.x(), event.y());
        onDragStart(_startLoc);
    }

    @Override public void onPointerDrag (Pointer.Event event) {
        onDragged(_curLoc.set(event.x(), event.y()), _startLoc);
    }

    @Override public void onPointerEnd (Pointer.Event event) {
        onDragEnd(_curLoc.set(event.x(), event.y()), _startLoc);
    }

    protected Input.Region _region;

    protected Point _startLoc;
    protected Point _curLoc = new Point();
}
