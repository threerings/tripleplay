//
// Verbum - words, fonts, and tasteful shadows
// Copyright © 2014-2015 Three Rings Design, Inc.

package tripleplay.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import react.UnitSignal;
import react.UnitSlot;

import playn.core.GroupLayer;
import playn.core.Pointer;
import playn.core.util.Clock;
import static playn.core.PlayN.*;

import tripleplay.ui.Interface;
import tripleplay.ui.Root;
import tripleplay.util.Interpolator;

/**
 * Maintains a 2D layout of {@link Screen}s. New screens can be introduced in a direction, and the
 * view is scrolled in that direction to focus on the new screen. The user can then slide the view
 * back toward the previous screen (in the opposite direction that it was introduced). If they
 * release their slide with the old screen sufficiently visible, it will be restored to focus.
 */
public class ScreenSpace implements Iterable<ScreenSpace.Screen>
{
    /** The directions in which a new screen can be added. */
    public static enum Dir {
        UP {
            public int vertComp () { return -1; }
            public void update (Screen oscreen, Screen nscreen, float pct) {
                float oheight = oscreen.height();
                float ostart = 0, nstart = oheight, range = -oheight;
                float offset = pct * range;
                oscreen.layer.setTy(ostart + offset);
                nscreen.layer.setTy(nstart + offset);
            }
        },
        DOWN {
            public int vertComp () { return 1; }
            public void update (Screen oscreen, Screen nscreen, float pct) {
                float nheight = nscreen.height();
                float ostart = 0, nstart = -nheight, range = nheight;
                float offset = pct * range;
                oscreen.layer.setTy(ostart + offset);
                nscreen.layer.setTy(nstart + offset);
            }
        },
        LEFT {
            public int horizComp () { return -1; }
            public void update (Screen oscreen, Screen nscreen, float pct) {
                float owidth = oscreen.width();
                float ostart = 0, nstart = owidth, range = -owidth;
                float offset = pct * range;
                oscreen.layer.setTx(ostart + offset);
                nscreen.layer.setTx(nstart + offset);
            }
        },
        RIGHT {
            public int horizComp () { return 1; }
            public void update (Screen oscreen, Screen nscreen, float pct) {
                float nwidth = nscreen.width();
                float ostart = 0, nstart = -nwidth, range = nwidth;
                float offset = pct * range;
                oscreen.layer.setTx(ostart + offset);
                nscreen.layer.setTx(nstart + offset);
            }
        },
        IN {
            public void update (Screen oscreen, Screen nscreen, float pct) {
                oscreen.layer.setAlpha(1-pct);
                nscreen.layer.setAlpha(pct);
                // TODO: scaling
            }
            public void finish (Screen oscreen, Screen nscreen) {
                super.finish(oscreen, nscreen);
                oscreen.layer.setAlpha(1);
            }
        },
        OUT {
            public void update (Screen oscreen, Screen nscreen, float pct) {
                oscreen.layer.setAlpha(1-pct);
                nscreen.layer.setAlpha(pct);
                // TODO: scaling
            }
            public void finish (Screen oscreen, Screen nscreen) {
                super.finish(oscreen, nscreen);
                oscreen.layer.setAlpha(1);
            }
        },
        FLIP {
            public void update (Screen oscreen, Screen nscreen, float pct) {
                // TODO
            }
        };

        /** Returns the horizontal motion of this direction: 1, 0 or -1. */
        public int horizComp () { return 0; }

        /** Returns the vertical motion of this direction: 1, 0 or -1. */
        public int vertComp () { return 0; }

        /** Returns whether this direction can be manually "untransitioned". */
        public boolean canUntrans () {
            return horizComp() != 0 || vertComp() != 0;
        }

        /** Prepares {@code oscreen} and {@code nscreen} to be transitioned. {@code oscreen} is the
          * currently visible screen and {@code nscreen} is the screen transitioning into view. */
        public void init (Screen oscreen, Screen nscreen) {
            oscreen.setTransiting(true);
            nscreen.setTransiting(true);
        }

