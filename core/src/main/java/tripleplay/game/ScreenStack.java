//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.MathUtil;

import playn.core.Game;
import playn.core.ImmediateLayer;
import playn.core.Surface;
import static playn.core.PlayN.graphics;

import tripleplay.util.Interpolator;

/**
 * Manages a stack of screens. The stack supports useful manipulations: pushing a new screen onto
 * the stack, replacing the screen at the top of the stack with a new screen, popping a screen from
 * the stack.
 *
 * <p> Care is taken to preserve stack invariants even in the face of errors thrown by screens when
 * being added, removed, shown or hidden. Users can override {@link #handleError} and either simply
 * log the error, or rethrow it if they would prefer that a screen failure render their entire
 * screen stack unusable. </p>
 */
public abstract class ScreenStack
{
    /** Direction constants, used by transitions. */
    public static enum Dir { UP, DOWN, LEFT, RIGHT; };

    /** Implements a particular screen transition. */
    public interface Transition {
        /** Allows the transition to pre-compute useful values. This will immediately be followed
         * by call to {@link #update} with an elapsed time of zero. */
        void init (Screen oscreen, Screen nscreen);

        /** Called every frame to update the transition
         * @param oscreen the outgoing screen.
         * @param nscreen the incoming screen.
         * @param elapsed the elapsed time since the transition started (in millis if that's what
         * your game is sending to {@link ScreenStack#update}).
         * @return false if the transition is not yet complete, true when it is complete. The stack
         * will automatically destroy/hide the old screen when the transition returns true.
         */
        boolean update (Screen oscreen, Screen nscreen, float elapsed);
    }

    /** Simply puts the new screen in place and removes the old screen. */
    public static final Transition NOOP = new Transition() {
        public void init (Screen oscreen, Screen nscreen) {} // noopski!
        public boolean update (Screen oscreen, Screen nscreen, float elapsed) { return true; }
    };

    /** Slides the old screen off, and the new screen on right behind. */
    public class SlideTransition implements Transition {
        public SlideTransition dir (Dir dir) { _dir = dir; return this; }
        public SlideTransition up () { return dir(Dir.UP); }
        public SlideTransition down () { return dir(Dir.DOWN); }
        public SlideTransition left () { return dir(Dir.LEFT); }
        public SlideTransition right () { return dir(Dir.RIGHT); }

        public SlideTransition interp (Interpolator interp) { _interp = interp; return this; }
        public SlideTransition linear () { return interp(Interpolator.LINEAR); }
        public SlideTransition easeIn () { return interp(Interpolator.EASE_IN); }
        public SlideTransition easeOut () { return interp(Interpolator.EASE_OUT); }
        public SlideTransition easeInOut () { return interp(Interpolator.EASE_INOUT); }

        public SlideTransition duration (float duration) { _duration = duration; return this; }

        @Override public void init (Screen oscreen, Screen nscreen) {
            switch (_dir) {
            case UP:
                _odx = originX; _ody = originY-oscreen.height();
                _nsx = originX; _nsy = originY+nscreen.height();
                break;
            case DOWN:
                _odx = originX; _ody = originY+oscreen.height();
                _nsx = originX; _nsy = originY-nscreen.height();
                break;
            case LEFT: default:
                _odx = originX-oscreen.width(); _ody = originY;
                _nsx = originX+nscreen.width(); _nsy = originY;
                break;
            case RIGHT:
                _odx = originX+oscreen.width(); _ody = originY;
                _nsx = originX-nscreen.width(); _nsy = originY;
                break;
            }
            nscreen.layer.setTranslation(_nsx, _nsy);
        }

        @Override public boolean update (Screen oscreen, Screen nscreen, float elapsed) {
            float ox = _interp.apply(originX, _odx-originX, elapsed, _duration);
            float oy = _interp.apply(originY, _ody-originY, elapsed, _duration);
            oscreen.layer.setTranslation(ox, oy);
            float nx = _interp.apply(_nsx, originX-_nsx, elapsed, _duration);
            float ny = _interp.apply(_nsy, originY-_nsy, elapsed, _duration);
            nscreen.layer.setTranslation(nx, ny);
            return elapsed >= _duration;
        }

        protected Dir _dir = Dir.LEFT;
        protected Interpolator _interp = Interpolator.EASE_INOUT;
        protected float _duration = 500;
        protected float _odx, _ody, _nsx, _nsy;
    }

    /** Peels the current screen off like the page of a book, revealing the new screen beneath. */
    public class PageFlipTransition implements Transition {
        public PageFlipTransition duration (float duration) { _duration = duration; return this; }
        public PageFlipTransition unflip () { _unflip = true; return this; }

