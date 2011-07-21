//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import forplay.core.Canvas;
import forplay.core.ForPlay;
import forplay.core.TextFormat;
import forplay.core.TextLayout;

import pythagoras.f.Dimension;

/**
 * A widget that displays one or more lines of text.
 */
public class Label extends Widget
{
    /**
     * Creates a label with the specified starting text.
     */
    public Label (String text) {
        setText(text);
    }

    /**
     * Returns the currently configured text.
     */
    public String text () {
        return _text;
    }

    /**
     * Sets the text of this label to the supplied value.
     */
    public void setText (String text) {
        if (!text.equals(_text)) {
            _text = text;
            invalidate();
        }
    }

    @Override protected void computeSize (float hintX, float hintY, Dimension into) {
        TextFormat format = Style.createTextFormat(this, state());
        if (hintX > 0) {
            format = format.withWrapWidth(hintX);
        }
        // TODO: should we do something with a y-hint?
        _layout = ForPlay.graphics().layoutText(_text, format);
        into.setSize(_layout.width(), _layout.height());
    }

    protected void render (Canvas canvas) {
        canvas.drawText(_layout, 0, 0);
        _layout = null; // no need to keep this around
    }

    protected String _text;
    protected TextLayout _layout;
}
