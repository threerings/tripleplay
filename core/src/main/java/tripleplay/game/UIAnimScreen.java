//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game;

import tripleplay.ui.Interface;

/**
 * An abstract screen that contains UI and animations.
 */
public abstract class UIAnimScreen extends AnimScreen
{
    /** Manages our user interface roots. */
    public final Interface iface = new Interface();

    // override wasShown (or wasAdded) and create a root
    // @Override public void wasShown () {
    //     super.wasShown();
    //     _root = iface.createRoot(createLayout(), stylesheet(), layer);
    //     _root.addStyles(Style.BACKGROUND.is(background()));
    //     _root.setSize(width(), height());
    //     // create your user interface
    // }

    // destroy your user interface in wasHidden (or wasRemoved)
    // @Override public void wasHidden () {
    //     super.wasHidden();
    //     iface.destroyRoot(_root);
    // }

    @Override public void update (float delta) {
        super.update(delta);
        iface.update(delta);
    }

    @Override public void paint (float alpha) {
        super.paint(alpha);
        iface.paint(alpha);
    }
}
