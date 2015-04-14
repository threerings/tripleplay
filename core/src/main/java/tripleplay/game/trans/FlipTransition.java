//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game.trans;

import pythagoras.f.FloatMath;

import playn.core.Platform;

import tripleplay.game.ScreenStack.Screen;
import tripleplay.shaders.RotateYBatch;
import tripleplay.util.Interpolator;

/**
 * Flips the current screen over, revealing the new screen as if it were on the reverse side of the
 * current screen.
 */
public class FlipTransition extends InterpedTransition<FlipTransition>
{
    /** Reverses this transition, making it flip the other direction. */
    public FlipTransition unflip () { _unflip = true; return this; }

    @Override public void init (Platform plat, Screen oscreen, Screen nscreen) {
        super.init(plat, oscreen, nscreen);
        nscreen.layer.setDepth(-1);
        _obatch = new RotateYBatch(plat.graphics().gl, 0.5f, 0.5f, 1);
        oscreen.layer.setBatch(_obatch);
        _nbatch = new RotateYBatch(plat.graphics().gl, 0.5f, 0.5f, 1);
        nscreen.layer.setBatch(_nbatch);
    }

    @Override public boolean update (Screen oscreen, Screen nscreen, float elapsed) {
        float pct = _interp.applyClamp(0, 1, elapsed, _duration);
        if (pct >= 0.5f && !_flipped) {
            nscreen.layer.setDepth(0);
            oscreen.layer.setDepth(-1);
            _flipped = true;
        }
        if (_unflip) pct = -pct;
        _obatch.angle = FloatMath.PI * pct;
        _nbatch.angle = FloatMath.PI * (pct - 1);
        return elapsed >= _duration;
    }

    @Override public void complete (Screen oscreen, Screen nscreen) {
        super.complete(oscreen, nscreen);
        oscreen.layer.setDepth(0);
        oscreen.layer.setBatch(null);
        nscreen.layer.setDepth(0);
        nscreen.layer.setBatch(null);
    }

    @Override protected Interpolator defaultInterpolator () {
        return Interpolator.LINEAR;
    }

    protected boolean _flipped, _unflip;
    protected RotateYBatch _obatch, _nbatch;
}
