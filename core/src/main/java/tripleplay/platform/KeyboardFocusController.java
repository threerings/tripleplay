//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import pythagoras.f.Point;

/** Defines application hooks into controlling focus on native text fields. */
public interface KeyboardFocusController
{
    /** Return true if the keyboard focus should be relinquished for a pointer that starts at the
     * given location.
     *
     * The default (with no KeyboardFocusController specified) is to relinquish focus for any point
     * that does not start on a native text field. With this method, fine control is possible,
     * allowing some in-game UI to be interacted with without losing focus if desired. */
    boolean unfocusForLocation (Point location);

    /** Called each time a field has the return key pressed. Return true to relinquish the
     * keyboard. */
    boolean unfocusForEnter ();
}