        /** Updates the position of {@code oscreen} and {@code nscreen} based on {@code pct}.
          * @param pct a value ranged {@code [0,1]} indicating degree of completeness. */
        public abstract void update (Screen oscreen, Screen nscreen, float pct);

        /** Cleans up after a transition. {@link update} will have been called with {@code pct}
          * equal to one immediately prior to this call, so this method is only needed when actual
          * cleanup is needed, like the removal of custom shaders, etc.
          *
          * <p>Note also that the old screen's layer will have been made non-visible prior to this
          * call. This call should not restore that visibility. */
        public void finish (Screen oscreen, Screen nscreen) {
            oscreen.setTransiting(false);
            nscreen.setTransiting(false);
        }
    };

    /**
     * A screen that integrates with {@code ScreenSpace}. The screen lifecycle is:
     * {@code init [wake gainedFocus lostFocus sleep]+ destroy}.
     *
     * <p>When the screen has the potential to become visible (due to the user scrolling part of
     * the screen into view) it will have been wakened. If the user selects the screen, it will be
     * animated into position and then {@code gainedFocus} will be called. If the user scrolls a
     * new screen into view, {@code lostFocus} will be called, the screen will be animated away. If
     * the screen is no longer "at risk" of being shown, {@code sleep} will be called. When the
     * screen is finally removed from the screen space, {@code destroy} will be called.
     */
    public static class Screen {

        /** Contains the scene graph root for this screen. */
        public final GroupLayer layer = graphics().createGroupLayer();

        /** Called when this screen is first added to the screen space. */
        public void init () {
            // nada by default
        }

        /** Returns the width of this screen, for use by transitions.
          * Defaults to the width of the entire view. */
        public float width () { return graphics().width(); }
        /** Returns the height of this screen, for use by transitions.
          * Defaults to the height of the entire view. */
        public float height () { return graphics().height(); }

        /** Returns true when this screen is awake. */
        public boolean awake () { return (_flags & AWAKE) != 0; }
        /** Returns true when this screen is in-transition. */
        public boolean transiting () { return (_flags & TRANSITING) != 0; }

        /** Called when this screen will potentially be shown.
          * Should create main UI and prepare it for display. */
        public void wake () {
            _flags |= AWAKE;
        }

        /** Called when this screen has become the active screen. */
        public void gainedFocus () {
            assert awake();
        }

        /** Called when some other screen is about to become the active screen. This screen will be
          * animated out of view. This may not be immediately followed by a call to {@link #sleep}
          * because the screen may remain visible due to incidental scrolling by the user. Only
          * when the screen is separated from the focus screen by at least one screen will it be
          * put to sleep. */
        public void lostFocus () {
        }

        /** Called when this screen is no longer at risk of being seen by the user. This should
          * destroy the UI and minimize the screen's memory footprint as much as possible. */
        public void sleep () {
            _flags &= ~AWAKE;
        }

        /** Called when this screen is removed from the screen space. This will always be preceded
          * by a call to {@link #sleep}, but if there are any resources that the screen retains
          * until it is completely released, this is the place to remove them. */
        public void destroy () {
            assert !awake();
        }

        /** called once per game update to allow this screen to participate in the game loop. */
        public void update (int delta) {
        }

        /** Called once per game paint to allow this screen to participate in the game loop. */
        public void paint (Clock clock) {
        }

        /** Returns whether or not an untransition gesture may be initiated via {@code dir}.
          *
          * <p>By default this requires that the screen be at its origin in x or y depending on the
          * orientation of {@code dir}. If a screen uses a {@code Flicker} to scroll vertically,
          * this will automatically do the right thing. If there are other circumstances in which a
          * screen wishes to prevent the user from initiating an untransition gesture, this is the
          * place to put 'em.
          */
        public boolean canUntrans (Dir dir) {
            if (dir.horizComp() != 0) return layer.tx() == 0;
            if (dir.vertComp() != 0) return layer.ty() == 0;
            return true;
        }

