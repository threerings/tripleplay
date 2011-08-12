//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.CanvasLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.PlayN;
import playn.core.ResourceCallback;
import playn.core.TextFormat;
import playn.core.TextLayout;

import pythagoras.f.Dimension;
import pythagoras.f.IRectangle;

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
            clearLayoutData();
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

    /**
     * Sets the icon to be displayed by this widget.
     */
    public TextWidget setIcon (Image icon) {
        if (icon != _icon || _iregion != null) {
            _icon = icon;
            _icon.addCallback(new ResourceCallback<Image>() {
                public void done (Image resource) {
                    clearLayoutData();
                    invalidate();
                }
                public void error (Throwable err) {
                    // noop!
                }
            });
            _iregion = null;
        }
        return this;
    }

    /**
     * Sets the icon to be displayed by this widget.
     *
     * @param region the subregion of the supplied image to be used as the icon.
     */
    public TextWidget setIcon (Image icon, IRectangle region) {
        if (icon != _icon || !region.equals(_iregion)) {
            _icon = icon;
            _iregion = region;
            _icon.addCallback(new ResourceCallback<Image>() {
                public void done (Image resource) {
                    clearLayoutData();
                    invalidate();
                }
                public void error (Throwable err) {
                    // noop!
                }
            });
        }
        return this;
    }

    /**
     * Returns the icon displayed by this widget, or null.
     */
    public Image icon () {
        return _icon;
    }

    /**
     * Returns a slot which can be used to wire the icon of this widget to a {@link react.Signal}
     * or {@link react.Value}.
     */
    public Slot<Image> iconSlot () {
        return new Slot<Image>() {
            public void onEmit (Image icon) {
                setIcon(icon);
            }
        };
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        clearTextLayer();
        clearIconLayer();
    }

    protected void layoutText (LayoutData ldata, String text, float hintX, float hintY) {
        if (!isVisible()) return;

        if (text.length() > 0) {
            TextFormat format = Style.createTextFormat(this, state());
            if (hintX > 0) format = format.withWrapWidth(hintX);
            // TODO: should we do something with a y-hint?
            ldata.text = PlayN.graphics().layoutText(text, format);
        }
        ldata.halign = resolveStyle(state(), Style.HALIGN);
        ldata.valign = resolveStyle(state(), Style.VALIGN);
        if (_icon != null) {
            ldata.iconPos = resolveStyle(state(), Style.ICON_POS);
            ldata.iconGap = resolveStyle(state(), Style.ICON_GAP);
        }
    }

    protected Dimension computeTextSize (LayoutData ldata, Dimension size) {
        if (ldata.text != null) {
            size.width += ldata.text.width();
            size.height += ldata.text.height();
        }
        if (_icon != null) {
            switch (ldata.iconPos) {
            case LEFT:
            case RIGHT:
                size.width += (iconWidth() + ldata.iconGap);
                size.height = Math.max(size.height, iconHeight());
                break;
            case ABOVE:
            case BELOW:
                size.width = Math.max(size.width, iconWidth());
                size.height += (iconHeight() + ldata.iconGap);
                break;
            }
        }
        return size;
    }

    protected void renderLayout (LayoutData ldata, float x, float y, float width, float height) {
        float tx = x, ty = y, usedWidth = 0, usedHeight = 0;
        if (_icon != null) {
            float ix = x, iy = y;
            float iwidth = iconWidth(), iheight = iconHeight();
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
            if (_ilayer == null) layer.add(_ilayer = PlayN.graphics().createImageLayer(_icon));
            else _ilayer.setImage(_icon);
            if (_iregion != null) {
                _ilayer.setWidth(_iregion.width());
                _ilayer.setHeight(_iregion.height());
                _ilayer.setSourceRect(_iregion.x(), _iregion.y(),
                                      _iregion.width(), _iregion.height());
            }
            _ilayer.setTranslation(ix, iy);
        }

        if (ldata.text != null) {
            float twidth = ldata.text.width(), theight = ldata.text.height();
            _tlayer = prepareCanvas(_tlayer, twidth, theight);
            // _tlayer.canvas().setFillColor(0xFFCCCCCC);
            // _tlayer.canvas().fillRect(0, 0, width, height);
            _tlayer.canvas().drawText(ldata.text, 0, 0);
            _tlayer.setTranslation(tx + ldata.halign.offset(twidth, width-usedWidth),
                                   ty + ldata.valign.offset(theight, height-usedHeight));
        }
    }

    protected void clearTextLayer () {
        if (_tlayer != null) {
            _tlayer.destroy();
            _tlayer = null;
        }
    }

    protected void clearIconLayer () {
        if (_ilayer != null) {
            _ilayer.destroy();
            _ilayer = null;
        }
    }

    protected float iconWidth () {
        return (_iregion == null) ? _icon.width() : _iregion.width();
    }

    protected float iconHeight () {
        return (_iregion == null) ? _icon.height() : _iregion.height();
    }

    protected static class LayoutData {
        public TextLayout text;
        public Style.HAlign halign;
        public Style.VAlign valign;
        public Style.Pos iconPos;
        public int iconGap;
    }

    protected String _text = "";
    protected CanvasLayer _tlayer;

    protected Image _icon;
    protected IRectangle _iregion;
    protected ImageLayer _ilayer;
}
