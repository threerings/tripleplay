//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game.trans;

import pythagoras.f.FloatMath;
import pythagoras.f.MathUtil;

import static playn.core.PlayN.graphics;

import tripleplay.game.Screen;
import tripleplay.game.ScreenStack;
import tripleplay.shaders.RotateYShader;
import tripleplay.util.Interpolator;

/**
 * Flips the current screen over, revealing the new screen as if it were on the reverse side of the
 * current screen.
 */
public class FlipTransition extends InterpedTransition<FlipTransition>
{
    /** Reverses this transition, making it flip the other direction. */
    public FlipTransition unflip () { _unflip = true; return this; }

    @Override public void init (Screen oscreen, Screen nscreen) {
        nscreen.layer.setVisible(false);
        _shader = new RotateYShader(graphics().ctx(), 0.5f, 0.5f, 1);
        oscreen.layer.setShader(_shader);
    }

    @Override public boolean update (Screen oscreen, Screen nscreen, float elapsed) {
        float pct = _interp.apply(0, 1, elapsed, _duration);
        if (pct >= 0.5f && !_flipped) {
            nscreen.layer.setShader(_shader);
            nscreen.layer.setVisible(true);
            oscreen.layer.setVisible(false);
            oscreen.layer.setShader(null);
            _flipped = true;
        }
        if (_unflip) pct = -pct;
        if (_flipped) pct -= 1;
        _shader.angle = FloatMath.PI * pct;
        return elapsed >= _duration;
    }

    @Override public void complete (Screen oscreen, Screen nscreen) {
        oscreen.layer.setVisible(true);
        nscreen.layer.setVisible(true);
        oscreen.layer.setShader(null);
        nscreen.layer.setShader(null);
    }

    @Override protected Interpolator defaultInterpolator () {
        return Interpolator.EASE_OUT;
    }

    protected boolean _flipped, _unflip;
    protected RotateYShader _shader;
}
