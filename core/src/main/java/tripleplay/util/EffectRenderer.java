//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import pythagoras.f.IRectangle;

import playn.core.Canvas;
import playn.core.PlayN;
import playn.core.TextLayout;

import tripleplay.ui.Style;

/**
 * Handles the rendering of text with a particular effect (shadow, outline, etc.).
 */
public abstract class EffectRenderer
{
    /** An "effect" that just renders the text normally. */
    public static final EffectRenderer NONE = new EffectRenderer() {
        @Override public void render (Canvas canvas, TextLayout layout, int textColor,
            boolean underlined, float x, float y) {
            canvas.save();
            canvas.setFillColor(textColor);
            if (underlined) {
                IRectangle bounds = layout.bounds();
                float sx = x + bounds.x(), sy = y + bounds.y() + bounds.height() + 1;
                canvas.fillRect(sx, sy, bounds.width(), 1);
            }
            canvas.fillText(layout, x, y);
            canvas.restore();
        }
    };

    public float adjustWidth (float width) { return width; }
    public float adjustHeight (float height) { return height; }
    public float offsetX () { return 0; }
    public float offsetY () { return 0; }

    public abstract void render (Canvas canvas, TextLayout layout, int textColor,
        boolean underlined, float x, float y);

    public static class PixelOutline extends EffectRenderer {
        public final int outlineColor;

        public PixelOutline (int outlineColor) {
            this.outlineColor = outlineColor;
        }

        @Override public float adjustWidth (float width) { return width + 2; }
        @Override public float adjustHeight (float height) { return height + 2; }

        @Override public void render (Canvas canvas, TextLayout text, int textColor,
            boolean underlined, float x, float y) {
            canvas.save();
            if (underlined) {
                IRectangle bounds = text.bounds();
                float sx = x + bounds.x() + 1, sy = y + bounds.y() + bounds.height() + 2;
                canvas.setFillColor(outlineColor).fillRect(sx-1, sy-1, bounds.width()+3, 3);
                canvas.setFillColor(textColor).fillRect(sx, sy, bounds.width(), 1);
            }
            canvas.setFillColor(outlineColor);
            canvas.fillText(text, x+0, y+0);
            canvas.fillText(text, x+0, y+1);
            canvas.fillText(text, x+0, y+2);
            canvas.fillText(text, x+1, y+0);
            canvas.fillText(text, x+1, y+2);
            canvas.fillText(text, x+2, y+0);
            canvas.fillText(text, x+2, y+1);
            canvas.fillText(text, x+2, y+2);
            canvas.setFillColor(textColor);
            canvas.fillText(text, x+1, y+1);
            canvas.restore();
        }

        @Override public boolean equals (Object obj) {
            if (!(obj instanceof PixelOutline)) return false;
            return outlineColor == ((PixelOutline)obj).outlineColor;
        }

        @Override public int hashCode () {
            return outlineColor;
        }
    }

    public static class VectorOutline extends EffectRenderer {
        public final int outlineColor;
        public final float outlineWidth;
        public final Canvas.LineCap outlineCap;
        public final Canvas.LineJoin outlineJoin;

        public VectorOutline (int outlineColor, float outlineWidth) {
            this(outlineColor, outlineWidth, Canvas.LineCap.ROUND, Canvas.LineJoin.ROUND);
        }

        public VectorOutline (int outlineColor, float outlineWidth,
                              Canvas.LineCap cap, Canvas.LineJoin join) {
            this.outlineColor = outlineColor;
            this.outlineWidth = outlineWidth;
            this.outlineCap = cap;
            this.outlineJoin = join;
        }

        @Override public float adjustWidth (float width) { return width + 2*outlineWidth; }
        @Override public float adjustHeight (float height) { return height + 2*outlineWidth; }

        @Override public void render (Canvas canvas, TextLayout text, int textColor,
            boolean underlined, float x, float y) {
            canvas.save();
            canvas.setStrokeColor(outlineColor);
            canvas.setStrokeWidth(outlineWidth*2);
            canvas.setLineCap(outlineCap);
            canvas.setLineJoin(outlineJoin);
            canvas.strokeText(text, x+outlineWidth, y+outlineWidth);
            canvas.setFillColor(textColor);
            canvas.fillText(text, x+outlineWidth, y+outlineWidth);
            if (underlined) {
                IRectangle bounds = text.bounds();
                float sx = x + bounds.x() + outlineWidth;
                float sy = y + bounds.y() + bounds.height() + outlineWidth + 1;
                canvas.fillRect(sx, sy, bounds.width(), 1);
            }
            canvas.restore();
        }

