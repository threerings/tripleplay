//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game;

import tripleplay.anim.Animator;

/**
 * An abstract screen that will contain animations.
 */
public abstract class AnimScreen extends Screen
{
    /** Manages animations. */
    public final Animator anim = Animator.create();

    // create your scene graph in wasShown or wasAdded and register animations any time; while the
    // screen is showing, animations will run; while the screen is hidden, animations will be
    // paused

    @Override public void update (float delta) {
        super.update(delta);
        _elapsed += delta;
    }

    @Override public void paint (float alpha) {
        super.paint(alpha);
        anim.update(_elapsed + alpha * updateRate());
    }

    /** Returns your game's update rate. */
    protected abstract float updateRate ();

    protected float _elapsed;
}