        void setTransiting (boolean transiting) {
            if (transiting) _flags |= TRANSITING;
            else            _flags &= ~TRANSITING;
        }

        /** Flag: whether this screen is currently awake. */
        protected static final int AWAKE = 1 << 0;
        /** Flag: whether this screen is currently transitioning. */
        protected static final int TRANSITING = 1 << 1;

        protected int _flags;
    }

    /** A {@link Screen} that takes care of basic UI setup for you. */
    public static abstract class UIScreen extends Screen {

        /** Manages the main UI elements for this screen. */
        public final Interface iface = new Interface();

        @Override public void wake () {
            super.wake();
            _root = iface.addRoot(createRoot());
            layer.add(_root.layer);
        }

        @Override public void sleep () {
            super.sleep();
            if (_root != null) {
                iface.destroyRoot(_root);
                _root = null;
            }
            // a screen is completely cleared and recreated between sleep/wake calls, so clear the
            // animator after destroying the root so that unprocessed anims don't hold onto memory
            iface.animator().clear();
        }

        @Override public void update (int delta) {
            super.update(delta);
            iface.update(delta);
        }

        @Override public void paint (Clock clock) {
            super.paint(clock);
            iface.paint(clock);
        }

        /** Creates the main UI root for this screen. This should also configure the size of the
          * root prior to returning it. */
        protected abstract Root createRoot ();

        /** Contains the main UI for this screen.
          * Created in {@link #wake}, destroyed in {@link #sleep}. */
        protected Root _root;
    }

    @Override public Iterator<Screen> iterator () {
        return new Iterator<Screen>() {
            private int _idx = 0;
            public boolean hasNext () { return _idx < screenCount(); }
            public Screen next () { return screen(_idx++); }
            public void remove () { throw new UnsupportedOperationException(); }
        };
    }

    /** Returns the number of screens in the space. */
    public int screenCount () {
        return _screens.size();
    }

    /** Returns the screen at {@code index}. */
    public Screen screen (int index) {
        return _screens.get(index).screen;
    }

    /** Returns true if we're transitioning between two screens at this instant. This may either be
      * an animation driven transition, or a manual transition in progress due to a user drag. */
    public boolean isTransiting () {
        return _driver != null || _untrans != null;
    }

    /** Adds {@code screen} to this space, positioned {@code dir}-wise from the current screen. For
      * example, using {@code RIGHT} will add the screen to the right of the current screen and
      * will slide the view to the right to reveal the new screen. The user would then manually
      * slide the view left to return to the previous screen. */
    public void add (Screen screen, Dir dir) {
        add(screen, dir, false);
    }

    /** Adds {@code screen} to this space, replacing the current top-level screen. The screen is
      * animated in the same manner as {@link #addScreen} using the same direction in which the
      * current screen was added. This ensures that the user returns to the previous screen in the
      * same way that they would via the to-be-replaced screen. */
    public void replace (Screen screen) {
        if (_screens.isEmpty()) throw new IllegalStateException("No current screen to replace()");
        add(screen, _screens.get(0).dir, true);
    }

    /** Removes {@code screen} from this space. If it is the top-level screen, an animated
      * transition to the previous screen will be performed. Otherwise the screen will simply be
      * removed. */
    public void pop (Screen screen) {
        if (_current.screen == screen) {
            if (!_screens.isEmpty()) popTrans(0);
            else {
                ActiveScreen oscr = _screens.remove(0);
                takeFocus(oscr);
                oscr.destroy();
                _current = null;
                pointer().setListener(null);
            }
        } else {
            // TODO: this screen may be inside UntransListener.previous so we may need to recreate
            // UntransListener with a new previous screen; or maybe just don't support pulling
            // screens out of the middle of the stack; that's kind of wacky; popTop and popTo may
            // be enough
            int idx = indexOf(screen);
            if (idx >= 0) {
                popAt(idx);
                checkSleep(); // we may need to wake a new screen
            }
        }
    }

