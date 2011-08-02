//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game;

import forplay.core.ForPlay;
import forplay.core.GroupLayer;

/**
 * Contains the UI for a single game screen.
 *
 * @see ScreenStack
 */
public abstract class Screen
{
    /** The layer on which all of this screen's UI must be placed. */
    public final GroupLayer layer = ForPlay.graphics().createGroupLayer();

    // the following methods provide hooks into the visibility lifecycle of a screen, which takes
    // the form: added -> shown -> { hidden -> shown -> ... } -> hidden -> removed

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

    /** Called every frame while a screen is visible. */
    public void update (float delta) {
    }

    /** Called every frame while a screen is visible. */
    public void paint (float alpha) {
    }
}
