//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Events;
import playn.core.Pointer;
import pythagoras.f.Point;

/**
 * Detects taps on a layer. This is a simple implementation using a threshold distance. If the
 * pointer is dragged less than the threshold, a call to {@link #onTap(Events.Position)} is
 * generated.
 */
public class Tapper
    implements Pointer.Listener
{
    /** Default threshold distance. */
    public static final float DEFAULT_TAP_DIST = 15;

    /** Default threshold distance, set to {@link #DEFAULT_TAP_DIST} squared. */
    public static final float DEFAULT_TAP_DIST_SQ = DEFAULT_TAP_DIST * DEFAULT_TAP_DIST;

    /** Square of the threshold distance for this tapper, defaults to
     * {@link #DEFAULT_TAP_DIST_SQ}. */
    public float maxTapDistSq = DEFAULT_TAP_DIST_SQ;

    /**
     * Called when a tap occurs. This is a simpler version of {@link #onTap(Events.Position)}, for
     * subclasses that don't require the event position.
     */
    public void onTap () {}

    /**
     * Called when a tap occurs. By default, this just calls {@link #onTap()}. Subclasses
     * overriding needn't call super.
     * @param where the pointer's end position
     */
    public void onTap (Events.Position where) {
        onTap();
    }

    @Override public void onPointerStart (Pointer.Event event) {
        _tracking = new Tracking(event);
    }

    @Override public void onPointerEnd (Pointer.Event event) {
        if (_tracking == null) return;
        _tracking.drag(event);
        if (_tracking.maxMovedSq < maxTapDistSq) onTap(event);
        _tracking = null;
    }

    @Override public void onPointerDrag (Pointer.Event event) {
        if (_tracking == null) return;
        _tracking.drag(event);
    }

    @Override public void onPointerCancel (Pointer.Event event) {
        _tracking = null;
    }

    /** Represents tracking info for tap detection. */
    protected static class Tracking {
        public Point start;
        public double startTime;
        public float maxMovedSq;

        public Tracking (Events.Position where) {
            start = new Point(where.x(), where.y());
            startTime = where.time();
        }

        public void drag (Events.Position where) {
            maxMovedSq = Math.max(maxMovedSq, dist(where));
        }

        public float dist (Events.Position where) {
            float x = where.x() - start.x, y = where.y() - start.y;
            return x * x + y * y;
        }
    }

    /** Data for current tracking, if any. */
    protected Tracking _tracking;
}
