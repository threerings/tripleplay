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
     * <p> After a mouse down, subsequent move and up events will be dispatched to the reaction that
     * successfully hit-tested the mouse start. If no mouse down reaction is active, movement is
     * dispatched via the same hit-testing mechanism. Mouse wheel scrolls are dispatched the the
     * active down reaction, or the most recent movement hit if there is no active down reaction.
     * </p>
     *
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (Region region, Mouse.Listener listener) {
        return _reactor.register(region, listener);
    }

    /** Receives input from the PlayN Mouse service. */
    protected class MouseReactor extends Reactor<Mouse.Listener>
        implements Mouse.Listener {
        @Override public void onMouseDown (Mouse.ButtonEvent event) {
            _active = hitTest(event);
            if (_active != null) {
                _active.onMouseDown(event);
            }
        }
        @Override public void onMouseMove (Mouse.MotionEvent event) {
            if (_active != null) {
                _active.onMouseMove(event);
            } else {
                _lastMoved = hitTest(event);
                if (_lastMoved != null) {
                    _lastMoved.onMouseMove(event);
                }
            }
        }
        @Override public void onMouseUp (Mouse.ButtonEvent event) {
            if (_active != null) {
                _active.onMouseUp(event);
                _active = null;
            }
        }
        @Override public void onMouseWheelScroll (Mouse.WheelEvent event) {
            if (_active != null) {
                _active.onMouseWheelScroll(event);
            } else if (_lastMoved != null) {
                _lastMoved.onMouseWheelScroll(event);
            }
        }

        protected Mouse.Listener _active, _lastMoved;
    };

    protected final MouseReactor _reactor = new MouseReactor();
    public final Mouse.Listener mlistener = _reactor;
}
