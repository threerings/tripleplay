//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.Sound;

import react.Signal;
import react.SignalView;
import react.Value;

/**
* A button that displays text, or an icon, or both.
*/
public class Button extends ClickableTextWidget<Button>
    implements Clickable<Button>
{
    /** The text displayed by this widget, or null. */
    public final Value<String> text = Value.create((String)null);

    /** The icon displayed by this widget, or null. */
    public final Value<Image> icon = Value.<Image>create(null);

    /** Creates a button with no text or icon. */
    public Button () {
        this(null, null);
    }

    /**  Creates a button with the supplied text. */
    public Button (String text) {
        this(text, null);
    }

    /** Creates a button with the supplied icon. */
    public Button (Image icon) {
        this(null, icon);
    }

    /** Creates a button with the supplied text and icon. */
    public Button (String text, Image icon) {
        this.text.update(text);
        this.icon.update(icon);
        this.text.connect(textDidChange());
        this.icon.connect(iconDidChange());
    }

    /** @deprecated Call {@code button.icon.update(icon)} or pass your icon to the ctor. */
    @Deprecated
    public Button setIcon (Image icon) {
        this.icon.update(icon);
        return this;
    }

    @Override public SignalView<Button> clicked () {
        return _clicked;
    }

    @Override public String toString () {
        return "Button(" + text.get() + ")";
    }

    @Override protected void layout () {
        super.layout();
        _actionSound = resolveStyle(Style.ACTION_SOUND);
    }

    @Override protected void onClick () {
        if (_actionSound != null) _actionSound.play();
        _clicked.emit(this); // emit a click event
    }

    @Override protected String text () {
        return text.get();
    }

    @Override protected Image icon () {
        return icon.get();
    }

    protected final Signal<Button> _clicked = Signal.create();
    protected Sound _actionSound;
}
