//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import playn.core.Image;

/**
 * A native overlay that simply draws an playn image.
 */
public interface ImageOverlay extends NativeOverlay
{
    /**
     * Gets the image.
     */
    Image image ();

    /**
     * Queues up a repaint. Games must call this whenever the image is updated.
     */
    void repaint ();
}
