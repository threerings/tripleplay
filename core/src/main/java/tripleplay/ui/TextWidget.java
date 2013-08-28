//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.FloatMath;
import pythagoras.f.MathUtil;

import playn.core.Layer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.util.Callback;
import static playn.core.PlayN.graphics;

import react.Slot;
import react.UnitSlot;

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
                    icon.addCallback(new Callback<Icon>() {
                        public void onSuccess (Icon resource) {
                            clearLayoutData();
                            invalidate();
                        }
                        public void onFailure (Throwable err) {} // noop!
                    });
                }
            }
        };
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
        public final boolean iconCuddle = resolveStyle(Style.ICON_CUDDLE);
        public final IconEffect iconEffect = resolveStyle(Style.ICON_EFFECT);
        public final int color = resolveStyle(Style.COLOR);
        public final boolean underlined = resolveStyle(Style.UNDERLINE);
        public final boolean wrap = resolveStyle(Style.TEXT_WRAP);
        public final boolean autoShrink = resolveStyle(Style.AUTO_SHRINK);

        public TextLayout text; // mostly final, only changed by autoShrink
        public final EffectRenderer renderer;
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
                renderer = Style.createEffectRenderer(TextWidget.this);
                TextFormat format = Style.createTextFormat(TextWidget.this);
                if (hints.width > 0 && wrap) format = format.withWrapWidth(hints.width);
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

                // add, then remove to minimize potential texture thrash
                Layer oilayer = _ilayer;
                layer.addAt(_ilayer = icon.render(), ix, iy);
                if (oilayer != null) oilayer.destroy();

            } else if (icon == null && _ilayer != null) {
                _ilayer.destroy();
                _ilayer = null;
            }

            if (text == null) _tglyph.destroy();
            else {
                updateTextGlyph(tx, ty, width-usedWidth, height-usedHeight);
                // if we're cuddling, adjust icon position based on the now known tex position
                if (_ilayer != null && iconCuddle) {
                    float ctx = _tglyph.layer().tx(), cty = _tglyph.layer().ty();
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

        // this is broken out so that subclasses can extend this action
        protected void accommodateIcon (Dimension hints, boolean haveText) {
            if (icon != null) {
                // remove the icon space from our hint dimensions
                switch (iconPos) {
                case LEFT:
                case RIGHT:
                    hints.width -= icon.width();
                    if (haveText) hints.width -= iconGap;
                    break;
                case ABOVE:
                case BELOW:
                    hints.height -= icon.height();
                    if (haveText) hints.height -= iconGap;
                    break;
                }
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
            float twidth = FloatMath.ceil(textWidth()), theight = FloatMath.ceil(textHeight());
            if (twidth <= 0 || theight <= 0 || availWidth <= 0 || availHeight <= 0) return;

            // if autoShrink is enabled, and our text is too wide, re-lay it out with successively
            // smaller fonts until it fits
            if (autoShrink && twidth > availWidth) {
                String curtext = text();
                TextFormat format = Style.createTextFormat(TextWidget.this);
                while (twidth > availWidth && format.font.size() > MIN_FONT_SIZE) {
                    format = format.withFont(format.font.derive(format.font.size()-1));
                    text = graphics().layoutText(curtext, format);
                    twidth = FloatMath.ceil(textWidth());
                }
            }

            // create a canvas no larger than the text, constrained to the available size
            float tgwidth = Math.min(availWidth, twidth), tgheight = Math.min(availHeight, theight);
            _tglyph.prepare(tgwidth, tgheight);

            // we do some extra fiddling here because one may want to constrain the height of a
            // button such that the text is actually cut off on the top and/or bottom because fonts
            // may have lots of whitespace above or below and you're trying to squeeze the text
            // snugly into your button
            float ox = MathUtil.ifloor(halign.offset(twidth, availWidth));
            float oy = MathUtil.ifloor(valign.offset(theight, availHeight));
            renderer.render(_tglyph.canvas(), text, color, underlined,
                Math.min(ox, 0), Math.min(oy, 0));
            _tglyph.layer().setTranslation(tx + Math.max(ox, 0), ty + Math.max(oy, 0));
        }

        protected float textWidth () { return renderer.adjustWidth(text.width()); }
        protected float textHeight () { return renderer.adjustHeight(text.height()); }
    }

    protected final Glyph _tglyph = new Glyph();
    protected Layer _ilayer;

    protected static final float MIN_FONT_SIZE = 6; // TODO: make customizable?
}
