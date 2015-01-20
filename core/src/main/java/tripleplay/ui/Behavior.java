//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.IDimension;
import pythagoras.f.Point;

import react.Closeable;
import react.Signal;
import react.Slot;
import react.Value;

import playn.core.Clock;
import playn.core.Sound;
import playn.scene.Pointer;

/**
 * Controls the behavior of a widget (how it responds to pointer events).
 */
public abstract class Behavior<T extends Element<T>> extends Pointer.Listener {

    /** Implements button-like behavior: selects the element when the pointer is in bounds, and
     * deselects on release. This is a pretty common case and inherited by {@link Click}. */
    public static class Select<T extends Element<T>> extends Behavior<T> {
        public Select (T owner) {
            super(owner);
        }

        @Override public void onPress (Pointer.Interaction iact) {
            updateSelected(true);
        }

        @Override public void onHover (Pointer.Interaction iact, boolean inBounds) {
            updateSelected(inBounds);
        }

        @Override public boolean onRelease (Pointer.Interaction iact) {
            // it's a click if we ended in bounds
            return updateSelected(false);
        }

        @Override public void onCancel (Pointer.Interaction iact) {
            updateSelected(false);
        }

        @Override public void onClick (Pointer.Interaction iact) {
            // nothing by default, subclasses wire this up as needed
        }
    }

    /** A behavior that ignores everything. This allows subclasses to easily implement a single
     * {@code onX} method. */
    public static class Ignore<T extends Element<T>> extends Behavior<T> {
        public Ignore (T owner) { super(owner); }
        @Override public void onPress (Pointer.Interaction iact) {}
        @Override public void onHover (Pointer.Interaction iact, boolean inBounds) {}
        @Override public boolean onRelease (Pointer.Interaction iact) { return false; }
        @Override public void onCancel (Pointer.Interaction iact) {}
        @Override public void onClick (Pointer.Interaction iact) {}
    }

    /** Implements clicking behavior. */
    public static class Click<T extends Element<T>> extends Select<T> {
        /** A delay (in milliseconds) during which the owner will remain unclickable after it has
         * been clicked. This ensures that users don't hammer away at a widget, triggering
         * multiple responses (which code rarely protects against). Inherited. */
        public static Style<Integer> DEBOUNCE_DELAY = Style.newStyle(true, 500);

        /** A signal emitted with our owner when clicked. */
        public Signal<T> clicked = Signal.create();

        public Click (T owner) {
            super(owner);
        }

        /** Triggers a click. */
        public void click () {
            soundAction();
            clicked.emit(_owner); // emit a click event
        }

        @Override public void layout () {
            super.layout();
            _debounceDelay = resolveStyle(DEBOUNCE_DELAY);
        }

        @Override public void onPress (Pointer.Interaction iact) {
            // ignore press events if we're still in our debounce interval
            if (iact.event.time - _lastClickStamp > _debounceDelay) super.onPress(iact);
        }

        @Override public void onClick (Pointer.Interaction iact) {
            _lastClickStamp = iact.event.time;
            click();
        }

        protected int _debounceDelay;
        protected double _lastClickStamp;
    }

    /** Implements toggling behavior. */
    public static class Toggle<T extends Element<T>> extends Behavior<T> {
        /** A signal emitted with our owner when clicked. */
        public final Signal<T> clicked = Signal.create();

        /** Indicates whether our owner is selected. It may be listened to, and updated. */
        public final Value<Boolean> selected = Value.create(false);

        public Toggle (T owner) {
            super(owner);
            selected.connect(selectedDidChange());
        }

        /** Triggers a click. */
        public void click () {
            soundAction();
            clicked.emit(_owner); // emit a click event
        }

        @Override public void onPress (Pointer.Interaction iact) {
            _anchorState = _owner.isSelected();
            selected.update(!_anchorState);
        }
        @Override public void onHover (Pointer.Interaction iact, boolean inBounds) {
            selected.update(inBounds ? !_anchorState : _anchorState);
        }
        @Override public boolean onRelease (Pointer.Interaction iact) {
            return _anchorState != _owner.isSelected();
        }
        @Override public void onCancel (Pointer.Interaction iact) {
            selected.update(_anchorState);
        }
        @Override public void onClick (Pointer.Interaction iact) {
            click();
        }

        protected boolean _anchorState;
    }

