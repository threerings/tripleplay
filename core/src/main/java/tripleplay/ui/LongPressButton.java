//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.Closeable;
import react.Signal;
import react.SignalView;
import react.Slot;

import playn.core.Clock;
import playn.scene.Pointer;

/**
 * A button that supports an action on a "long press". A long press is when the user holds the
 * button in the armed state for some reasonably lengthy amount of time (the default is 1000ms).
 * This element behaves as a {@link Button} for style purposes.
 */
public class LongPressButton extends Button
{
    /** An interval (in milliseconds) after which pressing and holding on a button will be
     * interpreted as a "long press" and fire a clicked event. The button is then disarmed, so that
     * when the button is released after a long press, a normal click event is not reported.
     * Defaults to 1000ms. */
    public static Style<Integer> LONG_PRESS_INTERVAL = Style.newStyle(true, 1000);

    /** Creates a button with no text or icon. */
    public LongPressButton () {
        this(null, (Icon)null);
    }

    /**  Creates a button with the supplied text. */
    public LongPressButton (String text) {
        this(text, (Icon)null);
    }

    /** Creates a button with the supplied icon. */
    public LongPressButton (Icon icon) {
        this(null, icon);
    }

    /** Creates a button with the supplied text and icon. */
    public LongPressButton (String text, Icon icon) {
        super(text, icon);
    }

    /** A signal that is emitted when this button is long pressed.
     * See {@link #LONG_PRESS_INTERVAL}. */
    public SignalView<Button> longPressed () {
        return _longPressed;
    }

    /** Programmatically triggers a long press of this button. This triggers the action sound, but
     * does not cause any change in the button's visualization. <em>Note:</em> this does not check
     * the button's enabled state, so the caller must handle that if appropriate. */
    public void longPress () {
        ((Behavior.Click<Button>)_behave).soundAction();
        _longPressed.emit(this);
    }

    /** A convenience method for registering a long press handler. Assumes you don't need the
     * result of {@link SignalView#connect}, because it throws it away. */
    public LongPressButton onLongPress (SignalView.Listener<? super Button> onLongPress) {
        longPressed().connect(onLongPress);
        return this;
    }

    @Override protected Behavior<Button> createBehavior () {
        return new Behavior.Click<Button>(this) {
            @Override public void layout () {
                super.layout();
                _longPressInterval = resolveStyle(LONG_PRESS_INTERVAL);
            }

            @Override public void onPress (Pointer.Interaction iact) {
                super.onPress(iact);
                if (isSelected()) startLongPressTimer();
            }
            @Override public void onHover (Pointer.Interaction iact, boolean inBounds) {
                super.onHover(iact, inBounds);
                if (!inBounds) cancelLongPressTimer();
                else startLongPressTimer();
            }
            @Override public boolean onRelease (Pointer.Interaction iact) {
                boolean click = super.onRelease(iact);
                cancelLongPressTimer();
                return click;
            }

            protected void startLongPressTimer () {
                if (_longPressInterval > 0 && _timerReg == Closeable.NOOP) {
                    _timerReg = root().iface.frame.connect(new Slot<Clock>() {
                        @Override public void onEmit (Clock clock) {
                            _accum += clock.dt;
                            if (_accum > _longPressInterval) fireLongPress();
                        }
                        protected int _accum;
                    });
                }
            }
            protected void cancelLongPressTimer () {
                _timerReg = Closeable.close(_timerReg);
            }
            protected void fireLongPress () {
                // cancel the current interaction which will disarm the button
                onCancel(null);
                cancelLongPressTimer();
                longPress();
            }

            protected int _longPressInterval;
            protected Closeable _timerReg = Closeable.NOOP;
        };
    }

    protected final Signal<Button> _longPressed = Signal.create();
}
