//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Canvas;
import playn.core.TextLayout;
import pythagoras.f.Rectangle;

/**
 * Handles the rendering of text with a particular effect (shadow, outline, etc.).
 */
public abstract class EffectRenderer
{
    /** An "effect" that just renders the text normally. */
    public static final EffectRenderer NONE = new EffectRenderer() {
        public void render (Canvas canvas, TextLayout layout, int textColor, boolean underlined,
            float x, float y) {
            canvas.save();
            canvas.setFillColor(textColor);
            if (underlined) {
                canvas.setStrokeColor(textColor);
                for (int ii = 0; ii < layout.lineCount(); ii++) {
                    Rectangle bounds = layout.lineBounds(ii);
                    float sx = x + bounds.x;
                    float sy = y + bounds.y + bounds.height() - 1;
                    canvas.fillRect(sx, sy, bounds.width(), 1);
                }
            }
            canvas.fillText(layout, x, y);
            canvas.restore();
        }
    };

    public float adjustWidth (float width) { return width; }
    public float adjustHeight (float height) { return height; }

    public abstract void render (Canvas canvas, TextLayout layout, int textColor,
        boolean underlined, float x, float y);

    public static class PixelOutline extends EffectRenderer {
        public final int outlineColor;

        public PixelOutline (int outlineColor) {
            this.outlineColor = outlineColor;
        }

        public float adjustWidth (float width) { return width + 2; }
        public float adjustHeight (float height) { return height + 2; }

        public void render (Canvas canvas, TextLayout text, int textColor, boolean underlined,
            float x, float y) {
            canvas.save();
            if (underlined) {
                for (int ii = 0; ii < text.lineCount(); ii++) {
                    Rectangle bounds = text.lineBounds(ii);
                    float sx = x + bounds.x + 1;
                    float sy = y + bounds.y + bounds.height();
                    canvas.setFillColor(outlineColor);
                    canvas.fillRect(sx-1, sy-1, bounds.width()+3, 3);
                    canvas.setFillColor(textColor);
                    canvas.fillRect(sx, sy, bounds.width(), 1);
                }
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

        public float adjustWidth (float width) { return width + 2*outlineWidth; }
        public float adjustHeight (float height) { return height + 2*outlineWidth; }

        public void render (Canvas canvas, TextLayout text, int textColor, boolean underlined,
            float x, float y) {
            canvas.save();
            if (underlined) {
                for (int ii = 0; ii < text.lineCount(); ii++) {
                    Rectangle bounds = text.lineBounds(ii);
                    float sx = x + bounds.x + outlineWidth;
                    float sy = y + bounds.y + bounds.height() - 1 + outlineWidth;
                    canvas.drawLine(sx, sy, sx+bounds.width(), sy);
                    canvas.setFillColor(textColor);
                    canvas.fillRect(sx, sy, bounds.width(), 1);
                }
            }
            canvas.setStrokeColor(outlineColor);
            canvas.setStrokeWidth(outlineWidth*2);
            canvas.setLineCap(outlineCap);
            canvas.setLineJoin(outlineJoin);
            canvas.strokeText(text, x+outlineWidth, y+outlineWidth);
            canvas.setFillColor(textColor);
            canvas.fillText(text, x+outlineWidth, y+outlineWidth);
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

        public float adjustWidth (float width) { return width + Math.abs(shadowX); }
        public float adjustHeight (float height) { return height + Math.abs(shadowY); }

        public void render (Canvas canvas, TextLayout text, int textColor, boolean underlined,
            float x, float y) {
            float tx = (shadowX < 0) ? -shadowX : 0, ty = (shadowY < 0) ? -shadowY : 0;
            float sx = (shadowX < 0) ? 0 : shadowX, sy = (shadowY < 0) ? 0 : shadowY;
            canvas.save();
            if (underlined) {
                for (int ii = 0; ii < text.lineCount(); ii++) {
                    Rectangle bounds = text.lineBounds(ii);
                    canvas.setFillColor(shadowColor);
                    canvas.fillRect(sx+bounds.x+x, sy+bounds.y+bounds.height()-1,
                        bounds.width()+1, 1);
                    canvas.setFillColor(textColor);
                    canvas.fillRect(tx+bounds.x+x, ty+bounds.y+bounds.height()-1,
                        bounds.width()+1, 1);
                }
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
}