    /** Removes all screens from the space until {@code screen} is reached. No transitions will be
      * used, all screens will simply be removed and destroyed until we reach {@code screen}, and
      * that screen will be woken and positioned properly. */
    public void popTo (Screen screen) {
        if (current() == screen) return; // NOOP!
        ActiveScreen top = _screens.get(0);
        while (top.screen != screen) {
            takeFocus(top);
            top.destroy();
            top = _screens.get(0);
        }
        checkSleep(); // wake up the top screen
        top.screen.layer.setTranslation(0, 0); // ensure that it's positioned properly
        giveFocus(top);
    }

    /** Returns the current screen, or {@code null} if this space is empty. */
    public Screen current () {
        return (_current == null) ? null : _current.screen;
    }

    /** Includes the screen space and its screens in the game update loop. */
    public void update (int delta) {
        if (_driver != null) _driver.update(delta);
        else {
            if (_current != null) _current.screen.update(delta);
            if (_untrans != null) _untrans.screen.update(delta);
        }
    }

    /** Includes the screen space and its screens in the game paint loop. */
    public void paint (Clock clock) {
        if (_driver != null) _driver.paint(clock);
        else {
            if (_current != null) _current.screen.paint(clock);
            if (_untrans != null) _untrans.screen.paint(clock);
        }
    }

    protected int indexOf (Screen screen) {
        for (int ii = 0, ll = _screens.size(); ii < ll; ii++) {
            if (_screens.get(ii).screen == screen) return ii;
        }
        return -1;
    }

    protected void add (Screen screen, Dir dir, final boolean replace) {
        screen.init();
        ActiveScreen otop = _screens.isEmpty() ? null : _screens.get(0);
        final ActiveScreen ntop = new ActiveScreen(screen, dir);
        _screens.add(0, ntop);
        ntop.check(true); // wake up the to-be-added screen
        if (otop == null) giveFocus(ntop);
        else transition(otop, ntop, ntop.dir, 0).onComplete.connect(new UnitSlot() {
            public void onEmit () {
                giveFocus(ntop);
                if (replace) popAt(1);
            }
        });
    }

    protected float transitionTime (Dir dir) {
        return 500f;
    }

    protected Driver transition (ActiveScreen oscr, ActiveScreen nscr, Dir dir, float startPct) {
        takeFocus(oscr);
        return _driver = new Driver(oscr, nscr, dir, startPct);
    }

    protected void checkSleep () {
        if (_screens.isEmpty()) return;
        // if the top-level screen was introduced via a slide transition, we need to keep the
        // previous screen awake because we could start sliding to it ay any time; otherwise we can
        // put that screen to sleep; all other screens should be sleeping
        int ss = _screens.get(0).dir.canUntrans() ? 2 : 1;
        log().info("checkSleep " + ss);
        for (int ii = 0, ll = _screens.size(); ii < ll; ii++) _screens.get(ii).check(ii < ss);
    }

    protected void popTrans (float startPct) {
        final ActiveScreen oscr = _screens.remove(0);
        Dir dir = reverse(oscr.dir);
        final ActiveScreen nscr = _screens.get(0);
        nscr.check(true); // wake screen, if necessary
        transition(oscr, nscr, dir, startPct).onComplete.connect(new UnitSlot() {
            public void onEmit () {
                giveFocus(nscr);
                oscr.destroy();
            }
        });
    }

    protected void popAt (int index) {
        _screens.remove(index).destroy();
    }

    protected void giveFocus (ActiveScreen as) {
        try {
            _current = as;
            as.screen.gainedFocus();

            // if we have a previous screen, and the direction supports manual untransitioning,
            // set up a listener to handle that
            ActiveScreen previous = (_screens.size() <= 1) ? null : _screens.get(1);
            if (previous != null && as.dir.canUntrans()) {
                pointer().setListener(new UntransListener(as, previous));
            } else {
                pointer().setListener(null);
            }

        } catch (Exception e) {
            log().warn("Screen choked in gainedFocus() [screen=" + as.screen + "]", e);
        }
        checkSleep();
    }

