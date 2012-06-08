//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game.trans;

import playn.core.ImmediateLayer;
import playn.core.Surface;
import static playn.core.PlayN.graphics;

import pythagoras.f.FloatMath;
import pythagoras.f.MathUtil;

import tripleplay.game.Screen;
import tripleplay.game.ScreenStack;
import tripleplay.shaders.RotateYShader;
import tripleplay.util.Interpolator;

/**
 * Opens the current screen like the page of a book, revealing the new screen beneath.
 */
public class PageTurnTransition implements ScreenStack.Transition
{
    /** Configures the duration of the transition. */
    public PageTurnTransition duration (float duration) { _duration = duration; return this; }

    /** Reverses this transition, making it a page close instead of open. */
    public PageTurnTransition close () { _close = true; return this; }

    @Override public void init (Screen oscreen, Screen nscreen) {
        nscreen.layer.setDepth(_close ? 1 : -1);
        _toflip = _close ? nscreen : oscreen;
        _interp = _close ? Interpolator.EASE_INOUT : Interpolator.EASE_IN;
        _shader = new RotateYShader(graphics().ctx(), 0f, 0.5f);
        _toflip.layer.setShader(_shader);
        final float fwidth = _toflip.width(), fheight = _toflip.height();
        _shadow = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            public void render (Surface surf) {
                surf.setAlpha(_alpha);
                surf.setFillColor(0xFF000000);
                surf.fillRect(0, 0, fwidth/4, fheight);
            }
        });
        _toflip.layer.addAt(_shadow, fwidth, 0);
    }

    @Override public boolean update (Screen oscreen, Screen nscreen, float elapsed) {
        float pct = MathUtil.clamp(_interp.apply(0, 0.5f, elapsed, _duration), 0, 0.5f);
        if (_close) pct = 0.5f - pct;
        _alpha = pct;
        _shader.angle = FloatMath.PI * pct;
        return elapsed >= _duration;
    }

    @Override public void complete (Screen oscreen, Screen nscreen) {
        _shadow.destroy();
        nscreen.layer.setDepth(0);
        _toflip.layer.setShader(null);
    }

    protected float _duration = 1500;
    protected Interpolator _interp;
    protected float _alpha;
    protected boolean _close;

    protected Screen _toflip;
    protected ImmediateLayer _shadow;
    protected RotateYShader _shader;
}
