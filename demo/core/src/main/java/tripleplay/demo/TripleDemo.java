//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import playn.core.Game;
import playn.core.PlayN;
import playn.core.util.Clock;

import tripleplay.game.ScreenStack;

public class TripleDemo extends Game.Default
{
    /** Args from the Java bootstrap class. */
    public static String[] mainArgs = {};

    public static final int UPDATE_RATE = 50;

    public TripleDemo () {
        super(UPDATE_RATE);
    }

    @Override public void init () {
        _screens.push(new DemoMenuScreen(_screens));
    }

    @Override public void update (int delta) {
        _clock.update(delta);
        _screens.update(delta);
    }

    @Override public void paint (float alpha) {
        _clock.paint(alpha);
        _screens.paint(_clock);
    }

    protected final Clock.Source _clock = new Clock.Source(UPDATE_RATE);

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