    protected void takeFocus (ActiveScreen as) {
        try {
            as.screen.lostFocus();
        } catch (Exception e) {
            log().warn("Screen choked in lostFocus() [screen=" + as.screen + "]", e);
        }
    }

    protected final List<ActiveScreen> _screens = new ArrayList<ActiveScreen>();
    protected ActiveScreen _current, _untrans;
    protected Driver _driver;

    protected class UntransListener implements Pointer.Listener {
        // the start stamp of our gesture, or 0 if we're not in a gesture
        public double start;
        // whether we're in the middle of an untransition gesture
        public boolean untransing;

        private float _sx, _sy;
        private final Dir _udir;
        private final ActiveScreen _cur, _prev;
        public UntransListener  (ActiveScreen cur, ActiveScreen prev) {
            _udir = reverse(cur.dir); // untrans dir is opposite of trans dir
            _cur = cur; _prev = prev;
        }

        @Override public void onPointerStart (Pointer.Event event) {
            // if it's not OK to initiate an untransition gesture, or we're already in the middle
            // of animating an automatic transition, ignore this gesture
            if (!_cur.screen.canUntrans(_udir) || _driver != null) return;
            _sx = event.x(); _sy = event.y();
            start = currentTime();
        }

        @Override public void onPointerDrag (Pointer.Event event) {
            if (start == 0) return; // ignore if we were disabled at gesture start

            float frac = updateFracs(event.x(), event.y());
            if (!untransing) {
                // if the beginning of the gesture is not "more" in the untrans direction than not,
                // ignore the rest the interaction (note: _offFrac is always positive but frac is
                // positive if it's in the untrans direction and negative otherwise)
                if (_offFrac > frac) {
                    start = 0;
                    return;
                }
                // TODO: we should probably ignore small movements in all directions before we
                // commit to this gesture or not; if someone always jerks their finger to the right
                // before beginning an interaction, that would always disable an up or down gesture
                // before it ever had a chance to get started... oh, humans

                // the first time we start untransing, do _udir.init() & setViz
                untransing = true;
                _untrans = _prev;
                _udir.init(_cur.screen, _prev.screen);
                _prev.screen.layer.setVisible(true);
                pointer().cancelLayerDrags();
            }

            _udir.update(_cur.screen, _prev.screen, frac);
        }

        @Override public void onPointerEnd (Pointer.Event event) {
            if (start == 0 || !untransing) return;

            // clean up after our current manual transition because we're going to set up a driver
            // to put the current screen back into place or to pop it off entirely
            _udir.finish(_cur.screen, _prev.screen);

            float frac = updateFracs(event.x(), event.y());
            // compute the "velocity" of this gesture in "screens per second"
            float fvel = (1000*frac) / (float)(currentTime() - start);
            // if we've revealed more than 30% of the old screen, or we're going fast enough...
            if (frac > 0.3f || fvel > 1.25f) {
                // ...pop back to the previous screen
                assert _cur == _screens.get(0);
                popTrans(frac);
            } else {
                // ...otherwise animate the current screen back into position
                _driver = new Driver(_prev, _cur, _cur.dir, 1-frac);
            }
            clear();
        }

        @Override public void onPointerCancel (Pointer.Event event) {
            if (start == 0 || !untransing) return;

            // snap our screens back to their original positions
            _udir.update(_cur.screen, _prev.screen, 0);
            _prev.screen.layer.setVisible(false);
            _udir.finish(_cur.screen, _prev.screen);
            clear();
        }

        protected void clear () {
            untransing = false;
            start = 0;
            _untrans = null;
        }

        protected float updateFracs (float cx, float cy) {
            // project dx/dy along untransition dir's vector
            float dx = cx-_sx, dy = cy-_sy;
            int hc = _udir.horizComp(), vc = _udir.vertComp();
            float swidth = _prev.screen.width(), sheight = _prev.screen.height();
            float frac;
            if (hc != 0) {
                frac = (dx*hc) / swidth;
                _offFrac = Math.abs(dy) / sheight;
            } else {
                frac = (dy*vc) / sheight;
                _offFrac = Math.abs(dx) / swidth;
            }
            return frac;
        }

