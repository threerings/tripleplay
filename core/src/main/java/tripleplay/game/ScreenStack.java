//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import playn.core.Game;
import static playn.core.PlayN.graphics;

import tripleplay.game.trans.FlipTransition;
import tripleplay.game.trans.PageTurnTransition;
import tripleplay.game.trans.SlideTransition;

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
    /** Implements a particular screen transition. */
    public interface Transition {
        /** Direction constants, used by transitions. */
        enum Dir { UP, DOWN, LEFT, RIGHT; };

        /** Allows the transition to pre-compute useful values. This will immediately be followed
         * by call to {@link #update} with an elapsed time of zero. */
        void init (Screen oscreen, Screen nscreen);

        /** Called every frame to update the transition
         * @param oscreen the outgoing screen.
         * @param nscreen the incoming screen.
         * @param elapsed the elapsed time since the transition started (in millis if that's what
         * your game is sending to {@link ScreenStack#update}).
         * @return false if the transition is not yet complete, true when it is complete.
         */
        boolean update (Screen oscreen, Screen nscreen, float elapsed);

        /** Called when the transition is complete. This is where the transition should clean up
         * any temporary bits and restore the screens to their original state. The stack will
         * automatically destroy/hide the old screen after calling this method. Also note that this
         * method may be called <em>before</em> the transition signals completion, if a new
         * transition is started and this transition needs be aborted. */
        void complete (Screen oscreen, Screen nscreen);
    }

    /** Used to operate on screens. See {@link #remove(Predicate)}. */
    public interface Predicate {
        /** Returns true if the screen matches the predicate. */
        boolean apply (Screen screen);
    }

    /** Simply puts the new screen in place and removes the old screen. */
    public static final Transition NOOP = new Transition() {
        public void init (Screen oscreen, Screen nscreen) {} // noopski!
        public boolean update (Screen oscreen, Screen nscreen, float elapsed) { return true; }
        public void complete (Screen oscreen, Screen nscreen) {} // noopski!
    };

    /** The x-coordinate at which screens are located. Defaults to 0. */
    public float originX = 0;

    /** The y-coordinate at which screens are located. Defaults to 0. */
    public float originY = 0;

    /** Creates a slide transition. */
    public SlideTransition slide () { return new SlideTransition(this); }

    /** Creates a page turn transition. */
    public PageTurnTransition pageTurn () { return new PageTurnTransition(); }

    /** Creates a flip transition. */
    public FlipTransition flip () { return new FlipTransition(); }

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
            justShow(top());
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
            justRemove(_screens.get(1));
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
            final Screen otop = _screens.remove(0);
            transition(new Transitor(otop, screen, trans) {
                protected void onComplete () {
                    hide(otop);
                    wasRemoved(otop);
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
     *
     * @return true if the screen was found in the stack and removed, false if the screen was not
     * in the stack.
     */
    public boolean remove (Screen screen, Transition trans) {
        if (top() != screen) return justRemove(screen);

        if (_screens.size() > 1) {
            final Screen otop = _screens.remove(0);
            transition(new Untransitor(otop, top(), trans) {
                protected void onComplete () {
                    hide(otop);
                    wasRemoved(otop);
                }
            });
        } else {
            hide(screen);
            justRemove(screen);
        }
        return true;
    }

    /**
     * {@link #remove(Predicte,Transition)} with the default transition.
     */
    public void remove (Predicate pred) {
        remove(pred, defaultPopTransition());
    }

    /**
     * Removes all screens that match the supplied predicate, from lowest in the stack to highest.
     * If the top screen is removed (as the last action), the supplied transition will be used.
     */
    public void remove (Predicate pred, Transition trans) {
        // first, remove any non-top screens that match the predicate
        if (_screens.size() > 1) {
            Iterator<Screen> iter = _screens.iterator();
            iter.next(); // skip top
            while (iter.hasNext()) {
                Screen screen = iter.next();
                if (pred.apply(screen)) {
                    iter.remove();
                    justRemove(screen);
                }
            }
        }
        // last, remove the top screen if it matches the predicate
        if (_screens.size() > 0 && pred.apply(top())) remove(top(), trans);
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
        justShow(screen);
    }

    protected void justShow (Screen screen) {
        graphics().rootLayer().add(screen.layer);
        try { screen.wasShown(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected void hide (Screen screen) {
        graphics().rootLayer().remove(screen.layer);
        try { screen.wasHidden(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected boolean justRemove (Screen screen) {
        boolean removed = _screens.remove(screen);
        if (removed) wasRemoved(screen);
        return removed;
    }

    protected void wasRemoved(Screen screen) {
        try { screen.wasRemoved(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected void transition (Transitor transitor) {
        if (_transitor != null) _transitor.complete();
        _transitor = transitor;
        _transitor.init();
    }

    protected class Transitor {
        public Transitor (Screen oscreen, Screen nscreen, Transition trans) {
            _oscreen = oscreen;
            _nscreen = nscreen;
            _trans = trans;
        }

        public void init () {
            _trans.init(_oscreen, _nscreen);
            // disable pointer interactions while we transition; allowing interaction
            // PlayN.pointer().setEnabled(false);
            _oscreen.hideTransitionStarted();
            showNewScreen();
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
            // let the transition know that it's complete
            _trans.complete(_oscreen, _nscreen);
            // make sure the new screen is in the right position
            _nscreen.layer.setTranslation(originX, originY);
            _nscreen.showTransitionCompleted();
            // reenable pointer interactions
            // PlayN.pointer().setEnabled(true);
            onComplete();
        }

        protected void showNewScreen () {
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

        @Override protected void showNewScreen () {
            justShow(_nscreen);
        }
    }

    /** Called if any exceptions are thrown by the screen callback functions. */
    protected abstract void handleError (RuntimeException error);

    /** The currently executing transition, or null. */
    protected Transitor _transitor;

    /** Containts the stacked screens from top-most, to bottom-most. */
    protected final List<Screen> _screens = new ArrayList<Screen>();
}
