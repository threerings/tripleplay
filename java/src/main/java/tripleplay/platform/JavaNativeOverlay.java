//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;

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
    public JavaNativeOverlay (JComponent component) {
        this.component = component;
    }

    @Override public void setBounds (IRectangle bounds) {
        component.setBounds(
            (int)bounds.x(), (int)bounds.y(), (int)bounds.width(), (int)bounds.height());
    }

    @Override public void add () {
        if (component.getParent() == null) {
            root().add(component);
            didAdd();
        }
    }

    @Override public void remove () {
        if (component.getParent() != null) {
            root().remove(component);
            didRemove();
        }
    }

    /** Called if the view is added to the root. */
    protected void didAdd () {}

    /** Called if the view is removed from the root. */
    protected void didRemove () {}

    /**
     * Gets the parent view for all Java native overlays, i.e. the layered pane of
     * {@link JavaTPPlatform#frame()}.
     */
    protected static JLayeredPane root () {
        return ((JavaTPPlatform)TPPlatform.instance()).frame().getLayeredPane();
    }
}
