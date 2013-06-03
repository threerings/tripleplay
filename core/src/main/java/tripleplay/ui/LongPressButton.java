//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.PlayN;
import playn.core.Pointer;
import react.Signal;
import react.SignalView;
import react.Slot;

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

    /** Creates a button with the supplied icon. */
    @Deprecated
    public LongPressButton (Image icon) {
        this(null, Icons.image(icon));
    }

    /** Creates a button with the supplied text and icon. */
    @Deprecated
    public LongPressButton (String text, Image icon) {
        this(text, Icons.image(icon));
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
        if (_actionSound != null) _actionSound.play();
        _longPressed.emit(this);
    }

    /** A convenience method for registering a long press handler. Assumes you don't need the
     * result of {@link SignalView#connect}, because it throws it away. */
    public LongPressButton onLongPress (Slot<? super Button> onLongPress) {
        longPressed().connect(onLongPress);
        return this;
    }

    @Override protected void layout () {
        super.layout();
        _longPressInterval = resolveStyle(LONG_PRESS_INTERVAL);
    }

    @Override protected void onPress (Pointer.Event event) {
        super.onPress(event);
        startLongPressTimer();
    }

    @Override protected void onHover (Pointer.Event event, boolean inBounds) {
        super.onHover(event, inBounds);
        if (!inBounds) cancelLongPressTimer();
        else startLongPressTimer();
    }

    @Override protected void onRelease (Pointer.Event event) {
        super.onRelease(event);
        cancelLongPressTimer();
    }

    protected void startLongPressTimer () {
        if (_longPressInterval > 0 && _longPressReg == null) {
            _longPressReg = root().iface().addTask(new Interface.Task() {
                @Override public void update (int delta) {
                    _accum += delta;
                    if (_accum > _longPressInterval) fireLongPress();
                }
                protected int _accum;
            });
        }
    }

    protected void cancelLongPressTimer () {
        if (_longPressReg != null) {
            _longPressReg.remove();
            _longPressReg = null;
        }
    }

    protected void fireLongPress () {
        // cancel the current interaction which will disarm the button
        PlayN.pointer().cancelLayerDrags();
        cancelLongPressTimer();
        longPress();
    }

    protected final Signal<Button> _longPressed = Signal.create();
    protected int _longPressInterval;
    protected Interface.TaskHandle _longPressReg;
}
