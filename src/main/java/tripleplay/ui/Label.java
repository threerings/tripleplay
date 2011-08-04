//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import forplay.core.CanvasLayer;
import forplay.core.ForPlay;
import forplay.core.TextFormat;
import forplay.core.TextLayout;

import pythagoras.f.Dimension;

/**
 * A widget that displays one or more lines of text.
 */
public class Label extends TextWidget
{
    /**
     * Creates a label with no custom styles.
     */
    public Label () {
    }

    /**
     * Creates a label with the specified custom styles.
     */
    public Label (Styles styles) {
        setStyles(styles);
    }

    /**
     * Creates a label with the specified custom styles.
     */
    public Label (Style.Binding<?>... bindings) {
        setStyles(Styles.make(bindings));
    }

    /**
     * Captures a reference to this element in the supplied reference. See {@link Ref} for why you
     * might want to do this.
     * @return this instance for convenient call chaining.
     */
    public Label ref (Ref<? super Label> ref) {
        ref.elem = this;
        return this;
    }

    @Override public Label setText (String text) {
        super.setText(text);
        return this;
    }

    @Override public String toString () {
        return "Label(" + _text + ")";
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        Dimension size = new Dimension();
        LayoutData ldata = computeLayout(hintX, hintY);
        if (ldata.text != null) {
            size.width += ldata.text.width();
            size.height += ldata.text.height();
        }
        return size;
    }

    @Override protected void layout () {
        float width = _size.width, height = _size.height;
        LayoutData ldata = computeLayout(width, height);
        renderLayout(ldata, 0, 0, width, height);
        _ldata = null; // no need to keep this around
    }

    protected LayoutData computeLayout (float hintX, float hintY) {
        if (_ldata != null) return _ldata;
        _ldata = new LayoutData();
        layoutText(_ldata, _text, hintX, hintY);
        return _ldata;
    }

    protected LayoutData _ldata;
}
