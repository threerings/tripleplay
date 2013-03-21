//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game;

import playn.core.PlayN;
import playn.core.GroupLayer;
import playn.core.util.Clock;

/**
 * Contains the UI for a single game screen.
 *
 * @see ScreenStack
 */
public abstract class Screen
{
    /** The layer on which all of this screen's UI must be placed. */
    public final GroupLayer layer = PlayN.graphics().createGroupLayer();

    // the following methods provide hooks into the visibility lifecycle of a screen, which takes
    // the form: added -> shown -> { hidden -> shown -> ... } -> hidden -> removed

    /** Returns the width of this screen. This is used for transitions.
     * Defaults to the width of the entire view. */
    public float width () {
        return PlayN.graphics().width();
    }

    /** Returns the height of this screen. This is used for transitions.
     * Defaults to the height of the entire view. */
    public float height () {
        return PlayN.graphics().height();
    }

    /** Called when a screen is added to the screen stack for the first time. */
    public void wasAdded () {
    }

    /** Called when a screen becomes the top screen, and is therefore made visible. */
    public void wasShown () {
    }

    /** Called when a screen is no longer the top screen (having either been pushed down by another
     * screen, or popped off the stack). */
    public void wasHidden () {
    }

    /** Called when a screen has been removed from the stack. This will always be preceeded by a
     * call to {@link #wasHidden}, though not always immediately. */
    public void wasRemoved () {
    }

    /** Called when this screen's transition into view has completed. {@link #wasShown} is called
     * immediately before the transition begins, and this method is called when it ends. */
    public void showTransitionCompleted () {
    }

    /** Called when this screen's transition out of view has started. {@link #wasHidden} is called
     * when the hide transition completes. */
    public void hideTransitionStarted () {
    }

    /** Called on every update, while a screen is visible. */
    public void update (int delta) {
    }

    /** Called on every paint, while a screen is visible. */
    public void paint (Clock clock) {
    }
}