    /**
     * Tracks the pressed position as an anchor and delegates to subclasses to update state based
     * on anchor and drag position.
     */
    public static abstract class Track<T extends Element<T>> extends Ignore<T>
    {
        /** A distance, in event coordinates, used to decide if tracking should be temporarily
         * cancelled. If the pointer is hovered more than this distance outside of the owner's
         * bounds, the tracking will revert to the anchor position, just like when the pointer is
         * cancelled. A null value indicates that the tracking will be unconfined in this way.
         * TODO: default to 35 if no Slider uses are relying on lack of hover limit. */
        public static Style<Float> HOVER_LIMIT = Style.newStyle(true, (Float)null);

        /** Holds the necessary data for the currently active press. {@code Track} subclasses can
         * derive if more transient information is needed. */
        public class State {
            /** Time the press started. */
            public final double pressTime;

            /** The press and drag positions. */
            public final Point press, drag;

            /** How far the pointer strayed from the starting point, squared. */
            public float maxDistanceSq;

            /** Creates a new tracking state with the given starting press event. */
            public State (Pointer.Interaction iact) {
                pressTime = iact.event.time;
                toPoint(iact, press = new Point());
                drag = new Point(press);
            }

            /** Updates the state to the current event value and called {@link Track#onTrack()}. */
            public void update (Pointer.Interaction iact) {
                boolean cancel = false;
                toPoint(iact, drag);
                if (_hoverLimit != null) {
                    float lim = _hoverLimit;
                    IDimension size = _owner.size();
                    cancel = drag.x + lim < 0 || drag.y + lim < 0 ||
                            drag.x - lim >= size.width() || drag.y - lim >= size.height();
                }
                maxDistanceSq = Math.max(maxDistanceSq, press.distanceSq(drag));
                onTrack(press, cancel ? press : drag);
            }
        }

        protected Track (T owner) {
            super(owner);
        }

        /**
         * Called when the pointer is dragged. After cancel or if the pointer goes outside the
         * hover limit, drag will be equal to anchor.
         * @param anchor the pointer position when initially pressed
         * @param drag the current pointer position
         */
        abstract protected void onTrack (Point anchor, Point drag);

        /**
         * Creates the state instance for the given press. Subclasses may return an instance
         * of a derived {@code State} if more information is needed during tracking.
         */
        protected State createState (Pointer.Interaction press) {
            return new State(press);
        }

        /**
         * Converts an event to coordinates consumed by {@link #onTrack(Point, Point)}. By
         * default, simply uses the local x, y.
         */
        protected void toPoint (Pointer.Interaction iact, Point dest) {
            dest.set(iact.local.x, iact.local.y);
        }

        @Override public void onPress (Pointer.Interaction iact) {
            _state = createState(iact);
        }

        @Override public void onHover (Pointer.Interaction iact, boolean inBounds) {
            if (_state != null) _state.update(iact);
        }

        @Override public boolean onRelease (Pointer.Interaction iact) {
            _state = null;
            return false;
        }

        @Override public void onCancel (Pointer.Interaction iact) {
            // track to the press position to cancel
            if (_state != null) onTrack(_state.press, _state.press);
            _state = null;
        }

        @Override public void layout () {
            super.layout();
            _hoverLimit = resolveStyle(HOVER_LIMIT);
        }

        protected State _state;
        protected Float _hoverLimit;
    }

    /** A click behavior that captures the pointer and optionally issues clicks based on some time
      * based function. */
    public static abstract class Capturing<T extends Element<T>> extends Click<T>
    {
        protected Capturing (T owner) {
            super(owner);
        }

        @Override public void onPress (Pointer.Interaction iact) {
            super.onPress(iact);
            iact.capture();
            _conn = _owner.root().iface.frame.connect(new Slot<Clock>() {
                public void onEmit (Clock clock) { update(clock); }
            });
        }

        @Override public boolean onRelease (Pointer.Interaction iact) {
            super.onRelease(iact);
            cancel();
            return false;
        }

        @Override public void onCancel (Pointer.Interaction iact) {
            super.onCancel(iact);
            cancel();
        }

        /** Called on every frame while this behavior is active. */
        protected abstract void update (Clock clock);

        /** Cancels this time-based behavior. Called automatically on release and cancel events. */
        protected void cancel () {
            _conn = Closeable.Util.close(_conn);
        }

