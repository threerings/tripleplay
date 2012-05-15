//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game;

import playn.core.PlayN;

import tripleplay.ui.Background;
import tripleplay.ui.Interface;
import tripleplay.ui.Layout;
import tripleplay.ui.Root;
import tripleplay.ui.SimpleStyles;
import tripleplay.ui.Style;
import tripleplay.ui.Stylesheet;
import tripleplay.ui.layout.AxisLayout;

/**
 * A screen that contains UI elements.
 */
public abstract class IfaceScreen extends Screen
{
    /** Manages our user interfaces. */
    public final Interface iface = new Interface();

    @Override public void wasAdded () {
        PlayN.log().info(this + ".wasAdded()");
        _root = iface.createRoot(createLayout(), stylesheet(), layer);
        _root.addStyles(Style.BACKGROUND.is(background()));
        _root.setSize(width(), height());
        createIface();
    }

    @Override public void wasShown () {
        PlayN.log().info(this + ".wasShown()");
    }

    @Override public void wasHidden () {
        PlayN.log().info(this + ".wasHidden()");
    }

    @Override public void wasRemoved () {
        PlayN.log().info(this + ".wasRemoved()");
        layer.destroy();
        iface.destroyRoot(_root);
    }

    @Override public void showTransitionCompleted () {
        PlayN.log().info(this + ".showTransitionCompleted()");
    }

    @Override public void hideTransitionStarted () {
        PlayN.log().info(this + ".hideTransitionStarted()");
    }

    @Override public void update (float delta) {
        iface.update(delta);
    }

    @Override public void paint (float alpha) {
        iface.paint(alpha);
    }

    /** Returns the stylesheet to use for this screen. */
    protected Stylesheet stylesheet () {
        return SimpleStyles.newSheet();
    }

    /** Creates the layout for the interface root. The default is a vertical axis layout. */
    protected Layout createLayout () {
        return AxisLayout.vertical();
    }

    /** Returns the background to use for this screen. */
    protected Background background () {
        return Background.solid(0xFFCCCCCC).inset(15, 10);
    }

    /** Override this method and create your UI in it. Add elements to {@link #_root}. */
    protected abstract void createIface ();

    protected Root _root;
}
