//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game.trans;

import pythagoras.f.FloatMath;

import playn.core.Platform;
import playn.core.Surface;
import playn.scene.Layer;

import tripleplay.game.ScreenStack.Screen;
import tripleplay.shaders.RotateYBatch;
import tripleplay.util.Interpolator;

/**
 * Opens the current screen like the page of a book, revealing the new screen beneath.
 */
public class PageTurnTransition extends InterpedTransition<PageTurnTransition>
{
    /**
     * Reverses this transition, making it a page close instead of open. Note that this changes the
     * interpolator, so if you want a custom interpolator, configure it <em>after</em> calling this
     * method.
     */
    public PageTurnTransition close () {
        _close = true;
        _interp = Interpolator.EASE_INOUT;
        return this;
    }

    @Override public void init (Platform plat, Screen oscreen, Screen nscreen) {
        super.init(plat, oscreen, nscreen);
        nscreen.layer.setDepth(_close ? 1 : -1);
        _toflip = _close ? nscreen : oscreen;
        _batch = new RotateYBatch(plat.graphics().gl, 0f, 0.5f, 1.5f);
        _toflip.layer.setBatch(_batch);
        final float fwidth = _toflip.size().width(), fheight = _toflip.size().height();
        _shadow = new Layer() {
            @Override protected void paintImpl (Surface surf) {
                surf.setAlpha(_alpha).setFillColor(0xFF000000).fillRect(0, 0, fwidth/4, fheight);
            }
        };
        _toflip.layer.addAt(_shadow, fwidth, 0);
        updateAngle(0); // start things out appropriately
    }

    @Override public boolean update (Screen oscreen, Screen nscreen, float elapsed) {
        updateAngle(elapsed);
        return elapsed >= _duration;
    }

    @Override public void complete (Screen oscreen, Screen nscreen) {
        super.complete(oscreen, nscreen);
        _shadow.close();
        nscreen.layer.setDepth(0);
        _toflip.layer.setBatch(null);
    }

    @Override protected float defaultDuration () {
        return 1500;
    }

    @Override protected Interpolator defaultInterpolator () {
        return Interpolator.EASE_IN;
    }

    protected void updateAngle (float elapsed) {
        float pct = _interp.applyClamp(0, 0.5f, elapsed, _duration);
        if (_close) pct = 0.5f - pct;
        _alpha = pct;
        _batch.angle = FloatMath.PI * pct;
    }

    protected float _alpha;
    protected boolean _close;

    protected Screen _toflip;
    protected Layer _shadow;
    protected RotateYBatch _batch;
}
