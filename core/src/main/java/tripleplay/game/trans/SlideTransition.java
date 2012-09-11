//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game.trans;

import tripleplay.game.Screen;
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

    @Override public void init (Screen oscreen, Screen nscreen) {
        super.init(oscreen, nscreen);
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
