//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.FloatMath;
import pythagoras.f.MathUtil;

import playn.core.Canvas;
import playn.core.Graphics;
import playn.core.TextWrap;
import playn.scene.Layer;

import react.Slot;
import react.UnitSlot;

import tripleplay.util.Glyph;
import tripleplay.util.StyledText;
import tripleplay.util.TextStyle;

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
    protected abstract Icon icon ();

    /**
     * Returns a slot that subclasses should wire up to their text {@code Value}.
     */
    protected UnitSlot textDidChange () {
        return invalidateSlot(true);
    }

    /**
     * Returns a slot that subclasses should wire up to their icon {@code Value}.
     */
    protected Slot<Icon> iconDidChange () {
        return new Slot<Icon>() {
            @Override public void onEmit (Icon icon) {
                if (icon == null) {
                    clearLayoutData();
                    invalidate();
                } else {
                    icon.state().onSuccess(new Slot<Icon>() {
                        public void onEmit (Icon resource) {
                            // clear out the rendered icon in case we got laid out before the async
                            // load finished
                            _renderedIcon = null;
                            clearLayoutData();
                            invalidate();
                        }
                    });
                }
            }
        };
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        _tglyph.close();
        if (_ilayer != null) {
            _ilayer.close();
            _ilayer = null;
        }
        _renderedIcon = null;
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new TextLayoutData(hintX, hintY);
    }

    protected class TextLayoutData extends LayoutData {
        public final Style.HAlign halign = resolveStyle(Style.HALIGN);
        public final Style.VAlign valign = resolveStyle(Style.VALIGN);
        public final Style.Pos iconPos = resolveStyle(Style.ICON_POS);
        public final int iconGap = resolveStyle(Style.ICON_GAP);
        public final boolean iconCuddle = resolveStyle(Style.ICON_CUDDLE);
        public final IconEffect iconEffect = resolveStyle(Style.ICON_EFFECT);
        public final boolean wrap = resolveStyle(Style.TEXT_WRAP);
        public final boolean autoShrink = resolveStyle(Style.AUTO_SHRINK);
        public final float minFontSize = resolveStyle(Style.MIN_FONT_SIZE);

        public final Graphics gfx = root().iface.plat.graphics();
        public StyledText.Plain text; // mostly final, only changed by autoShrink
        public final Icon icon;

        public TextLayoutData (float hintX, float hintY) {
            String curtext = text();
            boolean haveText = (curtext != null && curtext.length() > 0);

            // start with hints minus background insets
            Dimension hints = bg.insets.subtractFrom(new Dimension(hintX, hintY));

            // apply effects to the icon, if we have one
            Icon icon = icon();
            this.icon = icon == null ? null : iconEffect.apply(icon);

            // accommodate our icon
            accommodateIcon(hints, haveText);

            // layout our text, if we have any
            if (haveText) {
                TextStyle style = Style.createTextStyle(TextWidget.this);
                // TODO: should we do something with a y-hint?
                if (hints.width > 0 && wrap) {
                    text = new StyledText.Block(gfx, curtext, style, new TextWrap(hints.width),
                                                Style.toAlignment(resolveStyle(Style.HALIGN)));
                } else {
                    text = new StyledText.Span(gfx, curtext, style);
                }
            }
        }

        @Override public Dimension computeSize (float hintX, float hintY) {
            if (text != null && autoShrink) {
                float usedWidth = 0;
                // account for the icon width and gap
                if (icon != null && iconPos.horizontal()) usedWidth = icon.width() + iconGap;
                // if autoShrink is enabled, and our text is too wide, re-lay it out with
                // successively smaller fonts until it fits
                float twidth = textWidth(), availWidth = hintX - usedWidth;
                if (twidth > availWidth) {
                    while (twidth > availWidth && text.style.font.size > minFontSize) {
                        text = text.resize(text.style.font.size-1);
                        twidth = FloatMath.ceil(textWidth());
                    }
                }
            }

            Dimension size = new Dimension();
            addTextSize(size);
            if (icon != null) {
                if (iconPos.horizontal()) {
                    size.width += icon.width();
                    if (text != null) size.width += iconGap;
                    size.height = Math.max(size.height, icon.height());
                } else {
                    size.width = Math.max(size.width, icon.width());
                    size.height += icon.height();
                    if (text != null) size.height += iconGap;
                }
            }

            return size;
        }

        @Override public void layout (float left, float top, float width, float height) {
            float tx = left, ty = top, usedWidth = 0, usedHeight = 0;

            if (icon != null && iconPos != null) {
                float ix = left, iy = top;
                float iwidth = icon.width(), iheight = icon.height();
                switch (iconPos) {
                case LEFT:
                    tx += iwidth + iconGap;
                    iy += valign.offset(iheight, height);
                    usedWidth = iwidth + iconGap;
                    break;
                case ABOVE:
                    ty += iheight + iconGap;
                    ix += halign.offset(iwidth, width);
                    usedHeight = iheight + iconGap;
                    break;
                case RIGHT:
                    ix += width - iwidth;
                    iy += valign.offset(iheight, height);
                    usedWidth = iwidth + iconGap;
                    break;
                case BELOW:
                    iy += height - iheight;
                    ix += halign.offset(iwidth, width);
                    usedHeight = iheight + iconGap;
                    break;
                }
                if (_renderedIcon == icon) {
                    // This is the same icon, just reposition its layer
                    _ilayer.setTranslation(ix,  iy);
                } else {
                    // Otherwise, dispose and recreate
                    if (_ilayer != null) _ilayer.close();
                    layer.addAt(_ilayer = icon.render(), ix, iy);
                }

            } else if (icon == null && _ilayer != null) {
                _ilayer.close();
                _ilayer = null;
            }
            _renderedIcon = icon;

            if (text == null) _tglyph.close();
            else {
                updateTextGlyph(tx, ty, width-usedWidth, height-usedHeight);
                // if we're cuddling, adjust icon position based on the now known tex position
                if (_ilayer != null && iconCuddle) {
                    Layer tlayer = _tglyph.layer();
                    float ctx = (tlayer == null) ? 0 : tlayer.tx();
                    float cty = (tlayer == null) ? 0 : tlayer.ty();
                    float ix = _ilayer.tx(), iy = _ilayer.ty();
                    float iwid = icon.width(), ihei = icon.height();
                    switch (iconPos) {
                    case LEFT:  ix = ctx - iwid - iconGap; break;
                    case ABOVE: iy = cty - ihei - iconGap; break;
                    case RIGHT: ix = ctx + textWidth() + iconGap; break;
                    case BELOW: iy = cty + textHeight() + iconGap; break;
                    }
                    _ilayer.setTranslation(ix, iy);
                }
            }
        }

        @Override public String toString () {
            return "TextLayoutData[text=" + text + ", icon=" + icon + "]";
        }

        // this is broken out so that subclasses can extend this action
        protected void accommodateIcon (Dimension hints, boolean haveText) {
            if (icon != null) {
                // remove the icon space from our hint dimensions
                if (iconPos.horizontal()) {
                    hints.width -= icon.width();
                    if (haveText) hints.width -= iconGap;
                } else {
                    hints.height -= icon.height();
                    if (haveText) hints.height -= iconGap;
                }
            }
        }

        // this is broken out so that subclasses can extend this action
        protected void addTextSize (Dimension size) {
            if (_constraint instanceof Constraints.TextConstraint) {
                Dimension tsize = (text == null) ? null : new Dimension(textWidth(), textHeight());
                ((Constraints.TextConstraint)_constraint).addTextSize(size, tsize);
            } else if (text != null) {
                size.width += textWidth();
                size.height += textHeight();
            }
        }

        // this is broken out so that subclasses can extend this action
        protected void updateTextGlyph (float tx, float ty, float availWidth, float availHeight) {
            float twidth = FloatMath.ceil(textWidth()), theight = FloatMath.ceil(textHeight());
            float awidth = FloatMath.ceil(availWidth), aheight = FloatMath.ceil(availHeight);
            if (twidth <= 0 || theight <= 0 || awidth <= 0 || aheight <= 0) return;

            // if autoShrink is enabled, and our text is too wide, re-lay it out with successively
            // smaller fonts until it fits
            if (autoShrink && twidth > availWidth) {
                while (twidth > availWidth && text.style.font.size > minFontSize) {
                    text = text.resize(text.style.font.size-1);
                    twidth = FloatMath.ceil(textWidth());
                }
                theight = FloatMath.ceil(textHeight());
            }

            // create a canvas no larger than the text, constrained to the available size
            float tgwidth = Math.min(awidth, twidth), tgheight = Math.min(aheight, theight);

            // we do some extra fiddling here because one may want to constrain the height of a
            // button such that the text is actually cut off on the top and/or bottom because fonts
            // may have lots of whitespace above or below and you're trying to squeeze the text
            // snugly into your button
            float ox = MathUtil.ifloor(halign.offset(twidth, awidth));
            float oy = MathUtil.ifloor(valign.offset(theight, aheight));

            // only re-render our text if something actually changed
            if (!text.equals(_renderedText) || tgwidth != _tglyph.preparedWidth() ||
                tgheight != _tglyph.preparedHeight()) {
                _tglyph.prepare(root().iface.plat.graphics(), tgwidth, tgheight);
                Canvas canvas = _tglyph.begin();
                text.render(canvas, Math.min(ox, 0), Math.min(oy, 0));
                _tglyph.end();
                _renderedText = text;
            }

            // always set the translation since other non-text style changes can affect it
            _tglyph.layer().setTranslation(tx + Math.max(ox, 0) + text.style.effect.offsetX(),
                                           ty + Math.max(oy, 0) + text.style.effect.offsetY());
        }

        protected float textWidth () { return text.width(); }
        protected float textHeight () { return text.height(); }
    }

    protected final Glyph _tglyph = new Glyph(layer);
    protected StyledText.Plain _renderedText;
    protected Layer _ilayer;
    protected Icon  _renderedIcon;
}
