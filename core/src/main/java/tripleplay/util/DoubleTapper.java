//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Event;
import playn.scene.Pointer;

/**
 * Detects double taps on a layer, using a threshold time between taps. Two taps that occur
 * within a time span shorter than the threshold are considered a double tap.
 */
public class DoubleTapper extends Tapper
{
    /** Maximum time between taps for the 2nd to be considered a double. */
    public final static double DOUBLE_TIME = 500;

    @Override public void onTap (Event.XY where) {
        super.onTap(where);
        if (where.time - _tapTime < DOUBLE_TIME) onDoubleTap(where);
        else _tapTime = where.time;
    }

    /**
     * Called when a double tap occurs. This is a simpler version of {@link
     * #onDoubleTap(Event.XY)}, for subclasses that don't require the event position.
     */
    public void onDoubleTap () {}

    /**
     * Called when a double tap occurs. By default, this just calls {@link #onDoubleTap()}.
     * Subclasses overriding this needn't call super.
     * @param where the pointer's end position (for the 2nd tap)
     */
    public void onDoubleTap (Event.XY where) { onDoubleTap(); }

    /** Last tap time recorded. */
    protected double _tapTime;
}
