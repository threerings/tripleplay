//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.game;

import static playn.core.PlayN.log;

import tripleplay.game.UIScreen;
import tripleplay.ui.Background;
import tripleplay.ui.Layout;
import tripleplay.ui.Root;
import tripleplay.ui.SimpleStyles;
import tripleplay.ui.Style;
import tripleplay.ui.Stylesheet;
import tripleplay.ui.layout.AxisLayout;

/**
 * A screen that contains UI elements.
 */
public abstract class TestScreen extends UIScreen
{
    public TestScreen (int depth) {
        _depth = depth;
    }

    @Override public void wasAdded () {
        super.wasAdded();
        _root = iface.createRoot(createLayout(), stylesheet(), layer);
        _root.addStyles(Style.BACKGROUND.is(background()));
        _root.setSize(width(), height());
        createIface();
    }

    @Override public void wasShown () {
        super.wasShown();
        log().info(this + ".wasShown()");
    }

    @Override public void wasHidden () {
        super.wasHidden();
        log().info(this + ".wasHidden()");
    }

    @Override public void wasRemoved () {
        super.wasRemoved();
        log().info(this + ".wasRemoved()");
        layer.destroy();
        iface.destroyRoot(_root);
    }

    @Override public void showTransitionCompleted () {
        super.showTransitionCompleted();
        log().info(this + ".showTransitionCompleted()");
    }

    @Override public void hideTransitionStarted () {
        super.hideTransitionStarted();
        log().info(this + ".hideTransitionStarted()");
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
        int borderColor = (_depth % 2 == 0) ? 0xFF99CCFF : 0xFFCC99FF;
        return Background.bordered(0xFFCCCCCC, borderColor, 15).inset(15, 10);
    }

    /** Override this method and create your UI in it. Add elements to {@link #_root}. */
    protected abstract void createIface ();

    protected final int _depth;
    protected Root _root;
}