        @Override public void init (Screen oscreen, Screen nscreen) {
            nscreen.layer.setDepth(_unflip ? 1 : -1);
            _toflip = _unflip ? nscreen : oscreen;
            _osx = _toflip.layer.transform().scaleX();
            _osy = _toflip.layer.transform().scaleY();
            if (_unflip) {
                _ss = 0.00001f; _sr = 1;
                _sa = 0; _ar = 0.5f;
            } else {
                _ss = 1; _sr = -1;
                _sa = 0.5f; _ar = -0.5f;
            }
            final float fwidth = _toflip.width(), fheight = _toflip.height();
            _shadow = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
                public void render (Surface surf) {
                    surf.setAlpha(_alpha);
                    surf.setFillColor(0xFF000000);
                    surf.fillRect(0, 0, fwidth, fheight);
                }
            });
            _toflip.layer.addAt(_shadow, fwidth, 0);
        }

        @Override public boolean update (Screen oscreen, Screen nscreen, float elapsed) {
            if (elapsed >= _duration) {
                _shadow.destroy();
                nscreen.layer.setDepth(0);
                _toflip.layer.setScale(_osx, _osy);
                return true;
            }
            float scale = MathUtil.clamp(_interp.apply(_ss, _sr, elapsed, _duration), 0, 1);
            _alpha = MathUtil.clamp(_interp.apply(_sa, _ar, elapsed, _duration), 0, 1);
            _toflip.layer.setScale(_osx * scale, _osy);
            return false;
        }

        protected float _duration = 1000;
        protected Interpolator _interp = Interpolator.EASE_INOUT;
        protected float _alpha;
        protected boolean _unflip;

        protected Screen _toflip;
        protected float _ss, _sr, _sa, _ar;
        protected float _osx, _osy;
        protected ImmediateLayer _shadow;
    }

    /** The x-coordinate at which screens are located. Defaults to 0. */
    public float originX = 0;

    /** The y-coordinate at which screens are located. Defaults to 0. */
    public float originY = 0;

    /** Creates a slide transition. */
    public SlideTransition slide () { return new SlideTransition(); }

    /** Creates a page flip transition. */
    public PageFlipTransition pageFlip () { return new PageFlipTransition(); }

    /**
     * {@link #push(Screen,Transition)} with the default transition.
     */
    public void push (Screen screen) {
        push(screen, defaultPushTransition());
    }

    /**
     * Pushes the supplied screen onto the stack, making it the visible screen. The currently
     * visible screen will be hidden.
     * @throws IllegalArgumentException if the supplied screen is already in the stack.
     */
    public void push (Screen screen, Transition trans) {
        if (_screens.isEmpty()) {
            addAndShow(screen);
        } else {
            final Screen otop = top();
            transition(new Transitor(otop, screen, trans) {
                protected void onComplete() { hide(otop); }
            });
        }
    }

    /**
     * {@link #push(Iterable,Transition)} with the default transition.
     */
    public void push (Iterable<? extends Screen> screens) {
        push(screens, defaultPushTransition());
    }

    /**
     * Pushes the supplied set of screens onto the stack, in order. The last screen to be pushed
     * will also be shown, using the supplied transition. Note that the transition will be from the
     * screen that was on top prior to this call.
     */
    public void push (Iterable<? extends Screen> screens, Transition trans) {
        if (!screens.iterator().hasNext()) {
            throw new IllegalArgumentException("Cannot push empty list of screens.");
        }
        if (_screens.isEmpty()) {
            for (Screen screen : screens) add(screen);
            show(top());
        } else {
            final Screen otop = top();
            Screen last = null;
            for (Screen screen : screens) {
                if (last != null) add(last);
                last = screen;
            }
            transition(new Transitor(otop, last, trans) {
                protected void onComplete() { hide(otop); }
            });
        }
    }

    /**
     * {@link #popTo(Screen,Transition)} with the default transition.
     */
    public void popTo (Screen newTopScreen) {
        popTo(newTopScreen, defaultPopTransition());
    }

    /**
     * Pops the top screen from the stack until the specified screen has become the
     * topmost/visible screen.  If newTopScreen is null or is not on the stack, this will remove
     * all screens.
     */
    public void popTo (Screen newTopScreen, Transition trans) {
        // remove all intervening screens
        while (_screens.size() > 1 && _screens.get(1) != newTopScreen) {
            removeNonTop(_screens.get(1));
        }
        // now just pop the top screen
        remove(top(), trans);
    }

    /**
     * {@link #replace(Screen,Transition)} with the default transition.
     */
    public void replace (Screen screen) {
        replace(screen, defaultPushTransition());
    }

    /**
     * Pops the current screen from the top of the stack and pushes the supplied screen on as its
     * replacement.
     * @throws IllegalArgumentException if the supplied screen is already in the stack.
     */
    public void replace (Screen screen, Transition trans) {
        if (_screens.isEmpty()) {
            addAndShow(screen);
        } else {
            final Screen otop = top();
            transition(new Transitor(otop, screen, trans) {
                protected void onComplete () {
                    hide(otop);
                    remove(otop);
                }
            });
        }
    }

    /**
     * {@link #remove(Screen,Transition)} with the default transition.
     */
    public boolean remove (Screen screen) {
        return remove(screen, defaultPopTransition());
    }

    /**
     * Removes the specified screen from the stack. If it is the currently visible screen, it will
     * first be hidden, and the next screen below in the stack will be made visible.
     */
    public boolean remove (Screen screen, Transition trans) {
        if (top() == screen && _screens.size() > 1) {
            transition(new Untransitor(screen, _screens.get(1), trans) {
                protected void onComplete () {
                    hide(_oscreen);
                    removeNonTop(_oscreen);
                }
            });
            return true;

        } else {
            return removeNonTop(screen);
        }
    }

    /**
     * Updates the currently visible screen. A screen stack client should call this method from
     * {@link Game#update}.
     */
    public void update (float delta) {
        if (_transitor != null) _transitor.update(delta);
        else if (!_screens.isEmpty()) top().update(delta);
    }

    /**
     * Paints the currently visible screen. A screen stack client should call this method from
     * {@link Game#paint}.
     */
    public void paint (float alpha) {
        if (_transitor != null) _transitor.paint(alpha);
        else if (!_screens.isEmpty()) top().paint(alpha);
    }

    protected Transition defaultPushTransition () {
        return NOOP;
    }

    protected Transition defaultPopTransition () {
        return NOOP;
    }

    protected Screen top () {
        return _screens.get(0);
    }

    protected void add (Screen screen) {
        if (_screens.contains(screen)) {
            throw new IllegalArgumentException("Cannot add screen to stack twice.");
        }
        _screens.add(0, screen);
        try { screen.wasAdded(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected void addAndShow (Screen screen) {
        add(screen);
        show(screen);
    }

    protected void show (Screen screen) {
        graphics().rootLayer().add(screen.layer);
        try { screen.wasShown(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected void hide (Screen screen) {
        graphics().rootLayer().remove(screen.layer);
        try { screen.wasHidden(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected boolean removeNonTop (Screen screen) {
        boolean removed = _screens.remove(screen);
        if (removed) {
            try { screen.wasRemoved(); }
            catch (RuntimeException e) { handleError(e); }
        }
        return removed;
    }

    protected void transition (Transitor transitor) {
        if (_transitor != null) _transitor.complete();
        _transitor = transitor;
    }

    protected class Transitor {
        public Transitor (Screen oscreen, Screen nscreen, Transition trans) {
            _oscreen = oscreen;
            _nscreen = nscreen;
            _trans = trans;
            _trans.init(oscreen, nscreen);
            didInit();
        }

        public void update (float delta) {
            _oscreen.update(delta);
            _nscreen.update(delta);
            _elapsed += delta;
            if (_trans.update(_oscreen, _nscreen, _elapsed)) {
                complete();
            }
        }

        public void paint (float alpha) {
            _oscreen.paint(alpha);
            _nscreen.paint(alpha);
        }

        public void complete () {
            _transitor = null;
            // make sure the new screen is in the right position
            _nscreen.layer.setTranslation(originX, originY);
            _nscreen.showTransitionCompleted();
            onComplete();
        }

        protected void didInit () {
            _oscreen.hideTransitionStarted();
            addAndShow(_nscreen);
        }

        protected void onComplete () {}

        protected final Screen _oscreen, _nscreen;
        protected final Transition _trans;
        protected float _elapsed;
    }

    protected class Untransitor extends Transitor {
        public Untransitor (Screen oscreen, Screen nscreen, Transition trans) {
            super(oscreen, nscreen, trans);
        }

        @Override protected void didInit () {
            _oscreen.hideTransitionStarted();
            show(_nscreen);
        }
    }

    /** Called if any exceptions are thrown by the screen callback functions. */
    protected abstract void handleError (RuntimeException error);

    /** The currently executing transition, or null. */
    protected Transitor _transitor;

    /** Containts the stacked screens from top-most, to bottom-most. */
    protected final List<Screen> _screens = new ArrayList<Screen>();
}
