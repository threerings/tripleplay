//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Pointer;
import playn.core.Sound;

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

    /** An empty behavior that ignores everything. This allows subclasses to easily implement
     * a single {@code onX} method. */
    public static class Empty<T extends Element<T>> extends Behavior<T> {
        public Empty (T owner) { super(owner); }
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
