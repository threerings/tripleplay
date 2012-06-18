//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import playn.core.Game;
import playn.core.PlayN;

import tripleplay.game.ScreenStack;

public class TripleDemo implements Game
{
    /** Args from the Java bootstrap class. */
    public static String[] mainArgs;

    @Override public void init () {
        _screens.push(new DemoMenuScreen(_screens));
    }

    @Override public void update (float delta) {
        _screens.update(delta);
    }

    @Override public void paint (float alpha) {
        _screens.paint(alpha);
    }

    @Override public int updateRate () {
        return 16;
    }

    protected final ScreenStack _screens = new ScreenStack() {
        @Override protected void handleError (RuntimeException error) {
            PlayN.log().warn("Screen failure", error);
        }
        @Override protected Transition defaultPushTransition () {
            return slide();
        }
        @Override protected Transition defaultPopTransition () {
            return slide().right();
        }
    };
}
