//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Pointer;
import playn.core.Sound;

import react.Signal;
import react.SignalView;
import react.Value;

/**
 * A toggle button that displays text, or an icon, or both. Clicking the button toggles it from
 * selected to unselected, and vice versa.
 */
public class ToggleButton extends TogglableTextWidget<ToggleButton>
    implements Clickable<ToggleButton>
{
    /** The text displayed by this widget, or null. */
    public final Value<String> text = Value.create(null);

    /** The icon displayed by this widget, or null. */
    public final Value<Icon> icon = Value.create(null);

    /** Creates a button with no text or icon. */
    public ToggleButton () {
        this(null, null);
    }

    /**  Creates a button with the supplied text. */
    public ToggleButton (String text) {
        this(text, null);
    }

    /** Creates a button with the supplied icon. */
    public ToggleButton (Icon icon) {
        this(null, icon);
    }

    /** Creates a button with the supplied text and icon. */
    public ToggleButton (String text, Icon icon) {
        this.text.update(text);
        this.icon.update(icon);
        this.text.connect(textDidChange());
        this.icon.connect(iconDidChange());
    }

    @Override public SignalView<ToggleButton> clicked () {
        return _clicked;
    }

    @Override public void click () {
        if (_actionSound != null) _actionSound.play();
        _clicked.emit(this); // emit a click event
    }

    @Override public String toString () {
        return "ToggleButton(" + text.get() + ")";
    }

    @Override protected Class<?> getStyleClass () {
        return ToggleButton.class;
    }

    @Override protected String text () {
        return text.get();
    }

    @Override protected Icon icon () {
        return icon.get();
    }

    @Override protected void layout () {
        super.layout();
        _actionSound = resolveStyle(Style.ACTION_SOUND);
    }

    @Override protected void onClick (Pointer.Event event) {
        click();
    }

    protected final Signal<ToggleButton> _clicked = Signal.create();
    protected Sound _actionSound;
}
