//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game.trans;

import playn.core.Platform;

import tripleplay.game.ScreenStack.Screen;
import tripleplay.game.ScreenStack;

/**
 * Slides the old screen off, and the new screen on right behind.
 */
public class SlideTransition extends InterpedTransition<SlideTransition>
{
    public SlideTransition up () { return dir(Dir.UP); }
    public SlideTransition down () { return dir(Dir.DOWN); }
    public SlideTransition left () { return dir(Dir.LEFT); }
    public SlideTransition right () { return dir(Dir.RIGHT); }
    public SlideTransition dir (Dir dir) { _dir = dir; return this; }

    public SlideTransition (ScreenStack stack) {
        _originX = stack.originX;
        _originY = stack.originY;
    }

    @Override public void init (Platform plat, Screen oscreen, Screen nscreen) {
        super.init(plat, oscreen, nscreen);
        switch (_dir) {
        case UP:
            _odx = _originX; _ody = _originY-oscreen.size().height();
            _nsx = _originX; _nsy = _originY+nscreen.size().height();
            break;
        case DOWN:
            _odx = _originX; _ody = _originY+oscreen.size().height();
            _nsx = _originX; _nsy = _originY-nscreen.size().height();
            break;
        case LEFT: default:
            _odx = _originX-oscreen.size().width(); _ody = _originY;
            _nsx = _originX+nscreen.size().width(); _nsy = _originY;
            break;
        case RIGHT:
            _odx = _originX+oscreen.size().width(); _ody = _originY;
            _nsx = _originX-nscreen.size().width(); _nsy = _originY;
            break;
        }
        _osx = oscreen.layer.tx();
        _osy = oscreen.layer.ty();
        nscreen.layer.setTranslation(_nsx, _nsy);
    }

    @Override public boolean update (Screen oscreen, Screen nscreen, float elapsed) {
        float ox = _interp.applyClamp(_originX, _odx-_originX, elapsed, _duration);
        float oy = _interp.applyClamp(_originY, _ody-_originY, elapsed, _duration);
        oscreen.layer.setTranslation(ox, oy);
        float nx = _interp.applyClamp(_nsx, _originX-_nsx, elapsed, _duration);
        float ny = _interp.applyClamp(_nsy, _originY-_nsy, elapsed, _duration);
        nscreen.layer.setTranslation(nx, ny);
        return elapsed >= _duration;
    }

    @Override public void complete (Screen oscreen, Screen nscreen) {
        super.complete(oscreen, nscreen);
        oscreen.layer.setTranslation(_osx, _osy);
    }

    @Override protected float defaultDuration () {
        return 500;
    }

    protected final float _originX, _originY;
    protected Dir _dir = Dir.LEFT;
    protected float _osx, _osy, _odx, _ody, _nsx, _nsy;
}
