//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Canvas;
import playn.core.TextLayout;

/**
 * Handles the rendering of text with a particular effect (shadow, outline, etc.).
 */
public abstract class EffectRenderer
{
    /** An "effect" that just renders the text normally. */
    public static final EffectRenderer NONE = new EffectRenderer() {
        public void render (Canvas canvas, TextLayout layout, int textColor, float x, float y) {
            canvas.setFillColor(textColor);
            canvas.fillText(layout, x, y);
        }
    };

    public float adjustWidth (float width) { return width; }
    public float adjustHeight (float height) { return height; }

    public abstract void render (Canvas canvas, TextLayout layout, int textColor, float x, float y);

    public static class PixelOutline extends EffectRenderer {
        public final int outlineColor;

        public PixelOutline (int outlineColor) {
            this.outlineColor = outlineColor;
        }

        public float adjustWidth (float width) { return width + 2; }
        public float adjustHeight (float height) { return height + 2; }

        public void render (Canvas canvas, TextLayout text, int textColor, float x, float y) {
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

        public void render (Canvas canvas, TextLayout text, int textColor, float x, float y) {
            canvas.setStrokeColor(outlineColor);
            canvas.setStrokeWidth(outlineWidth*2);
            canvas.setLineCap(outlineCap);
            canvas.setLineJoin(outlineJoin);
            canvas.strokeText(text, x+outlineWidth, y+outlineWidth);
            canvas.setFillColor(textColor);
            canvas.fillText(text, x+outlineWidth, y+outlineWidth);
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

        public void render (Canvas canvas, TextLayout text, int textColor, float x, float y) {
            float tx = (shadowX < 0) ? -shadowX : 0, ty = (shadowY < 0) ? -shadowY : 0;
            float sx = (shadowX < 0) ? 0 : shadowX, sy = (shadowY < 0) ? 0 : shadowY;
            canvas.setFillColor(shadowColor);
            canvas.fillText(text, x+sx, y+sy);
            canvas.setFillColor(textColor);
            canvas.fillText(text, x+tx, y+ty);
        }
    }
}
