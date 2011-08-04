//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import forplay.core.CanvasLayer;
import forplay.core.ForPlay;
import forplay.core.TextFormat;
import forplay.core.TextLayout;

import react.Slot;

/**
 * An abstract base class for widgets that contain text.
 */
public abstract class TextWidget extends Widget
{
    /**
     * Returns the text configured for this widget.
     */
    public String text () {
        return _text;
    }

    /**
     * Updates the text configured for this widget.
     */
    public TextWidget setText (String text) {
        if (!text.equals(_text)) {
            _text = text;
            clearTextLayer();
            invalidate();
        }
        return this;
    }

    /**
     * Returns a slot which can be used to wire the text of this widget to a {@link react.Signal}
     * or {@link react.Value}.
     */
    public Slot<String> textSlot () {
        return new Slot<String>() {
            public void onEmit (String text) {
                setText(text);
            }
        };
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        clearTextLayer();
    }

    protected void layoutText (LayoutData ldata, String text, float hintX, float hintY) {
        if (text.length() == 0 || !isVisible()) return;

        TextFormat format = Style.createTextFormat(this, state());
        // TODO: should we do something with a y-hint?
        if (hintX > 0) {
            format = format.withWrapWidth(hintX);
        }
        ldata.text = ForPlay.graphics().layoutText(text, format);
        ldata.halign = resolveStyle(state(), Style.HALIGN);
        ldata.valign = resolveStyle(state(), Style.VALIGN);
    }

    protected void renderLayout (LayoutData ldata, float x, float y, float width, float height) {
        if (ldata.text != null) {
            float twidth = ldata.text.width(), theight = ldata.text.height();
            _tlayer = prepareCanvas(_tlayer, twidth, theight);
            // _tlayer.canvas().setFillColor(0xFFCCCCCC);
            // _tlayer.canvas().fillRect(0, 0, width, height);
            _tlayer.canvas().drawText(ldata.text, 0, 0);
            _tlayer.setTranslation(x + ldata.halign.getOffset(twidth, width),
                                   y + ldata.valign.getOffset(theight, height));
        }
    }

    protected void clearTextLayer () {
        if (_tlayer != null) {
            _tlayer.destroy();
            _tlayer = null;
        }
    }

    protected static class LayoutData {
        public TextLayout text;
        public Style.HAlign halign;
        public Style.VAlign valign;
    }

    protected String _text;
    protected CanvasLayer _tlayer;
}