        protected float computeFrac (float cx, float cy) {
            // project dx/dy along untransition dir's vector
            float dx = cx-_sx, dy = cy-_sy;
            float tx = dx*_udir.horizComp();
            float ty = dy*_udir.vertComp();
            // the distance we've traveled over the full width/height of the screen is the frac
            return (tx > 0) ? tx/_prev.screen.width() : ty/_prev.screen.height();
        }

        protected float _offFrac;
    }

    protected static class ActiveScreen {
        public final Screen screen;
        public final Dir dir;

        public ActiveScreen (Screen screen, Dir dir) {
            this.screen = screen;
            this.dir = dir;
        }

        public void check (boolean awake) {
            if (screen.awake() != awake) {
                if (awake) {
                    log().info("waking " + screen);
                    graphics().rootLayer().add(screen.layer);
                    screen.wake();
                } else {
                    log().info("sleeping " + screen);
                    graphics().rootLayer().remove(screen.layer);
                    screen.sleep();
                }
            }
        }

        public void destroy () {
            check(false); // make sure screen is hidden/remove
            screen.destroy();
        }
    }

    /** Drives a transition via an animation. */
    protected class Driver {
        public final ActiveScreen outgoing, incoming;
        public final Dir dir;
        public final float duration;
        public final UnitSignal onComplete = new UnitSignal();
        public final float startPct;
        public final Interpolator interp;
        public float elapsed;

        public Driver (ActiveScreen outgoing, ActiveScreen incoming, Dir dir, float startPct) {
            this.outgoing = outgoing;
            this.incoming = incoming;
            this.dir = dir;
            this.duration = transitionTime(dir);
            this.startPct = startPct;
            // TODO: allow Dir to provide own interpolator?
            this.interp = (startPct == 0) ? Interpolator.EASE_INOUT : Interpolator.EASE_OUT;

            dir.init(outgoing.screen, incoming.screen);
            incoming.screen.layer.setVisible(true);
            dir.update(outgoing.screen, incoming.screen, startPct);
        }

        public void update (int delta) {
            outgoing.screen.update(delta);
            incoming.screen.update(delta);
        }

        public void paint (Clock clock) {
            outgoing.screen.paint(clock);
            incoming.screen.paint(clock);
            // if this is our first frame, cap dt at 33ms because the first frame has to eat the
            // initialization time for the to-be-introduced screen, and we don't want that to chew
            // up a bunch of our transition time if it's slow; the user will not have seen
            // anything happen up to now, so this won't cause a jitter in the animation
            if (elapsed == 0) elapsed += Math.min(33, clock.dt());
            else elapsed += clock.dt();
            float pct = Math.min(elapsed/duration, 1), ipct;
            if (startPct >= 0) ipct = startPct + (1-startPct)*interp.apply(pct);
            // if we were started with a negative startPct, we're scrolling back from an
            // untranslation gesture that ended on the "opposite" side
            else ipct = 1-interp.apply(pct);
            dir.update(outgoing.screen, incoming.screen, ipct);
            if (pct == 1) complete();
        }

        public void complete () {
            _driver = null;
            outgoing.screen.layer.setVisible(false);
            dir.finish(outgoing.screen, incoming.screen);
            onComplete.emit();
        }
    }

    protected static Dir reverse (Dir dir) {
        switch (dir) {
        case UP: return Dir.DOWN;
        case DOWN: return Dir.UP;
        case LEFT: return Dir.RIGHT;
        case RIGHT: return Dir.LEFT;
        case IN: return Dir.OUT;
        case OUT: return Dir.IN;
        case FLIP: return Dir.FLIP;
        default: throw new AssertionError("Sky is falling: " + dir);
        }
    }
}
