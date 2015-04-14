//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import pythagoras.f.IDimension;

import react.Closeable;
import react.Signal;
import react.Slot;

import playn.core.Clock;
import playn.core.Game;
import playn.core.Platform;
import playn.scene.GroupLayer;

import tripleplay.ui.Interface;
import tripleplay.ui.Root;

import tripleplay.game.trans.FlipTransition;
import tripleplay.game.trans.PageTurnTransition;
import tripleplay.game.trans.SlideTransition;

import static tripleplay.game.Log.log;

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
public class ScreenStack {

    /** Displays and manages the lifecycle for a single game screen. */
    public static abstract class Screen {

        /** The layer on which all of this screen's UI must be placed. */
        public final GroupLayer layer = new GroupLayer();
        /** A signal emitted on every simulation update, while this screen is showing. */
        public final Signal<Clock> update = Signal.create();
        /** A signal emitted on every frame, while this screen is showing. */
        public final Signal<Clock> paint = Signal.create();

        // the following methods provide hooks into the visibility lifecycle of a screen, which
        // takes the form: added -> shown -> { hidden -> shown -> ... } -> hidden -> removed

        /** Returns a reference to the game in which this screen is operating. */
        public abstract Game game ();

        /** Returns the size of this screen. This is used for transitions.
          * Defaults to the size of the entire view. */
        public IDimension size () { return game().plat.graphics().viewSize; }

        /** Called when a screen is added to the screen stack for the first time. */
        public void wasAdded () {}

        /** Called when a screen becomes the top screen, and is therefore made visible. */
        public void wasShown () {
            closeOnHide(game().update.connect(update.slot()));
            closeOnHide(game().paint.connect(paint.slot()));
        }

        /** Called when a screen is no longer the top screen (having either been pushed down by
          * another screen, or popped off the stack). */
        public void wasHidden () {
            _closeOnHide.close();
        }

        /** Called when a screen has been removed from the stack. This will always be preceeded by
          * a call to {@link #wasHidden}, though not always immediately. */
        public void wasRemoved () {}

        /** Called when this screen's transition into view has completed. {@link #wasShown} is
          * called immediately before the transition begins, and this method is called when it
          * ends. */
        public void showTransitionCompleted () {}

        /** Called when this screen's transition out of view has started. {@link #wasHidden} is
          * called when the hide transition completes. */
        public void hideTransitionStarted () {}

        /** Adds {@code ac} to a set to be closed when this screen is hidden. */
        public void closeOnHide (AutoCloseable ac) {
            _closeOnHide.add(ac);
        }

        protected Closeable.Set _closeOnHide = new Closeable.Set();
    }

    /** A {@link Screen} that takes care of basic UI setup for you. */
    public static abstract class UIScreen extends Screen {

        /** Manages the main UI elements for this screen. */
        public final Interface iface = new Interface(game().plat, paint);

        @Override public void wasShown () {
            super.wasShown();
            _root = iface.addRoot(createRoot());
            layer.add(_root.layer);
        }

        @Override public void wasHidden () {
            super.wasHidden();
            if (_root != null) {
                iface.disposeRoot(_root);
                _root = null;
            }
            // a screen is completely cleared and recreated between sleep/wake calls, so clear the
            // animator after destroying the root so that unprocessed anims don't hold onto memory
            iface.anim.clear();
        }

        /** Creates the main UI root for this screen. This should also configure the size of the
          * root prior to returning it. */
        protected abstract Root createRoot ();

        /** Contains the main UI for this screen.
          * Created in {@link #wake}, destroyed in {@link #sleep}. */
        protected Root _root;
    }

    /** Implements a particular screen transition. */
    public static abstract class Transition {

        /** Direction constants, used by transitions. */
        public static enum Dir { UP, DOWN, LEFT, RIGHT; }

        /** Allows the transition to pre-compute useful values. This will immediately be followed
         * by call to {@link #update} with an elapsed time of zero. */
        public void init (Platform plat, Screen oscreen, Screen nscreen) {}

        /** Called every frame to update the transition
         * @param oscreen the outgoing screen.
         * @param nscreen the incoming screen.
         * @param elapsed the elapsed time since the transition started (in millis if that's what
         * your game is sending to {@link ScreenStack#update}).
         * @return false if the transition is not yet complete, true when it is complete.
         */
        public abstract boolean update (Screen oscreen, Screen nscreen, float elapsed);

        /** Called when the transition is complete. This is where the transition should clean up
         * any temporary bits and restore the screens to their original state. The stack will
         * automatically destroy/hide the old screen after calling this method. Also note that this
         * method may be called <em>before</em> the transition signals completion, if a new
         * transition is started and this transition needs be aborted. */
        public void complete (Screen oscreen, Screen nscreen) {}
    }

    /** Used to operate on screens. See {@link #remove(Predicate)}. */
    public interface Predicate {
        /** Returns true if the screen matches the predicate. */
        boolean apply (Screen screen);
    }

