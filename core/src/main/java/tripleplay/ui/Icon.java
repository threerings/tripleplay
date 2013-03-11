//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.Layer;
import playn.core.util.Callback;

/**
 * An interface for icons.
 */
public interface Icon
{
    /**
     * Returns the width of this icon.
     */
    float width ();

    /**
     * Returns the height of this icon.
     */
    float height ();

    /**
     * Creates a new layer for displaying this icon.
     */
    Layer render ();

    /**
     * Adds a callback to be notified when this icon has loaded. If the icon is
     * already loaded, the callback will be notified immediately; otherwise on the main playn
     * thread at a later time. The callback is discarded once the icon is loaded.
     *
     * This mimicks the behavior of {@link Image} since the most common case is an
     * {@link ImageIcon}.
     */
    void addCallback (Callback<? super Icon> callback);
}
