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
    public static final EffectRenderer NONE = new NoEffect(false);

    public float adjustWidth (float width) { return width; }
    public float adjustHeight (float height) { return height; }

    public boolean underlined () { return _underlined; }

    public abstract void render (Canvas canvas, TextLayout layout, int textColor, float x, float y);

    public static class NoEffect extends EffectRenderer {
        public NoEffect (boolean underlined) {
            _underlined = underlined;
        }

        public void render (Canvas canvas, TextLayout layout, int textColor, float x, float y) {
            canvas.save();
            canvas.setFillColor(textColor);
            canvas.fillText(layout, x, y);
            if (_underlined) {
                canvas.setStrokeColor(textColor);
                for (int ii = 0; ii < layout.lineCount(); ii++) {
                    Rectangle bounds = layout.lineBounds(ii);
                    float sx = x + bounds.x;
                    float sy = y + bounds.y + layout.ascent();
                    canvas.drawLine(sx, sy, sx + layout.width(), sy);
                }
            }
            canvas.restore();
        }
    }

    public static class PixelOutline extends EffectRenderer {
        public final int outlineColor;

        public PixelOutline (int outlineColor, boolean underlined) {
            this.outlineColor = outlineColor;
            _underlined = underlined;
        }

        public float adjustWidth (float width) { return width + 2; }
        public float adjustHeight (float height) { return height + 2; }

        public void render (Canvas canvas, TextLayout text, int textColor, float x, float y) {
            canvas.save();
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
    }

    public static class VectorOutline extends EffectRenderer {
        public final int outlineColor;
        public final float outlineWidth;
        public final Canvas.LineCap outlineCap;
        public final Canvas.LineJoin outlineJoin;

        public VectorOutline (int outlineColor, float outlineWidth, boolean underlined) {
            this(outlineColor, outlineWidth, Canvas.LineCap.ROUND, Canvas.LineJoin.ROUND,
                underlined);
        }

        public VectorOutline (int outlineColor, float outlineWidth,
                              Canvas.LineCap cap, Canvas.LineJoin join, boolean underlined) {
            this.outlineColor = outlineColor;
            this.outlineWidth = outlineWidth;
            this.outlineCap = cap;
            this.outlineJoin = join;
            _underlined = underlined;
        }

        public float adjustWidth (float width) { return width + 2*outlineWidth; }
        public float adjustHeight (float height) { return height + 2*outlineWidth; }

        public void render (Canvas canvas, TextLayout text, int textColor, float x, float y) {
            canvas.save();
            canvas.setStrokeColor(outlineColor);
            canvas.setStrokeWidth(outlineWidth*2);
            canvas.setLineCap(outlineCap);
            canvas.setLineJoin(outlineJoin);
            canvas.strokeText(text, x+outlineWidth, y+outlineWidth);
            canvas.setFillColor(textColor);
            canvas.fillText(text, x+outlineWidth, y+outlineWidth);
            canvas.restore();
        }
    }

    public static class Shadow extends EffectRenderer {
        public final int shadowColor;
        public final float shadowX, shadowY;

        public Shadow (int shadowColor, float shadowX, float shadowY, boolean underlined) {
            this.shadowColor = shadowColor;
            this.shadowX = shadowX;
            this.shadowY = shadowY;
            _underlined = underlined;
        }

        public float adjustWidth (float width) { return width + Math.abs(shadowX); }
        public float adjustHeight (float height) { return height + Math.abs(shadowY); }

        public void render (Canvas canvas, TextLayout text, int textColor, float x, float y) {
            float tx = (shadowX < 0) ? -shadowX : 0, ty = (shadowY < 0) ? -shadowY : 0;
            float sx = (shadowX < 0) ? 0 : shadowX, sy = (shadowY < 0) ? 0 : shadowY;
            canvas.save();
            canvas.setFillColor(shadowColor);
            canvas.fillText(text, x+sx, y+sy);
            canvas.setFillColor(textColor);
            canvas.fillText(text, x+tx, y+ty);
            canvas.restore();
        }
    }

    protected boolean _underlined;
}
