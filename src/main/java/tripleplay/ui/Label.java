//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;

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

    /** Creates a label with the given text and styles.
    public Label (String text, Styles styles) {
        setText(text).setStyles(styles);
    }

    @Override public String toString () {
        return "Label(" + text() + ")";
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        LayoutData ldata = computeLayout(hintX, hintY);
        return computeTextSize(ldata, new Dimension());
    }

    @Override protected void layout () {
        float width = _size.width, height = _size.height;
        LayoutData ldata = computeLayout(width, height);
        renderLayout(ldata, 0, 0, width, height);
        clearLayoutData(); // no need to keep this around
    }

    @Override protected void clearLayoutData () {
        super.clearLayoutData();
        _ldata = null;
    }

    protected LayoutData computeLayout (float hintX, float hintY) {
        if (_ldata != null) return _ldata;
        _ldata = new LayoutData();
        layoutText(_ldata, _text, hintX, hintY);
        return _ldata;
    }

    protected LayoutData _ldata;
}
