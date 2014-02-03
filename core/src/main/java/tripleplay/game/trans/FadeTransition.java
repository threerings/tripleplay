//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game.trans;

import tripleplay.game.Screen;
import tripleplay.game.ScreenStack;

/**
 * Fades the new screen in front of the old one.
 */
public class FadeTransition extends InterpedTransition<SlideTransition>
{
    public FadeTransition (ScreenStack stack) {
    }

    @Override public void init (Screen oscreen, Screen nscreen) {
        nscreen.layer.setAlpha(0);
    }

    @Override public boolean update (Screen oscreen, Screen nscreen, float elapsed) {
        float nalpha = _interp.applyClamp(0, 1, elapsed, _duration);
        nscreen.layer.setAlpha(nalpha);
        return elapsed >= _duration;
    }

    @Override public void complete (Screen oscreen, Screen nscreen) {
        super.complete(oscreen, nscreen);
        nscreen.layer.setAlpha(1);
    }
}
