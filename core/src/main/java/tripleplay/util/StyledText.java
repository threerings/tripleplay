//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import pythagoras.f.Rectangle;

import playn.core.Canvas;
import playn.core.Graphics;
import playn.core.TextBlock;
import playn.core.TextLayout;
import playn.core.TextWrap;
import playn.scene.ImageLayer;

/**
 * Manages styled text. This comes in many flavors: a single line of plain (uniformly styled) text,
 * multiple lines of plain text, and (coming soon) multiple lines of rich (non-uniformly styled)
 * text.
 */
public abstract class StyledText
{
    /** A shared base class for single- and multi-line plain text. */
    public static abstract class Plain extends StyledText {
        /** The text being rendered. */
        public final String text;
        /** The stylings applied to this text. */
        public final TextStyle style;

        protected Plain (Graphics gfx, String text, TextStyle style) {
            super(gfx);
            assert text != null && style != null;
            this.text = text;
            this.style = style;
        }

        /** Creates a new instance equivalent to this one excepting that the font size is adjusted
         * to {@code size}. This is useful for auto-shrinking text to fit into fixed space. */
        public abstract Plain resize (float size);

        @Override public ImageLayer toLayer (ImageLayer target) {
            Canvas canvas = toCanvas();
            target.setTile(canvas.toTexture());
            target.setTranslation(style.effect.offsetX(), style.effect.offsetY());
            return target;
        }

        @Override public int hashCode () {
            return text.hashCode() ^ style.hashCode();
        }

        @Override public boolean equals (Object other) {
            if (other instanceof Plain) {
                Plain op = (Plain)other;
                return text.equals(op.text) && style.equals(op.style);
            } else return false;
        }
    }

    /** A single line of plain (uniformly styled) text. */
    public static class Span extends Plain {
        public Span (Graphics gfx, String text, TextStyle style) {
            super(gfx, text, style);
            _layout = gfx.layoutText(text, style);
        }

        @Override public float width () {
            return style.effect.adjustWidth(_layout.size.width());
        }
        @Override public float height () {
            return style.effect.adjustHeight(_layout.size.height());
        }

        @Override public void render (Canvas canvas, float x, float y) {
            style.effect.render(canvas, _layout, style.textColor, style.underlined, x, y);
        }

        @Override public Span resize (float size) {
            return new Span(_gfx, text, style.withFont(style.font.derive(size)));
        }

        @Override public boolean equals (Object other) {
            return (other instanceof Span) && super.equals(other);
        }

        @Override public String toString () {
            return "Span '" + text + "' @ " + style;
        }

        protected final TextLayout _layout;
    }

    /** Multiple lines of plain (uniformly styled) text. */
    public static class Block extends Plain {
        /** The text wrap configuration, unused if not wrapping. */
        public final TextWrap wrap;

        /** The alignment of wrapped text, unused if not wrapping. */
        public final TextBlock.Align align;

        /** Additional space to be added (or subtracted) from the default line spacing. */
        public final float lineSpacing;

        public Block (Graphics gfx, String text, TextStyle style, TextWrap wrap,
                      TextBlock.Align align, float lineSpacing) {
            super(gfx, text, style);
            assert wrap != null && align != null;
            this.wrap = wrap;
            this.align = align;
            this.lineSpacing = lineSpacing;
            _layouts = gfx.layoutText(text, style, wrap);
            _bounds = TextBlock.getBounds(_layouts, new Rectangle());
            _bounds.height += lineSpacing*(_layouts.length-1);
            _bounds.width = style.effect.adjustWidth(_bounds.width);
            _bounds.height = style.effect.adjustHeight(_bounds.height);
        }

        @Override public float width () {
            return _bounds.width;
        }
        @Override public float height () {
            return _bounds.height;
        }

        @Override public void render (Canvas canvas, float x, float y) {
            float bx = _bounds.x, ly = y + _bounds.y;
            for (TextLayout layout : _layouts) {
                float lx = x + bx + align.getX(style.effect.adjustWidth(layout.size.width()),
                                               _bounds.width-_bounds.x);
                style.effect.render(canvas, layout, style.textColor, style.underlined, lx, ly);
                ly += layout.ascent() + layout.descent() + layout.leading() + this.lineSpacing;
            }
        }

        @Override public Block resize (float size) {
            return new Block(_gfx, text, style.withFont(style.font.derive(size)),
                             wrap, align, lineSpacing);
        }

        @Override public int hashCode () {
            return super.hashCode() ^ wrap.hashCode() ^ align.hashCode();
        }

        @Override public boolean equals (Object other) {
            if (other instanceof Plain) {
                Plain op = (Plain)other;
                return text.equals(op.text) && style.equals(op.style);
            } else return false;
        }

        @Override public String toString () {
            return "Block '" + text + "' @ " + style + "/" + wrap + "/" + align;
        }

        protected final TextLayout[] _layouts;
        protected final Rectangle _bounds;
    }

    /** Creates a uniformly formatted single-line of text. */
    public static Span span (Graphics gfx, String text, TextStyle style) {
        return new Span(gfx, text, style);
    }

    /** Creates a uniformly formatted multiple-lines of text wrapped at {@code wrapWidth} and
     * left-aligned. */
    public static Block block (Graphics gfx, String text, TextStyle style, float wrapWidth) {
        return new Block(gfx, text, style, new TextWrap(wrapWidth), TextBlock.Align.LEFT, 0);
    }

    /** The width of this styled text when rendered. */
    public abstract float width ();

    /** The height of this styled text when rendered. */
    public abstract float height ();

    /** Renders this styled text into the supplied canvas at the specified offset. */
    public abstract void render (Canvas canvas, float x, float y);

    /** Creates a canvas large enough to accommodate this styled text, and renders it therein. The
      * canvas will include a one pixel border beyond the size of the styled text which is needed
      * to accommodate antialiasing. */
    public Canvas toCanvas () {
        float pad = 1/_gfx.scale().factor;
        Canvas canvas = _gfx.createCanvas(width()+2*pad, height()+2*pad);
        render(canvas, pad, pad);
        return canvas;
    }

    /** Creates an image large enough to accommodate this styled text, renders it therein and
      * returns an image layer with its translation adjusted per the effect renderer. */
    public ImageLayer toLayer () { return toLayer(new ImageLayer()); }

    /** Creates an image large enough to accommodate this styled text, renders it therein and
      * applies it to {@code layer}, adjusting its translation per the effect renderer. */
    public abstract ImageLayer toLayer (ImageLayer target);

    protected StyledText (Graphics gfx) {
        assert gfx != null;
        _gfx = gfx;
    }

    protected final Graphics _gfx;
}
