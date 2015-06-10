//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.Closeable;
import react.Value;
import react.ValueView;

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

    /** Creates a label with the supplied text and icon. */
    public Label (String text, Icon icon) {
        this.text.update(text);
        this.text.connect(textDidChange());
        // update after connect so we trigger iconDidChange, in case our icon is a not-ready-image
        this.icon.connect(iconDidChange());
        this.icon.update(icon);
    }

    /**
     * Binds the text of this label to the supplied reactive value. The current text will be
     * adjusted to match the state of {@code text}.
     */
    public Label bindText (final ValueView<String> textV) {
        return addBinding(new Binding(_bindings) {
            public Closeable connect () {
                return textV.connectNotify(text.slot());
            }
            @Override public String toString () {
                return Label.this + ".bindText";
            }
        });
    }

    /**
     * Binds the icon of this label to the supplied reactive value. The current icon will be
     * adjusted to match the state of {@code icon}.
     */
    public Label bindIcon (final ValueView<Icon> iconV) {
        return addBinding(new Binding(_bindings) {
            public Closeable connect () {
                return iconV.connectNotify(icon.slot());
            }
            @Override public String toString () {
                return Label.this + ".bindIcon";
            }
        });
    }

    /** Updates the text displayed by this label. */
    public Label setText (String text) {
        this.text.update(text);
        return this;
    }

    /** Updates the icon displayed by this label. */
    public Label setIcon (Icon icon) {
        this.icon.update(icon);
        return this;
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