        protected Closeable _conn = Closeable.Util.NOOP;
    }

    /** Captures the pointer and dispatches one click on press, a second after an initial delay
     * and at regular intervals after that. */
    public static class RapidFire<T extends Element<T>> extends Capturing<T>
    {
        /** Milliseconds after the first click that the second click is dispatched. */
        public static final Style<Integer> INITIAL_DELAY = Style.newStyle(true, 200);

        /** Milliseconds between repeated click dispatches. */
        public static final Style<Integer> REPEAT_DELAY = Style.newStyle(true, 75);

        /** Creates a new rapid fire behavior for the given owner. */
        public RapidFire (T owner) {
            super(owner);
        }

        @Override public void onPress (Pointer.Interaction iact) {
            super.onPress(iact);
            _timeInBounds = 0;
            click();
        }

        @Override public void onHover (Pointer.Interaction iact, boolean inBounds) {
            super.onHover(iact, inBounds);
            if (!inBounds) _timeInBounds = -1;
            else if (_timeInBounds < 0) {
                _timeInBounds = 0;
                click();
            }
        }

        @Override protected void update (Clock clock) {
            if (_timeInBounds < 0) return;
            int was = _timeInBounds;
            _timeInBounds += clock.dt;
            int limit = was < _initDelay ? _initDelay :
                _initDelay + _repDelay * ((was - _initDelay) / _repDelay + 1);
            if (was < limit && _timeInBounds >= limit) click();
        }

        @Override public void layout () {
            super.layout();
            _initDelay = _owner.resolveStyle(INITIAL_DELAY);
            _repDelay = _owner.resolveStyle(REPEAT_DELAY);
        }

        protected int _initDelay, _repDelay, _timeInBounds;
    }

    public Behavior (T owner) {
        _owner = owner;
    }

    @Override public void onStart (Pointer.Interaction iact) {
        if (_owner.isEnabled()) onPress(iact);
    }

    @Override public void onDrag (Pointer.Interaction iact) {
        if (_owner.isEnabled()) onHover(iact, _owner.contains(iact.local.x, iact.local.y));
    }

    @Override public void onEnd (Pointer.Interaction iact) {
        if (onRelease(iact)) onClick(iact);
    }

    /** Called when our owner is laid out. If the behavior needs to resolve configuration via
     * styles, this is where it should do it. */
    public void layout () {
        _actionSound = resolveStyle(Style.ACTION_SOUND);
    }

    /** Emits the action sound for our owner, if one is configured. */
    public void soundAction () {
        if (_actionSound != null) _actionSound.play();
    }

    /** Called when the pointer is pressed down on our element. */
    public abstract void onPress (Pointer.Interaction iact);

    /** Called as the user drags the pointer around after pressing. Derived classes map this onto
     * the widget state, such as updating selectedness. */
    public abstract void onHover (Pointer.Interaction iact, boolean inBounds);

    /** Called when the pointer is released after having been pressed on this widget. This should
     * return true if the gesture is considered a click, in which case {@link #onClick} will
     * be called automatically. */
    public abstract boolean onRelease (Pointer.Interaction iact);

    /** Called when the pointer is released and the subclass decides that it is a click, i.e.
     * returns true from {@link #onRelease(Pointer.Event)}. */
    public abstract void onClick (Pointer.Interaction iact);

    /** Resolves the value for the supplied style via our owner. */
    protected <V> V resolveStyle (Style<V> style) {
        return Styles.resolveStyle(_owner, style);
    }

    /** Returns the {@link Root} to which our owning element is added, or null. */
    protected Root root () {
        return _owner.root();
    }

    /** Updates the selected state of our owner, invalidating if selectedness changes.
     * @return true if the owner was selected on entry. */
    protected boolean updateSelected (boolean selected) {
        boolean wasSelected = _owner.isSelected();
        if (selected != wasSelected) {
            _owner.set(Element.Flag.SELECTED, selected);
            _owner.invalidate();
        }
        return wasSelected;
    }

    /** Slot for calling {@link #updateSelected(boolean)}. */
    protected Slot<Boolean> selectedDidChange () {
        return new Slot<Boolean>() {
            @Override public void onEmit (Boolean selected) {
                updateSelected(selected);
            }
        };
    }

    protected final T _owner;
    protected Sound _actionSound;
}
