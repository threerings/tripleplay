//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.Pointer;
import playn.core.Sound;
import react.Signal;
import react.SignalView;
import react.Slot;
import react.Value;
import react.ValueView;

/**
* A button that displays text, or an icon, or both.
*/
public class Button extends ClickableTextWidget<Button>
    implements Clickable<Button>
{
    /** A delay (in milliseconds) during which a button will remain unclickable after it has been
     * clicked. This ensures that users don't hammer away at a button, triggering multiple
     * responses (which code rarely protects against). Inherited. */
    public static Style<Integer> DEBOUNCE_DELAY = Style.newStyle(true, 500);

    /** The text displayed by this widget, or null. */
    public final Value<String> text = Value.create((String)null);

    /** The icon displayed by this widget, or null. */
    public final Value<Icon> icon = Value.<Icon>create(null);

    /** Creates a button with no text or icon. */
    public Button () {
        this(null, (Icon)null);
    }

    /**  Creates a button with the supplied text. */
    public Button (String text) {
        this(text, (Icon)null);
    }

    /** Creates a button with the supplied icon. */
    public Button (Icon icon) {
        this(null, icon);
    }

    /** Creates a button with the supplied icon. */
    @Deprecated
    public Button (Image icon) {
        this(null, Icons.image(icon));
    }

    /** Creates a button with the supplied text and icon. */
    @Deprecated
    public Button (String text, Image icon) {
        this(text, Icons.image(icon));
    }

    /** Creates a button with the supplied text and icon. */
    public Button (String text, Icon icon) {
        this.text.update(text);
        this.icon.update(icon);
        this.text.connect(textDidChange());
        this.icon.connect(iconDidChange());
    }

    /**
     * Binds the text of this button to the supplied reactive value. The current text will be
     * adjusted to match the state of {@code text}.
     */
    public Button bindText (ValueView<String> text) {
        text.connectNotify(this.text.slot());
        return this;
    }

    /**
     * Binds the icon of this button to the supplied reactive value. The current icon will be
     * adjusted to match the state of {@code icon}.
     */
    public Button bindIcon (ValueView<Icon> icon) {
        icon.connectNotify(this.icon.slot());
        return this;
    }

    /** Programmatically triggers a click of this button. This triggers the action sound, but does
     * not cause any change in the button's visualization. <em>Note:</em> this does not check the
     * button's enabled state, so the caller must handle that if appropriate. */
    public void click () {
        if (_actionSound != null) _actionSound.play();
        _clicked.emit(this); // emit a click event
    }

    /** A convenience method for registering a click handler. Assumes you don't need the result of
     * {@link SignalView#connect}, because it throws it away. */
    public Button onClick (Slot<? super Button> onClick) {
        clicked().connect(onClick);
        return this;
    }

    @Override public SignalView<Button> clicked () {
        return _clicked;
    }

    @Override public String toString () {
        return "Button(" + text.get() + ")";
    }

    @Override protected Class<?> getStyleClass () {
        return Button.class;
    }

    @Override protected void layout () {
        super.layout();
        _actionSound = resolveStyle(Style.ACTION_SOUND);
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

    @Override protected String text () {
        return text.get();
    }

    @Override protected Icon icon () {
        return icon.get();
    }

    protected final Signal<Button> _clicked = Signal.create();
    protected Sound _actionSound;
    protected int _debounceDelay;
    protected double _lastClickStamp;
}
