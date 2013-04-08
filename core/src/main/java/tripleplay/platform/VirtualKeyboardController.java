package tripleplay.platform;

import pythagoras.f.Point;

public interface VirtualKeyboardController
{
    /**
     * Return true if the keyboard should be hidden for a touch that starts at the given location.
     *
     * The default (with no VirtualKeyboardController specified) is to hide the virtual keyboard
     * for any touch that does not start on a native text field. With this method, fine control is
     * possible, allowing some in-game UI to be interacted with without hiding the virtual keyboard
     * if desired.
     */
    boolean hideKeyboardForTouch (Point location);

    /**
     * Called each time a field has the return key pressed. Return true to hide the keyboard
     * (the default).
     */
    boolean hideKeyboardOnEnter ();
}
