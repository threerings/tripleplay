//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.game;

import tripleplay.ui.Background;
import tripleplay.ui.Layout;
import tripleplay.ui.Root;
import tripleplay.ui.SimpleStyles;
import tripleplay.ui.Style;
import tripleplay.ui.Stylesheet;
import tripleplay.ui.layout.AxisLayout;

import tripleplay.game.ScreenStack;

/**
 * A screen that contains UI elements.
 */
public abstract class TestScreen extends ScreenStack.UIScreen
{
    public TestScreen (int depth) {
        _depth = depth;
    }

    @Override public void wasShown () {
        super.wasShown();
        game().plat.log().info(this + ".wasShown()");
    }

    @Override public void wasHidden () {
        super.wasHidden();
        game().plat.log().info(this + ".wasHidden()");
    }

    @Override public void wasRemoved () {
        super.wasRemoved();
        game().plat.log().info(this + ".wasRemoved()");
        layer.close();
        iface.disposeRoot(_root);
    }

    @Override public void showTransitionCompleted () {
        super.showTransitionCompleted();
        game().plat.log().info(this + ".showTransitionCompleted()");
    }

    @Override public void hideTransitionStarted () {
        super.hideTransitionStarted();
        game().plat.log().info(this + ".hideTransitionStarted()");
    }

    @Override protected Root createRoot () {
        Root root = iface.createRoot(createLayout(), stylesheet());
        root.addStyles(Style.BACKGROUND.is(background()));
        root.setSize(size());
        createIface(root);
        return root;
    }

    /** Returns the stylesheet to use for this screen. */
    protected Stylesheet stylesheet () {
        return SimpleStyles.newSheet(game().plat.graphics());
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

    /** Override this method and create your UI in it. Add elements to {@code root}. */
    protected abstract void createIface (Root root);

    protected final int _depth;
    protected Root _root;
}
