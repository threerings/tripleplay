//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.MathUtil;

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ResourceCallback;
import playn.core.TextFormat;
import playn.core.TextLayout;
import static playn.core.PlayN.graphics;

import react.Slot;
import tripleplay.util.EffectRenderer;

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

    @Override protected void wasAdded (Elements<?> parent) {
        super.wasAdded(parent);
        invalidate();
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        _tglyph.destroy();
        if (_ilayer != null) {
            _ilayer.destroy();
            _ilayer = null;
        }
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new TextLayoutData(hintX, hintY);
    }

    protected class TextLayoutData extends LayoutData {
        public final Style.HAlign halign = resolveStyle(Style.HALIGN);
        public final Style.VAlign valign = resolveStyle(Style.VALIGN);
        public final Style.Pos iconPos = resolveStyle(Style.ICON_POS);
        public final int iconGap = resolveStyle(Style.ICON_GAP);
        public final int color = resolveStyle(Style.COLOR);
        public final boolean wrap = resolveStyle(Style.TEXT_WRAP);

        public final TextLayout text;
        public final EffectRenderer renderer;

        public TextLayoutData (float hintX, float hintY) {
            String curtext = text();
            boolean haveText = (curtext != null && curtext.length() > 0);

            // remove our background insets from the hint
            hintX -= bg.width(); hintY -= bg.height();

            Image icon = icon();
            if (icon != null) {
                // remove the icon space from our hint dimensions
                switch (iconPos) {
                case LEFT:
                case RIGHT:
                    hintX -= icon.width();
                    if (haveText) hintX -= iconGap;
                    break;
                case ABOVE:
                case BELOW:
                    hintY -= icon.height();
                    if (haveText) hintX -= iconGap;
                    break;
                }
            }

            if (haveText) {
                renderer = Style.createEffectRenderer(TextWidget.this);
                TextFormat format = Style.createTextFormat(TextWidget.this);
                if (hintX > 0 && wrap) format = format.withWrapWidth(hintX);
                // TODO: should we do something with a y-hint?
                text = graphics().layoutText(curtext, format);
            } else {
                renderer = null;
                text = null;
            }
        }

        @Override public Dimension computeSize (float hintX, float hintY) {
            Dimension size = new Dimension();
            addTextSize(size);

            Image icon = icon();
            if (icon != null) {
                switch (iconPos) {
                case LEFT:
                case RIGHT:
                    size.width += icon.width();
                    if (text != null) size.width += iconGap;
                    size.height = Math.max(size.height, icon.height());
                    break;
                case ABOVE:
                case BELOW:
                    size.width = Math.max(size.width, icon.width());
                    size.height += icon.height();
                    if (text != null) size.height += iconGap;
                    break;
                }
            }
            return size;
        }

        @Override public void layout (float left, float top, float width, float height) {
            float tx = left, ty = top, usedWidth = 0, usedHeight = 0;

            Image icon = icon();
            if (icon != null && iconPos != null) {
                float ix = left, iy = top;
                float iwidth = icon.width(), iheight = icon.height();
                switch (iconPos) {
                case LEFT:
                    tx += iwidth + iconGap;
                    iy += valign.offset(iheight, height);
                    usedWidth = iwidth;
                    break;
                case ABOVE:
                    ty += iheight + iconGap;
                    ix += halign.offset(iwidth, width);
                    usedHeight = iheight;
                    break;
                case RIGHT:
                    ix += width - iwidth;
                    iy += valign.offset(iheight, height);
                    usedWidth = iwidth;
                    break;
                case BELOW:
                    iy += height - iheight;
                    ix += halign.offset(iwidth, width);
                    usedHeight = iheight;
                    break;
                }
                if (_ilayer == null) layer.add(_ilayer = graphics().createImageLayer(icon));
                else _ilayer.setImage(icon);
                _ilayer.setTranslation(ix, iy);

            } else if (icon == null && _ilayer != null) {
                layer.remove(_ilayer);
                _ilayer = null;
            }

            if (text != null) {
                updateTextGlyph(tx, ty, width-usedWidth, height-usedHeight);
            } else {
                _tglyph.destroy();
            }
        }

        // this is broken out so that subclasses can extend this action
        protected void addTextSize (Dimension size) {
            if (_constraint instanceof Constraints.TextConstraint) {
                ((Constraints.TextConstraint)_constraint).addTextSize(size, text);
            } else if (text != null) {
                size.width += textWidth();
                size.height += textHeight();
            }
        }

        // this is broken out so that subclasses can extend this action
        protected void updateTextGlyph (float tx, float ty, float availWidth, float availHeight) {
            float twidth = textWidth(), theight = textHeight();
            if (twidth <= 0 || theight <= 0) return;

            // make sure our canvas layer is big enough to hold our text
            _tglyph.prepare(twidth, theight);

            // we do some extra fiddling here because one may want to constrain the height of a
            // button such that the text is actually cut off on the top and/or bottom because fonts
            // may have lots of whitespace above or below and you're trying to squeeze the text
            // snugly into your button
            float oy = valign.offset(theight, availHeight);
            if (oy >= 0) {
                renderer.render(_tglyph.canvas(), text, color, 0, 0);
            } else {
                renderer.render(_tglyph.canvas(), text, color, 0, oy);
                oy = 0;
            }
            _tglyph.layer().setTranslation(MathUtil.ifloor(tx + halign.offset(twidth, availWidth)),
                                           MathUtil.ifloor(ty + oy));
        }

        protected float textWidth () { return renderer.adjustWidth(text.width()); }
        protected float textHeight () { return renderer.adjustHeight(text.height()); }
    }

    protected final Glyph _tglyph = new Glyph();
    protected ImageLayer _ilayer;
}
