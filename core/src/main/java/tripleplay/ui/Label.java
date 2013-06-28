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
    public final Value<String> text = Value.create(null);

    /** The icon displayed by this widget, or null. */
    public final Value<Icon> icon = Value.create(null);

    /** Creates a label with no text or icon. */
    public Label () {
        this(null, (Icon)null);
    }

    /**  Creates a label with the supplied text. */
    public Label (String text) {
        this(text, (Icon)null);
    }

    /** Creates a label with the supplied icon. */
    public Label (Icon icon) {
        this(null, icon);
    }

    /** Creates a label with the supplied icon. */
    @Deprecated
    public Label (Image icon) {
        this(null, Icons.image(icon));
    }

    /** Creates a label with the supplied text and icon. */
    @Deprecated
    public Label (String text, Image icon) {
        this(text, Icons.image(icon));
    }

    /** Creates a label with the supplied text and icon. */
    public Label (String text, Icon icon) {
        this.text.update(text);
        this.text.connect(textDidChange());
        // update after connect so we trigger iconDidChange, in case our icon is a not-ready-image
        this.icon.connect(iconDidChange());
        this.icon.update(icon);
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

    @Override protected Icon icon () {
        return icon.get();
    }
}
