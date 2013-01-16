//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;

import react.Value;

/**
 * A widget that displays one or more lines of text and/or an icon image.
 */
public class Label extends TextWidget<Label>
{
    /** The text displayed by this widget, or null. */
    public final Value<String> text = Value.create((String)null);

    /** The icon displayed by this widget, or null. */
    public final Value<Image> icon = Value.<Image>create(null);

    /** Creates a label with no text or icon. */
    public Label () {
        this(null, null);
    }

    /**  Creates a label with the supplied text. */
    public Label (String text) {
        this(text, null);
    }

    /** Creates a label with the supplied icon. */
    public Label (Image icon) {
        this(null, icon);
    }

    /** Creates a label with the supplied text and icon. */
    public Label (String text, Image icon) {
        this.text.update(text);
        this.icon.update(icon);
        this.text.connect(textDidChange());
        this.icon.connect(iconDidChange());
    }

    @Override public String toString () {
        return "Label(" + text.get() + ")";
    }

    @Override protected Class<?> getStyleClass () {
        return Label.class;
    }

    @Override protected String text () {
        return text.get();
    }

    @Override protected Image icon () {
        return icon.get();
    }
}
