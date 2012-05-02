//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.PlayN;
import playn.core.ResourceCallback;
import playn.core.TextFormat;
import playn.core.TextLayout;

import pythagoras.f.Dimension;
import pythagoras.f.IRectangle;
import pythagoras.f.MathUtil;

import react.Slot;

import tripleplay.util.Objects;

/**
 * An abstract base class for widgets that contain text.
 */
public abstract class TextWidget<T extends TextWidget<T>> extends Widget<T>
{
    /**
     * Returns the current text displayed by this widget, or null if it has no text.
     */
    protected abstract String text ();

    /**
     * Returns the current icon displayed by this widget, or null if it has no icon.
     */
    protected abstract Image icon ();

    /**
     * Returns a slot that subclasses should wire up to their text {@code Value}.
     */
    protected Slot<String> textDidChange () {
        return new Slot<String> () {
            @Override public void onEmit (String newText) {
                clearLayoutData();
                invalidate();
            }
        };
    }

    /**
     * Returns a slot that subclasses should wire up to their icon {@code Value}.
     */
    protected Slot<Image> iconDidChange() {
        return new Slot<Image>() {
            @Override public void onEmit (Image icon) {
                if (icon == null) {
                    clearLayoutData();
                    invalidate();
                } else {
                    icon.addCallback(new ResourceCallback<Image>() {
                        public void done (Image resource) {
                            clearLayoutData();
                            invalidate();
                        }
                        public void error (Throwable err) {} // noop!
                    });
                }
            }
        };
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        if (_bginst != null) {
            _bginst.destroy();
            _bginst = null;
        }
        _tglyph.destroy();
        if (_ilayer != null) {
            _ilayer.destroy();
            _ilayer = null;
        }
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        LayoutData ldata = computeLayout(hintX, hintY);
        Dimension size = computeContentsSize(ldata, new Dimension());
        return ldata.bg.addInsets(size);
    }

    @Override protected void layout () {
        float width = _size.width, height = _size.height;
        LayoutData ldata = computeLayout(width, height);

        // prepare our background
        Background bg = ldata.bg;
        if (_bginst != null) _bginst.destroy();
        if (_size.width > 0 && _size.height > 0) {
            _bginst = bg.instantiate(_size);
            _bginst.addTo(layer);
        }
        width -= bg.width();
        height -= bg.height();

        // prepare our label and icon
        renderLayout(ldata, bg.left, bg.top, width, height);

        clearLayoutData(); // we no longer need our layout data
    }

    @Override protected void clearLayoutData () {
        super.clearLayoutData();
        _ldata = null;
    }

    protected LayoutData createLayoutData () {
        return new LayoutData();
    }

    protected LayoutData computeLayout (float hintX, float hintY) {
        if (_ldata != null) return _ldata;
        _ldata = createLayoutData();

        // determine our background
        Background bg = resolveStyle(Style.BACKGROUND);
        hintX -= bg.width();
        hintY -= bg.height();
        _ldata.bg = bg;

        // layout our text and icon
        layoutContents(_ldata, hintX, hintY);

        return _ldata;
    }

    protected void layoutContents (LayoutData ldata, float hintX, float hintY) {
        if (!isVisible()) return;

        ldata.wrap = resolveStyle(Style.TEXT_WRAP);
        ldata.halign = resolveStyle(Style.HALIGN);
        ldata.valign = resolveStyle(Style.VALIGN);

        String curtext = text();
        boolean haveText = (curtext != null && curtext.length() > 0);

        Image icon = icon();
        if (icon != null) {
            ldata.iconPos = resolveStyle(Style.ICON_POS);
            ldata.iconGap = resolveStyle(Style.ICON_GAP);
            // remove the icon space from our hint dimensions
            switch (ldata.iconPos) {
            case LEFT:
            case RIGHT:
                hintX -= icon.width();
                if (haveText) hintX -= ldata.iconGap;
                break;
            case ABOVE:
            case BELOW:
                hintY -= icon.height();
                if (haveText) hintX -= ldata.iconGap;
                break;
            }
        }

        if (haveText) {
            TextFormat format = Style.createTextFormat(this);
            if (hintX > 0 && ldata.wrap) format = format.withWrapWidth(hintX);
            // TODO: should we do something with a y-hint?
            ldata.text = PlayN.graphics().layoutText(curtext, format);
        }
    }

