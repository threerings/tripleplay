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
     * Creates a new layer for displaying this icon. The caller is takes ownership of the new
     * layer and is responsible for its destruction.
     */
    Layer render ();

    /**
     * Adds a callback to be notified when this icon has loaded. If the icon is already loaded, the
     * callback will be notified immediately; otherwise later on the main thread. The callback is
     * discarded once the icon is loaded. This mimics the behavior of {@link Image}.
     */
    void addCallback (Callback<? super Icon> callback);
}