        @Override public boolean equals (Object obj) {
            if (!(obj instanceof VectorOutline)) return false;
            VectorOutline that = (VectorOutline)obj;
            return outlineColor == that.outlineColor && outlineWidth == that.outlineWidth &&
                    outlineCap == that.outlineCap && outlineJoin == that.outlineJoin;
        }

        @Override public int hashCode () {
            return outlineColor ^ (int)outlineWidth ^ outlineCap.hashCode() ^
                    outlineJoin.hashCode();
        }
    }

    public static class Shadow extends EffectRenderer {
        public final int shadowColor;
        public final float shadowX, shadowY;

        public Shadow (int shadowColor, float shadowX, float shadowY) {
            this.shadowColor = shadowColor;
            this.shadowX = shadowX;
            this.shadowY = shadowY;
        }

        @Override public float adjustWidth (float width) { return width + Math.abs(shadowX); }
        @Override public float adjustHeight (float height) { return height + Math.abs(shadowY); }

        @Override public void render (Canvas canvas, TextLayout text, int textColor,
            boolean underlined, float x, float y) {
            float tx = (shadowX < 0) ? -shadowX : 0, ty = (shadowY < 0) ? -shadowY : 0;
            float sx = (shadowX < 0) ? 0 : shadowX, sy = (shadowY < 0) ? 0 : shadowY;
            canvas.save();
            if (underlined) {
                IRectangle bounds = text.bounds();
                canvas.setFillColor(shadowColor).fillRect(
                    sx+bounds.x()+x, sy+bounds.y()+bounds.height()+1, bounds.width()+1, 1);
                canvas.setFillColor(textColor).fillRect(
                    tx+bounds.x()+x, ty+bounds.y()+bounds.height()+1, bounds.width()+1, 1);
            }
            canvas.setFillColor(shadowColor);
            canvas.fillText(text, x+sx, y+sy);
            canvas.setFillColor(textColor);
            canvas.fillText(text, x+tx, y+ty);
            canvas.restore();
        }

        @Override public boolean equals (Object obj) {
            if (!(obj instanceof Shadow)) return false;
            Shadow that = (Shadow)obj;
            return shadowColor == that.shadowColor &&
                    shadowX == that.shadowX && shadowY == that.shadowY;
        }

        @Override public int hashCode () {
            return shadowColor ^ (int)shadowX ^ (int)shadowY;
        }
    }

    public static class Gradient extends EffectRenderer {
        public final int gradientColor;
        public final Style.GradientType gradientType;

        public Gradient (int gradientColor, Style.GradientType gradientType) {
            this.gradientColor = gradientColor;
            this.gradientType = gradientType;
        }

        @Override public void render (Canvas canvas, TextLayout text, int textColor,
            boolean underlined, float x, float y) {

            int colors[] = null;
            float positions[] = null;

            switch (gradientType) {
            case BOTTOM:
                colors = new int[] {textColor, gradientColor};
                positions = new float[] {0, 1};
                break;
            case TOP:
                colors = new int[] {gradientColor, textColor};
                positions = new float[] {0, 1};
                break;
            case CENTER:
                colors = new int[] {textColor, gradientColor, textColor};
                positions = new float[] {0, 0.5f, 1};
                break;
            }

            // The compiler should've warned if new values showed up in the enum, but sanity check
            assert colors != null : "Unhandled gradient type: " + gradientType;

            canvas.save();

            canvas.setFillGradient(
                PlayN.graphics().createLinearGradient(0, 0, 0, text.height(), colors, positions));
            canvas.fillText(text, x, y);

            if (underlined) {
                IRectangle bounds = text.bounds();
                float sx = x + bounds.x(), sy = y + bounds.y() + bounds.height() + 1;
                canvas.fillRect(sx, sy, bounds.width(), 1);
            }

            canvas.restore();
        }

        @Override public boolean equals (Object obj) {
            if (!(obj instanceof Gradient)) return false;
            Gradient that = (Gradient)obj;
            return gradientColor == that.gradientColor && gradientType == that.gradientType;
        }

        @Override public int hashCode () {
            return (83 * gradientColor) ^ (113 * gradientType.ordinal());
        }
    }
}
