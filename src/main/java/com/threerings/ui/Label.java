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
    @Override public Label setText (String text) {
        super.setText(text);
        return this;
    }

    @Override public String toString () {
        return "Label(" + _text + ")";
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        Dimension size = new Dimension();
        TextLayout layout = computeLayout(hintX, hintY);
        if (layout != null) {
            size.width += layout.width();
            size.height += layout.height();
        }
        return size;
    }

    @Override protected void layout () {
        float width = _size.width, height = _size.height;
        TextLayout layout = computeLayout(width, height);
        renderLayout(layout, 0, 0, width, height);
        _layout = null; // no need to keep this around
    }

    protected TextLayout computeLayout (float hintX, float hintY) {
        if (_layout != null) return _layout;
        _layout = layoutText(_text, hintX, hintY);
        return _layout;
    }

    protected TextLayout _layout;
}
