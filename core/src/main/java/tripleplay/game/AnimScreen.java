//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game;

import playn.core.util.Clock;

import tripleplay.anim.Animator;

/**
 * An abstract screen that will contain animations.
 */
public abstract class AnimScreen extends Screen
{
    /** Manages animations. */
    public final Animator anim = new Animator();

    // create your scene graph in wasShown or wasAdded and register animations any time; while the
    // screen is showing, animations will run; while the screen is hidden, animations will be
    // paused

    @Override public void paint (Clock clock) {
        super.paint(clock);
        anim.paint(clock);
    }
}