    protected Dimension computeContentsSize (LayoutData ldata, Dimension size) {
        if (_constraint instanceof Constraints.TextConstraint) {
            ((Constraints.TextConstraint)_constraint).addTextSize(size, ldata.text);
        } else if (ldata.text != null) {
            size.width += ldata.text.width();
            size.height += ldata.text.height();
        }
        Image icon = icon();
        if (icon != null) {
            switch (ldata.iconPos) {
            case LEFT:
            case RIGHT:
                size.width += icon.width();
                if (ldata.text != null) size.width += ldata.iconGap;
                size.height = Math.max(size.height, icon.height());
                break;
            case ABOVE:
            case BELOW:
                size.width = Math.max(size.width, icon.width());
                size.height += icon.height();
                if (ldata.text != null) size.height += ldata.iconGap;
                break;
            }
        }
        return size;
    }

    protected void renderLayout (LayoutData ldata, float x, float y, float width, float height) {
        float tx = x, ty = y, usedWidth = 0, usedHeight = 0;
        Image icon = icon();
        if (icon != null && ldata.iconPos != null) {
            float ix = x, iy = y;
            float iwidth = icon.width(), iheight = icon.height();
            switch (ldata.iconPos) {
            case LEFT:
                tx += iwidth + ldata.iconGap;
                iy += ldata.valign.offset(iheight, height);
                usedWidth = iwidth;
                break;
            case ABOVE:
                ty += iheight + ldata.iconGap;
                ix += ldata.halign.offset(iwidth, width);
                usedHeight = iheight;
                break;
            case RIGHT:
                ix += width - iwidth;
                iy += ldata.valign.offset(iheight, height);
                usedWidth = iwidth;
                break;
            case BELOW:
                iy += height - iheight;
                ix += ldata.halign.offset(iwidth, width);
                usedHeight = iheight;
                break;
            }
            if (_ilayer == null) layer.add(_ilayer = PlayN.graphics().createImageLayer(icon));
            else _ilayer.setImage(icon);
            _ilayer.setTranslation(ix, iy);
        } else if (icon == null && _ilayer != null) {
            layer.remove(_ilayer);
            _ilayer = null;
        }

        if (ldata.text != null) {
            float availWidth = width-usedWidth, availHeight = height-usedHeight;
            float twidth = Math.min(availWidth, ldata.text.width());
            float theight = Math.min(availHeight, ldata.text.height());
            createTextLayer(ldata, tx, ty, twidth, theight, availWidth, availHeight);
        } else {
            _tglyph.destroy();
        }
    }

    // this is broken out so that subclasses can extend this action
    protected void createTextLayer (LayoutData ldata, float tx, float ty,
                                    float twidth, float theight,
                                    float availWidth, float availHeight) {
        if (twidth > 0 && theight > 0) {
            _tglyph.prepare(twidth, theight);
            _tglyph.canvas().drawText(ldata.text, 0, 0);
            _tglyph.layer().setTranslation(
                MathUtil.ifloor(tx + ldata.halign.offset(twidth, availWidth)),
                MathUtil.ifloor(ty + ldata.valign.offset(theight, availHeight)));
        }
    }

    protected static class LayoutData {
        public TextLayout text, maxText;
        public boolean wrap;
        public Style.HAlign halign;
        public Style.VAlign valign;
        public Style.Pos iconPos;
        public int iconGap;
        public Background bg;
    }

    protected Background.Instance _bginst;
    protected LayoutData _ldata;

    protected final Glyph _tglyph = new Glyph();

    protected ImageLayer _ilayer;
    protected String _maxText;
}
