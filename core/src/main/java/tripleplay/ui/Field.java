//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.Keyboard;
import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.util.Callback;

import react.Value;

/**
 * Displays text which can be edited via the {@link Keyboard#getText} popup.
 */
public class Field extends TextWidget<Field>
{
    /** The text displayed by this widget. */
    public final Value<String> text = Value.create("");

    public Field () {
        this("");
    }

    public Field (String initialText) {
        this(initialText, Styles.none());
    }

    public Field (Styles styles) {
        this("", styles);
    }

    public Field (String initialText, Styles styles) {
        enableInteraction();
        setStyles(styles);
        this.text.update(initialText);
        this.text.connect(textDidChange());
    }

    /**
     * Configures the keyboard type to use when text is requested via a popup.
     */
    public Field setTextType (Keyboard.TextType type) {
        _textType = type;
        return this;
    }

    /**
     * Configures the label to be displayed when text is requested via a popup.
     */
    public Field setPopupLabel (String label) {
        _popupLabel = label;
        return this;
    }

    @Override protected String text () {
        String ctext = text.get();
        // we always want non-empty text so that we force ourselves to always have a text layer and
        // sane dimensions even if the text field contains no text
        return (ctext == null || ctext.length() == 0) ? " " : ctext;
    }

    @Override protected Image icon () {
        return null; // fields never have an icon
    }

    @Override protected void onPointerStart (Pointer.Event event, float x, float y) {
        super.onPointerStart(event, x, y);
        if (!isEnabled()) return;

        PlayN.keyboard().getText(_textType, _popupLabel, text.get(), new Callback<String>() {
            @Override public void onSuccess (String result) {
                // null result is a canceled entry dialog.
                if (result != null) text.update(result);
            }
            @Override public void onFailure (Throwable cause) { /* noop */ }
        });
    }

    // used when popping up a text entry interface on mobile platforms
    protected Keyboard.TextType _textType = Keyboard.TextType.DEFAULT;
    protected String _popupLabel;
}
