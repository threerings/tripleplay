//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.scene.Layer;
import react.RFuture;

/**
 * An interface for icons.
 */
public interface Icon
{
    /**
     * Returns the width of this icon. If the icon is not yet loaded, this should return zero.
     */
    float width ();

    /**
     * Returns the height of this icon. If the icon is not yet loaded, this should return zero.
     */
    float height ();

    /**
     * Creates a new layer for displaying this icon. The caller is takes ownership of the new layer
     * and is responsible for its destruction.
     */
    Layer render ();

    /**
     * A future which is completed when this icon has loaded.
     */
    RFuture<Icon> state ();
}
