//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Pointer;

/**
* Dispatches user input from {@link Pointer}.
*/
public class PointerInput extends Input<Pointer.Listener>
{
    /** A listener that will call {@link Action#onTrigger} when the pointer is pressed. */
    public static abstract class Action extends Pointer.Adapter {
        /** Called when the user triggers a reactor. */
        public abstract void onTrigger ();

        @Override public void onPointerStart (Pointer.Event event) {
            onTrigger();
        }
    }

    /** Receives input from the PlayN Pointer service. */
    public final Pointer.Listener plistener = new PointerReactor();

    /**
     * Configures a reaction to be notified of pointer activity. On pointer start, reactions will be
     * scanned from most-recently-registered to least-recently-registered and hit-tested. Thus more
     * recently registered reactions that overlap previously registered reactions will take
     * precedence. TODO: use layer depth information to hit test based on depth.
     *
     * <p> Subsequent pointer drag and end events will be dispatched to the reaction that
     * successfully hit-tested the pointer start. </p>
     *
     * @return a handle that can be used to clear this registration.
     */
    @Override public Registration register (Region region, Pointer.Listener listener) {
        return ((PointerReactor)plistener).register(region, listener);
    }

    protected class PointerReactor extends Reactor<Pointer.Listener> implements Pointer.Listener {
        @Override public void onPointerStart (Pointer.Event event) {
            _active = hitTest(event);
            if (_active != null) {
                _active.onPointerStart(event);
            }
        }
        @Override public void onPointerDrag (Pointer.Event event) {
            if (_active != null) {
                _active.onPointerDrag(event);
            }
        }
        @Override public void onPointerEnd (Pointer.Event event) {
            if (_active != null) {
                _active.onPointerEnd(event);
                _active = null;
            }
        }
        @Override public void onPointerCancel (Pointer.Event event) {
            if (_active != null) {
                _active.onPointerCancel(event);
                _active = null;
            }
        }
        protected Pointer.Listener _active;
    }
}
