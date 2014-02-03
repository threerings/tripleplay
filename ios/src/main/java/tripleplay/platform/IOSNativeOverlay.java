//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import cli.MonoTouch.UIKit.UIView;
import cli.System.Drawing.RectangleF;
import playn.ios.IOSPlatform;
import pythagoras.f.IRectangle;

/**
 * Basic machinery for managing an iOS native child view.
 */
public class IOSNativeOverlay
    implements NativeOverlay
{
    /** Child view instance. */
    public final UIView view;

    /**
     * Creates a new iOS overlay to manage the given view.
     */
    public IOSNativeOverlay (UIView view) {
        this.view = view;
    }

    @Override public void setBounds (IRectangle bounds) {
        _bounds = bounds;
        updateBounds();
    }

    @Override public void add () {
        if (!view.IsDescendantOfView(root())) {
            root().Add(view);
            didAdd();
        }
    }

    @Override public void remove () {
        if (view.IsDescendantOfView(root())) {
            view.RemoveFromSuperview();
            didRemove();
        }
    }

    /** Called if the view is added to the root. */
    protected void didAdd () {}

    /** Called if the view is removed from the root. */
    protected void didRemove () {}

    /**
     * Updates the bounds to match the currently requested bounds, giving subclasses an
     * opportunity to adjust.
     */
    protected void updateBounds () {
        if (_bounds == null) return;
        RectangleF bounds = new RectangleF(_bounds.x(), _bounds.y(),
            _bounds.width(), _bounds.height());
        adjustBounds(bounds);
        view.set_Frame(bounds);
    }

    /**
     * Gets the parent view for all iOS native overlays, i.e. {@link IOSPlatform#uiOverlay()}.
     */
    protected static UIView root () {
        return ((IOSTPPlatform)TPPlatform.instance()).overlay();
    }

    /**
     * Adjusts the given bounds, if necessary. By default, no adjustment is performed.
     */
    protected void adjustBounds (RectangleF bounds) {}

    /** Bounds, if set. */
    protected IRectangle _bounds;
}
