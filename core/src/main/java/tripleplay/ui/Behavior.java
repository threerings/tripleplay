//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Pointer;
import playn.core.Pointer.Event;
import playn.core.Sound;
import pythagoras.f.IDimension;
import pythagoras.f.Point;

import react.Signal;
import react.Slot;
import react.Value;

/**
 * Controls the behavior of a widget (how it responds to pointer events).
 */
public abstract class Behavior<T extends Element<T>> implements Pointer.Listener {
    /** Implements button-like behavior: selects the element when the pointer is in bounds, and
     * deselects on release. This is a pretty common case and inherited by {@link Click}. */
    public static class Select<T extends Element<T>> extends Behavior<T> {
        public Select (T owner) {
            super(owner);
        }

        @Override protected void onPress (Pointer.Event event) {
            updateSelected(true);
        }

        @Override protected void onHover (Pointer.Event event, boolean inBounds) {
            updateSelected(inBounds);
        }

        @Override protected boolean onRelease (Pointer.Event event) {
            // it's a click if we ended in bounds
            return updateSelected(false);
        }

        @Override protected void onCancel (Pointer.Event event) {
            updateSelected(false);
        }

        @Override protected void onClick (Pointer.Event event) {
            // nothing by default, subclasses wire this up as needed
        }
    }

    /** A behavior that ignores everything. This allows subclasses to easily implement a single
     * {@code onX} method. */
    public static class Ignore<T extends Element<T>> extends Behavior<T> {
        public Ignore (T owner) { super(owner); }
        @Override protected void onPress (Pointer.Event event) {}
        @Override protected void onHover (Pointer.Event event, boolean inBounds) {}
        @Override protected boolean onRelease (Pointer.Event event) { return false; }
        @Override protected void onCancel (Pointer.Event event) {}
        @Override protected void onClick (Pointer.Event event) {}
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

        @Override protected void onPress (Pointer.Event event) {
            // ignore press events if we're still in our debounce interval
            if (event.time() - _lastClickStamp > _debounceDelay) super.onPress(event);
        }

        @Override protected void onClick (Pointer.Event event) {
            _lastClickStamp = event.time();
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

        @Override protected void onPress (Pointer.Event event) {
            _anchorState = _owner.isSelected();
            selected.update(!_anchorState);
        }
        @Override protected void onHover (Pointer.Event event, boolean inBounds) {
            selected.update(inBounds ? !_anchorState : _anchorState);
        }
        @Override protected boolean onRelease (Pointer.Event event) {
            return _anchorState != _owner.isSelected();
        }
        @Override protected void onCancel (Pointer.Event event) {
            selected.update(_anchorState);
        }
        @Override protected void onClick (Pointer.Event event) {
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

            /** Creates a new tracking state with the given starting press event. */
            public State (Pointer.Event event) {
                pressTime = event.time();
                toPoint(event, press = new Point());
                drag = new Point(press);
            }
            /** Updates the state to the current event value and called {@link Track#onTrack()}. */
            public void update (Pointer.Event event) {
                boolean cancel = false;
                if (_hoverLimit != null) {
                    float lim = _hoverLimit, lx = event.localX(), ly = event.localY();
                    IDimension size = _owner.size();
                    cancel = lx + lim < 0 || ly + lim < 0 ||
                             lx - lim >= size.width() || ly - lim >= size.height();
                }
                toPoint(event, drag);
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
        protected State createState (Pointer.Event press) {
            return new State(press);
        }

        /**
         * Converts an event to coordinates consumed by {@link #onTrack(Point, Point)}. By
         * default, simply uses the local x, y.
         */
        protected void toPoint (Pointer.Event event, Point dest) {
            dest.set(event.localX(), event.localY());
        }

        @Override protected void onPress (Event event) {
            _state = createState(event);
        }

        @Override protected void onHover (Event event, boolean inBounds) {
            if (_state != null) _state.update(event);
        }

        @Override protected boolean onRelease (Event event) {
            _state = null;
            return false;
        }

        @Override protected void onCancel (Event event) {
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
        implements Interface.Task
    {
        protected Capturing (T owner) {
            super(owner);
        }

        @Override protected void onPress (Event event) {
            super.onPress(event);
            event.capture();
            _task = _owner.root().iface().addTask(this);
        }

        @Override protected void onCancel (Event event) {
            super.onCancel(event);
            cancelTask();
        }

        @Override protected boolean onRelease (Event event) {
            super.onRelease(event);
            cancelTask();
            return false;
        }

        /** Cancels the time-based task. This is called automatically by the pointer release
         * and cancel events. */
        protected void cancelTask () {
            if (_task == null) return;
            _task.remove();
            _task = null;
        }

        protected Interface.TaskHandle _task;
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

        @Override protected void onPress (Event event) {
            super.onPress(event);
            _timeInBounds = 0;
            click();
        }

        @Override protected void onHover (Event event, boolean inBounds) {
            super.onHover(event, inBounds);
            if (!inBounds) _timeInBounds = -1;
        }

        @Override public void update (int delta) {
            if (_timeInBounds < 0) return;
            int was = _timeInBounds;
            _timeInBounds += delta;
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

    @Override public void onPointerStart (Pointer.Event event) {
        if (_owner.isEnabled()) onPress(event);
    }

    @Override public void onPointerDrag (Pointer.Event event) {
        if (_owner.isEnabled()) onHover(event, _owner.contains(event.localX(), event.localY()));
    }

    @Override public void onPointerEnd (Pointer.Event event) {
        if (onRelease(event)) onClick(event);
    }

    @Override public void onPointerCancel (Pointer.Event event) {
        onCancel(event);
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

    /** Resolves the value for the supplied style via our owner. */
    protected <V> V resolveStyle (Style<V> style) {
        return Styles.resolveStyle(_owner, style);
    }

    /** Returns the {@link Root} to which our owning element is added, or null. */
    protected Root root () {
        return _owner.root();
    }

    /** Called when the pointer is pressed down on our element. */
    protected abstract void onPress (Pointer.Event event);

    /** Called as the user drags the pointer around after pressing. Derived classes map this onto
     * the widget state, such as updating selectedness. */
    protected abstract void onHover (Pointer.Event event, boolean inBounds);

    /** Called when the pointer is released after having been pressed on this widget. This should
     * return true if the gesture is considered a click, in which case {@link #onClick} will
     * be called automatically. */
    protected abstract boolean onRelease (Pointer.Event event);

    /** Called when the interaction is canceled after having been pressed on this widget. This
     * should not result in a call to {@link #onClick}. */
    protected abstract void onCancel (Pointer.Event event);

    /** Called when the pointer is released and the subclass decides that it is a click, i.e.
     * returns true from {@link #onRelease(Pointer.Event)}. */
    protected abstract void onClick (Pointer.Event event);

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
