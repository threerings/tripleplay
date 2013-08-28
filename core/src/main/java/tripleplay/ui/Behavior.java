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
public abstract class Behavior<T extends Widget<T>> implements Pointer.Listener {

    /** Implements clicking behavior. */
    public static class Click<T extends Widget<T>> extends Behavior<T> {
        /** A delay (in milliseconds) during which a widget will remain unclickable after it has been
         * clicked. This ensures that users don't hammer away at a widget, triggering multiple
         * responses (which code rarely protects against). Inherited. */
        public static Style<Integer> DEBOUNCE_DELAY = Style.newStyle(true, 500);

        /** A signal emitted with our owning widget when clicked. */
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
    public static class Toggle<T extends Widget<T>> extends Behavior<T> {
        /** A signal emitted with our owning widget when clicked. */
        public final Signal<T> clicked = Signal.create();

        /** Indicates whether our owner is selected. It may be listened to, and updated. */
        public final Value<Boolean> selected = Value.create(false);

        public Toggle (T owner) {
            super(owner);
            selected.connect(new Slot<Boolean>() {
                @Override public void onEmit (Boolean selected) {
                    updateSelected(selected);
                }
            });
        }

        /** Triggers a click. */
        public void click () {
            soundAction();
            clicked.emit(_owner); // emit a click event
        }

        @Override protected void onPress (Pointer.Event event) {
            // we explicitly don't call super here
            _anchorState = _owner.isSelected();
            selected.update(!_anchorState);
        }
        @Override protected void onHover (Pointer.Event event, boolean inBounds) {
            // we explicitly don't call super here
            selected.update(inBounds ? !_anchorState : _anchorState);
        }
        @Override protected void onRelease (Pointer.Event event) {
            // we explicitly don't call super here
            if (_anchorState != _owner.isSelected()) onClick(event);
        }
        @Override protected void onCancel (Pointer.Event event) {
            // we explicitly don't call super here
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
        onRelease(event);
    }

    @Override public void onPointerCancel (Pointer.Event event) {
        onCancel(event);
    }

    /** Called when our owning widget is laid out. If the behavior needs to resolve configuration
     * via styles, this is where it should do it. */
    public void layout () {
        _actionSound = resolveStyle(Style.ACTION_SOUND);
    }

    /** Emits the action sound for our owner widget, if one is configured. */
    public void soundAction () {
        if (_actionSound != null) _actionSound.play();
    }

    /** Resolves the value for the supplied style via our owner widget. */
    protected <V> V resolveStyle (Style<V> style) {
        return Styles.resolveStyle(_owner, style);
    }

    /** Returns the {@link Root} to which our owning element is added, or null. */
    protected Root root () {
        return _owner.root();
    }

    /** Called when the pointer is clicked on our widget. */
    protected void onPress (Pointer.Event event) {
        _owner.set(Element.Flag.SELECTED, true);
        _owner.invalidate();
    }

    /** Called as the user drags the pointer around with the widget depressed. */
    protected void onHover (Pointer.Event event, boolean inBounds) {
        updateSelected(inBounds);
    }

    /** Called when the pointer is released after having been pressed on this widget. This should
     * {@link #onClick} if appropriate. */
    protected void onRelease (Pointer.Event event) {
        if (_owner.isSelected()) {
            _owner.set(Element.Flag.SELECTED, false);
            _owner.invalidate();
            onClick(event);
        }
    }

    /** Called when the interaction is canceled after having been pressed on this widget. This
     * should not result in a call to {@link #onClick}. */
    protected void onCancel (Pointer.Event event) {
        if (_owner.isSelected()) {
            _owner.set(Element.Flag.SELECTED, false);
            _owner.invalidate();
        }
    }

    /** Called when the pointer is pressed and released over our owning widget. */
    protected void onClick (Pointer.Event event) {
    }

    /** Updates the selected state of our owner, invalidating if selectedness changes. */
    protected void updateSelected (boolean selected) {
        if (selected != _owner.isSelected()) {
            _owner.set(Element.Flag.SELECTED, selected);
            _owner.invalidate();
        }
    }

    protected final T _owner;
    protected Sound _actionSound;
}
