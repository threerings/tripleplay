//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.Closeable;
import react.ValueView;

/**
 * A label that displays a {@link ValueView}. If you already have the contents of the label in a
 * reactive value, you can use this to simply display it rather than using a stock {@link Label}
 * which requires manually connecting your value to the label's value. This label does not support
 * icons. If you want an icon and changing text, you'll have to manually wire up a {@link Label}.
 */
public class ValueLabel extends TextWidget<ValueLabel>
{
    /** The source for the text of this label. */
    public final ValueView<?> text;

    /** Creates a label with the supplied value. The value will be converted to a string for
      * display as this label's text. */
    public ValueLabel (ValueView<?> text) {
        this.text = text;
    }

    @Override protected void wasAdded () {
        super.wasAdded();
        _conn = text.connect(textDidChange());
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        _conn.close();
    }

    @Override public String toString () { return "VLabel(" + text.get() + ")"; }
    @Override protected Class<?> getStyleClass () { return Label.class; }
    @Override protected String text () { return String.valueOf(text.get()); }
    @Override protected Icon icon () { return null; }

    protected Closeable _conn;
}
