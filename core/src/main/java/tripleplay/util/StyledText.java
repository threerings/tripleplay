//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import pythagoras.f.Rectangle;

import playn.core.Asserts;
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.ImageLayer;
import playn.core.TextLayout;
import playn.core.TextWrap;
import playn.core.util.TextBlock;
import static playn.core.PlayN.graphics;

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

        protected Plain (String text, TextStyle style) {
            this.text = Asserts.checkNotNull(text);
            this.style = Asserts.checkNotNull(style);
        }

        /** Creates a new instance equivalent to this one excepting that the font size is adjusted
         * to {@code size}. This is useful for auto-shrinking text to fit into fixed space. */
        public abstract Plain resize (float size);

        @Override public ImageLayer toLayer () {
            ImageLayer layer = graphics().createImageLayer(toImage());
            layer.setTranslation(style.effect.offsetX(), style.effect.offsetY());
            return layer;
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
        public Span (String text, TextStyle style) {
            super(text, style);
            _layout = graphics().layoutText(text, style);
        }

        @Override public float width () {
            return style.effect.adjustWidth(_layout.width()) + 2*TextBlock.pad();
        }
        @Override public float height () {
            return style.effect.adjustHeight(_layout.height()) + 2*TextBlock.pad();
        }

        @Override public void render (Canvas canvas, float x, float y) {
            float pad = TextBlock.pad();
            style.effect.render(canvas, _layout, style.textColor, style.underlined, x+pad, y+pad);
        }

        @Override public Span resize (float size) {
            return new Span(text, style.withFont(style.font.derive(size)));
        }

        @Override public boolean equals (Object other) {
            return (other instanceof Span) && super.equals(other);
        }

        protected final TextLayout _layout;
    }

    /** Multiple lines of plain (uniformly styled) text. */
    public static class Block extends Plain {
        /** The text wrap configuration, unused if not wrapping. */
        public final TextWrap wrap;

        /** The alignment of wrapped text, unused if not wrapping. */
        public final TextBlock.Align align;

        public Block (String text, TextStyle style, TextWrap wrap, TextBlock.Align align) {
            super(text, style);
            this.wrap = Asserts.checkNotNull(wrap);
            this.align = Asserts.checkNotNull(align);
            _layouts = graphics().layoutText(text, style, wrap);
            _bounds = TextBlock.getBounds(_layouts, new Rectangle());
            _bounds.width = style.effect.adjustWidth(_bounds.width);
            _bounds.height = style.effect.adjustWidth(_bounds.height);
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
                float lx = x + bx + align.getX(style.effect.adjustWidth(layout.width()),
                                               _bounds.width-_bounds.x);
                style.effect.render(canvas, layout, style.textColor, style.underlined, lx, ly);
                ly += layout.ascent() + layout.descent() + layout.leading();
            }
        }

        @Override public Block resize (float size) {
            return new Block(text, style.withFont(style.font.derive(size)), wrap, align);
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

        protected final TextLayout[] _layouts;
        protected final Rectangle _bounds;
    }

    /** Creates a uniformly formatted single-line of text. */
    public static Span span (String text, TextStyle style) {
        return new Span(text, style);
    }

    /** Creates a uniformly formatted multiple-lines of text wrapped at {@code wrapWidth} and
     * left-aligned. */
    public static Block block (String text, TextStyle style, float wrapWidth) {
        return new Block(text, style, new TextWrap(wrapWidth), TextBlock.Align.LEFT);
    }

    /** The width of this styled text when rendered. */
    public abstract float width ();

    /** The height of this styled text when rendered. */
    public abstract float height ();

    /** Renders this styled text into the supplied canvas at the specified offset. */
    public abstract void render (Canvas canvas, float x, float y);

    /** Creates an image large enough to accommodate this styled text, and renders it therein. */
    public CanvasImage toImage () {
        CanvasImage image = graphics().createImage(width(), height());
        render(image.canvas(), 0, 0);
        return image;
    }

    /** Creates an image large enough to accommodate this styled text, renders it therein and
     * returns an image layer with its translation adjusted per the effect renderer. */
    public abstract ImageLayer toLayer ();
}
