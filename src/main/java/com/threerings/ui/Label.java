//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

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
     * Creates a label with the specified starting text.
     */
    public Label (String text) {
        setText(text);
    }

    /**
     * Returns the currently configured text.
     */
    @Override public String text () {
        return _text;
    }

    /**
     * Sets the text of this label to the supplied value.
     */
    @Override public Label setText (String text) {
        if (!text.equals(_text)) {
            _text = text;
            invalidate();
        }
        return this;
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        // destroy our text canvas
        if (_tlayer != null) _tlayer.destroy();
        // if we're added again, we'll be re-laid-out
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        TextFormat format = Style.createTextFormat(this, state());
        if (hintX > 0) {
            format = format.withWrapWidth(hintX);
        }
        // TODO: should we do something with a y-hint?
        _layout = ForPlay.graphics().layoutText(_text, format);
        return new Dimension(_layout.width(), _layout.height());
    }

    @Override protected void layout () {
        // prepare our label
        if (_text.length() > 0) {
            _tlayer = prepareCanvas(_tlayer, _size.width, _size.height);
            _tlayer.canvas().drawText(_layout, 0, 0);
        } else {
            if (_tlayer != null) _tlayer.destroy();
        }

        _layout = null; // no need to keep this around
    }

    protected String _text;
    protected CanvasLayer _tlayer;
    protected TextLayout _layout;
}
