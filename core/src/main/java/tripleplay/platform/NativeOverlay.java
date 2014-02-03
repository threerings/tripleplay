//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import pythagoras.f.IRectangle;

/**
 * A platform element that draws on top of the main playn root layer.
 */
public interface NativeOverlay
{
    /**
     * Sets the bounds of the overlay, in root coordinates.
     */
    void setBounds (IRectangle bounds);

    /**
     * Adds the native overlay to the display. If the overlay is already added, does nothing.
     */
    void add ();

    /**
     * Removes the native overlay from the display. If the overlay is already removed, does
     * nothing.
     */
    void remove ();
}
