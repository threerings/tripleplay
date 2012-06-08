//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game.trans;

import tripleplay.game.Screen;
import tripleplay.game.ScreenStack;
import tripleplay.util.Interpolator;

/**
 * Slides the old screen off, and the new screen on right behind.
 */
public class SlideTransition implements ScreenStack.Transition
{
    public SlideTransition up () { return dir(Dir.UP); }
    public SlideTransition down () { return dir(Dir.DOWN); }
    public SlideTransition left () { return dir(Dir.LEFT); }
    public SlideTransition right () { return dir(Dir.RIGHT); }
    public SlideTransition dir (Dir dir) { _dir = dir; return this; }

    public SlideTransition interp (Interpolator interp) { _interp = interp; return this; }
    public SlideTransition linear () { return interp(Interpolator.LINEAR); }
    public SlideTransition easeIn () { return interp(Interpolator.EASE_IN); }
    public SlideTransition easeOut () { return interp(Interpolator.EASE_OUT); }
    public SlideTransition easeInOut () { return interp(Interpolator.EASE_INOUT); }

    /** Configures the duration of the transition. */
    public SlideTransition duration (float duration) { _duration = duration; return this; }

    public SlideTransition (ScreenStack stack) {
        _originX = stack.originX;
        _originY = stack.originY;
    }

    @Override public void init (Screen oscreen, Screen nscreen) {
        switch (_dir) {
        case UP:
            _odx = _originX; _ody = _originY-oscreen.height();
            _nsx = _originX; _nsy = _originY+nscreen.height();
            break;
        case DOWN:
            _odx = _originX; _ody = _originY+oscreen.height();
            _nsx = _originX; _nsy = _originY-nscreen.height();
            break;
        case LEFT: default:
            _odx = _originX-oscreen.width(); _ody = _originY;
            _nsx = _originX+nscreen.width(); _nsy = _originY;
            break;
        case RIGHT:
            _odx = _originX+oscreen.width(); _ody = _originY;
            _nsx = _originX-nscreen.width(); _nsy = _originY;
            break;
        }
        _osx = oscreen.layer.transform().tx();
        _osy = oscreen.layer.transform().ty();
        nscreen.layer.setTranslation(_nsx, _nsy);
    }

    @Override public boolean update (Screen oscreen, Screen nscreen, float elapsed) {
        float ox = _interp.apply(_originX, _odx-_originX, elapsed, _duration);
        float oy = _interp.apply(_originY, _ody-_originY, elapsed, _duration);
        oscreen.layer.setTranslation(ox, oy);
        float nx = _interp.apply(_nsx, _originX-_nsx, elapsed, _duration);
        float ny = _interp.apply(_nsy, _originY-_nsy, elapsed, _duration);
        nscreen.layer.setTranslation(nx, ny);
        return elapsed >= _duration;
    }

    @Override public void complete (Screen oscreen, Screen nscreen) {
        oscreen.layer.setTranslation(_osx, _osy);
    }

    protected final float _originX, _originY;
    protected Dir _dir = Dir.LEFT;
    protected Interpolator _interp = Interpolator.EASE_INOUT;
    protected float _duration = 500;
    protected float _osx, _osy, _odx, _ody, _nsx, _nsy;
}
