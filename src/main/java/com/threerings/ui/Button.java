//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import forplay.core.Canvas;
import forplay.core.CanvasLayer;
import forplay.core.ForPlay;
import forplay.core.TextFormat;
import forplay.core.TextLayout;

import pythagoras.f.Dimension;

/**
 * A button that displays text, or an icon, or both.
 */
public class Button extends TextWidget
{
    /**
     * Returns the currently configured text, or null if the button does not use text.
     */
    @Override public String text () {
        return _text;
    }

    /**
     * Sets the text of this button to the supplied value.
     */
    @Override public Button setText (String text) {
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
        // clear out our background instance
        if (_bginst != null) _bginst.destroy();
        // if we're added again, we'll be re-laid-out
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        LayoutData ldata = computeLayout(hintX, hintY);
        // TODO: if we have an icon, add that into the mix
        return ldata.bg.addInsets(new Dimension(ldata.text.width(), ldata.text.height()));
    }

    @Override protected void layout () {
        LayoutData ldata = computeLayout(_size.width, _size.height);

        // prepare our background
        if (_bginst != null) _bginst.destroy();
        _bginst = ldata.bg.instantiate(_size);
        _bginst.addTo(layer);

        // prepare our label
        if (_text.length() > 0) {
            float twidth = _size.width - ldata.bg.width();
            float theight = _size.height - ldata.bg.height();
            _tlayer = prepareCanvas(_tlayer, twidth, theight);
            // _tlayer.canvas().setFillColor(0xFFCCCCCC);
            // _tlayer.canvas().fillRect(0, 0, _size.width, _size.height);
            _tlayer.canvas().drawText(ldata.text, 0, 0);
            _tlayer.setTranslation(ldata.bg.left, ldata.bg.top);
        } else {
            if (_tlayer != null) _tlayer.destroy();
        }

        _ldata = null; // we no longer need our layout data
    }

    @Override protected State state () {
        return super.state(); // TODO: return down state
    }

    protected LayoutData computeLayout (float hintX, float hintY) {
        if (_ldata != null) return _ldata;
        _ldata = new LayoutData();

        // determine our background
        _ldata.bg = resolveStyle(state(), Style.BACKGROUND);
        hintX -= _ldata.bg.width();
        hintY -= _ldata.bg.height();

        TextFormat format = Style.createTextFormat(this, state());
        // TODO: should we do something with a y-hint?
        if (hintX > 0) {
            format = format.withWrapWidth(hintX);
        }
        _ldata.text = ForPlay.graphics().layoutText(_text, format);

        return _ldata;
    }

    protected static class LayoutData {
        public TextLayout text;
        public Background bg;
    }

    protected String _text;
    protected CanvasLayer _tlayer;
    protected Background.Instance _bginst;
    protected LayoutData _ldata;
}
