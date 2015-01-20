//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import javax.swing.JComponent;

import pythagoras.f.IRectangle;

/**
 * Basic machinery for managing a java native child view.
 */
public class JavaNativeOverlay implements NativeOverlay
{
    /** Swing component that is being overlaid. */
    public final JComponent component;

    /**
     * Creates a new java native overlay for the given component.
     */
    public JavaNativeOverlay (JavaTPPlatform plat, JComponent component) {
        _plat = plat;
        this.component = component;
    }

    @Override public void setBounds (IRectangle bounds) {
        component.setBounds((int)bounds.x(), (int)bounds.y(),
                            (int)bounds.width(), (int)bounds.height());
    }

    @Override public void add () {
        if (component.getParent() == null) {
            _plat.addOverlay(this);
            didAdd();
        }
    }

    @Override public void remove () {
        if (component.getParent() != null) {
            _plat.removeOverlay(this);
            didRemove();
        }
    }

    /** Called if the view is added to the root. */
    protected void didAdd () {}

    /** Called if the view is removed from the root. */
    protected void didRemove () {}

    protected final JavaTPPlatform _plat;
}