    /** Simply puts the new screen in place and removes the old screen. */
    public static final Transition NOOP = new Transition() {
        public boolean update (Screen oscreen, Screen nscreen, float elapsed) { return true; }
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
     * Creates a screen stack that manages screens for {@code game} on {@code rootLayer}.
     */
    public ScreenStack (Game game, GroupLayer rootLayer) {
        _game = game;
        _rootLayer = rootLayer;
    }

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
                @Override protected void onComplete() { hide(otop); }
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
                @Override protected void onComplete() { hide(otop); }
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
        // if the desired top screen is already the top screen, then NOOP
        if (top() == newTopScreen) return;
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
            // log.info("Removed " + otop + ", new top " + top());
            transition(new Transitor(otop, screen, trans) {
                @Override protected void onComplete () {
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
            // log.info("Removed " + otop + ", new top " + top());
            transition(new Untransitor(otop, top(), trans) {
                @Override protected void onComplete () {
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
     * {@link #remove(Predicate,Transition)} with the default transition.
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
                    wasRemoved(screen);
                    // log.info("Pred removed " + screen + ", new top " + top());
                }
            }
        }
        // last, remove the top screen if it matches the predicate
        if (_screens.size() > 0 && pred.apply(top())) remove(top(), trans);
    }

    /** Returns the top screen on the stack, or null if the stack contains no screens. */
    public Screen top () { return _screens.isEmpty() ? null : _screens.get(0); }

    /**
     * Searches from the top-most screen to the bottom-most screen for a screen that matches the
     * predicate, returning the first matching screen. {@code null} is returned if no matching
     * screen is found.
     */
    public Screen find (Predicate pred) {
        for (Screen screen : _screens) if (pred.apply(screen)) return screen;
        return null;
    }

    /** Returns true if we're currently transitioning between screens. */
    public boolean isTransiting () { return _transitor != null; }

    /** Returns the number of screens on the stack. */
    public int size () { return _screens.size(); }

    protected Transition defaultPushTransition () { return NOOP; }
    protected Transition defaultPopTransition () { return NOOP; }

    protected void add (Screen screen) {
        if (_screens.contains(screen)) {
            throw new IllegalArgumentException("Cannot add screen to stack twice.");
        }
        _screens.add(0, screen);
        // log.info("Added " + screen + ", new top " + top());
        try { screen.wasAdded(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected void addAndShow (Screen screen) {
        add(screen);
        justShow(screen);
    }

    protected void justShow (Screen screen) {
        _rootLayer.addAt(screen.layer, originX, originY);
        try { screen.wasShown(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected void hide (Screen screen) {
        _rootLayer.remove(screen.layer);
        try { screen.wasHidden(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected boolean justRemove (Screen screen) {
        boolean removed = _screens.remove(screen);
        if (removed) wasRemoved(screen);
        // log.info("Just removed " + screen + ", new top " + top());
        return removed;
    }

    protected void wasRemoved (Screen screen) {
        try { screen.wasRemoved(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected void transition (Transitor transitor) {
        if (_transitor != null) _transitor.complete();
        _transitor = transitor;
        _transitor.init();
    }

    /**
     * A hacky mechanism to allow a game to force a transition to skip some number of frames at its
     * start. If a game's screens tend to do a lot of image loading in wasAdded or immediately
     * after, that will cause an unpleasant jerk at the start of the transition as the first frame
     * or two have order of magnitude larger frame deltas than subsequent frames. Having those
     * render as t=0 and then starting the timer after the skipped frames are done delays the
     * transition by a bit, but ensures that when things are actually animating, that they are nice
     * and smooth.
     */
    protected int transSkipFrames () {
        return 0;
    }

    protected void setInputEnabled (boolean enabled) {
        _game.plat.input().mouseEnabled = enabled;
        _game.plat.input().touchEnabled = enabled;
    }

    protected class Transitor {
        public Transitor (Screen oscreen, Screen nscreen, Transition trans) {
            _oscreen = oscreen;
            _nscreen = nscreen;
            _trans = trans;
        }

        public void init () {
            _oscreen.hideTransitionStarted();
            showNewScreen();
            _trans.init(_game.plat, _oscreen, _nscreen);
            setInputEnabled(false);

            // force a complete if the transition is a noop, so that we don't have to wait until
            // the next update; perhaps we should check some property of the transition object
            // rather than compare to noop, in case we have a custom 0-duration transition
            if (_trans == NOOP) complete();
            else _onPaint = _game.paint.connect(new Slot<Clock>() {
                public void onEmit (Clock clock) { paint(clock); }
            });
        }

        public void paint (Clock clock) {
            if (_skipFrames > 0) _skipFrames -= 1;
            else {
                _elapsed += clock.dt;
                if (_trans.update(_oscreen, _nscreen, _elapsed)) complete();
            }
        }

        public void complete () {
            _transitor = null;
            _onPaint.close();
            setInputEnabled(true);
            // let the transition know that it's complete
            _trans.complete(_oscreen, _nscreen);
            // make sure the new screen is in the right position
            _nscreen.layer.setTranslation(originX, originY);
            _nscreen.showTransitionCompleted();
            onComplete();
        }

        protected void showNewScreen () {
            addAndShow(_nscreen);
        }

        protected void onComplete () {}

        protected final Screen _oscreen, _nscreen;
        protected final Transition _trans;
        protected Closeable _onPaint = Closeable.Util.NOOP;
        protected int _skipFrames = transSkipFrames();
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

    /** Called if any exceptions are thrown by the screen calldown functions. */
    protected void handleError (RuntimeException error) {
        log.warning("Screen choked", error);
    }

    protected final Game _game;
    protected final GroupLayer _rootLayer;

    /** The currently executing transition, or null. */
    protected Transitor _transitor;

    /** Containts the stacked screens from top-most, to bottom-most. */
    protected final List<Screen> _screens = new ArrayList<Screen>();
}
