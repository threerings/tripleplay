//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

/**
 * A widget that displays one or more lines of text and/or an icon image.
 */
public class Label extends TextWidget<Label>
{
    /** Creates a label with no text and inherited styles. */
    public Label () {
        this("");
    }

    /**  Creates a label with the given text and inherited styles. */
    public Label (String text) {
        this(text, Styles.none());
    }

    /** Creates a label with no text and inherited styles. */
    public Label (Styles styles) {
        this("", styles);
    }

    /** Creates a label with the given text and styles. */
    public Label (String text, Styles styles) {
        setStyles(styles).text.update(text);
    }

    @Override public String toString () {
        return "Label(" + text.get() + ")";
    }
}
