//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Mouse;

/**
* Dispatches user input from {@link Mouse}.
*/
public class MouseInput extends Input<Mouse.Listener>
{
    /**
     * Configures a reaction to be notified of mouse activity. On mouse activity, reactions will be
     * scanned from most-recently-registered to least-recently-registered and hit-tested. Thus more
     * recently registered reactions that overlap previously registered reactions will take
     * precedence. TODO: use layer depth information to hit test based on depth.
     *
     * <p> Mouse motion events are dispatched to the region over which the mouse is hovering, if
     * any (with overlapping regions resolved as described above). After a mouse down, subsequent
     * movement, up and wheel events will be dispatched to the reaction that successfully
     * hit-tested the mouse start. In the absence of an active down reaction, mouse wheel events
     * are dispatched to the region over which the mouse is hovering, if any. </p>
     *
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (Region region, Mouse.Listener listener) {
        return _reactor.register(region, listener);
    }

    /** Receives input from the PlayN Mouse service. */
    protected class MouseReactor extends Reactor<Mouse.Listener> implements Mouse.Listener {
        @Override public void onMouseDown (Mouse.ButtonEvent event) {
            _target = hitTest(event);
            if (_target != null) {
                _down = true;
                _target.onMouseDown(event);
            }
        }
        @Override public void onMouseMove (Mouse.MotionEvent event) {
            if (!_down) {
                _target = hitTest(event);
            }
            if (_target != null) {
                _target.onMouseMove(event);
            }
        }
        @Override public void onMouseUp (Mouse.ButtonEvent event) {
            if (_down) {
                _down = false;
                Mouse.Listener oldHover = _target;
                // now that the mouse is released, we may be hovering over a new region; rechecking
                // hover here ensures that subsequent mouse wheel events will be correctly
                // dispatched even if the mouse is not moved
                _target = hitTest(event);
                // notify onMouseUp last so that our internal invariants are not broken if the
                // listener decides to throw an unchecked exception
                oldHover.onMouseUp(event);
            }
        }
        @Override public void onMouseWheelScroll (Mouse.WheelEvent event) {
            if (_target != null) {
                _target.onMouseWheelScroll(event);
            }
        }

        protected Mouse.Listener _target;
        protected boolean _down;
    };

    protected final MouseReactor _reactor = new MouseReactor();
    public final Mouse.Listener mlistener = _reactor;
}
