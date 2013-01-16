//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.gesture;

import playn.core.PlayN;
import playn.core.Touch.Event;

import pythagoras.f.Point;

/**
 * A node in the history of the current user gesture. Events that spawn a new node:
 *  * User touches a finger to the display (START)
 *  * User removes a finger from the display (END)
 *  * User moves far enough to trigger a drag in a new direction (MOVE)
 *  * User stops dragging long enough to be considered "still" (PAUSE)
 *  * User cancels a touch (CANCEL)
 */
public class GestureNode
{
    public enum Type { START, END, MOVE, PAUSE, CANCEL }

    /**
     * A timestamp for this node.
     */
    public final double timestamp;

    /**
     * The state change that caused the registration of this node.
     */
    public final Type type;

    /**
     * The touch event for this node.
     */
    public final Event touch;

    public GestureNode (Type type, Event touch) {
        this.timestamp = PlayN.currentTime();
        this.type = type;
        this.touch = touch;
    }

    /**
     * Convenience method to get a Point for the local location of the touch event in this node.
     */
    public Point location () {
        return new Point(touch.localX(), touch.localY());
    }
}
