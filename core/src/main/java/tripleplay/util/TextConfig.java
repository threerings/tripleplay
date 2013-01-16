//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.ImageLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import static playn.core.PlayN.graphics;

/**
 * Encapsulates a styled text configuration and provides utility methods for rendering it in
 * various ways.
 */
public class TextConfig
{
    public final TextFormat format;
    public final int textColor;
    public final EffectRenderer effect;

    public TextConfig (int textColor) {
        this(new TextFormat(), textColor, EffectRenderer.NONE);
    }

    public TextConfig (TextFormat format, int textColor) {
        this(format, textColor, EffectRenderer.NONE);
    }

    public TextConfig (TextFormat format, int textColor, EffectRenderer effect) {
        this.format = format;
        this.textColor = textColor;
        this.effect = effect;
    }

    /**
     * Returns a new text config which uses the specified text format.
     */
    public TextConfig withFormat (TextFormat format) {
        return new TextConfig(format, textColor, effect);
    }

    /**
     * Returns a new text config with a text format updated to use the specified font.
     */
    public TextConfig withFont (Font font) {
        return new TextConfig(format.withFont(font), textColor, effect);
    }

    /**
     * Returns a new text config which uses the specified text color.
     */
    public TextConfig withColor (int textColor) {
        return new TextConfig(format, textColor, effect);
    }

    /**
     * Returns a new text config which uses a shadow effect.
     */
    public TextConfig withShadow (int shadowColor, float shadowX, float shadowY) {
        return new TextConfig(format, textColor,
                              new EffectRenderer.Shadow(shadowColor, shadowX, shadowY));
    }

    /**
     * Returns a new text config which uses a pixel outline effect.
     */
    public TextConfig withOutline (int outlineColor) {
        return new TextConfig(format, textColor, new EffectRenderer.PixelOutline(outlineColor));
    }

    /**
     * Returns a new text config which uses a vector outline effect.
     */
    public TextConfig withOutline (int outlineColor, float outlineWidth) {
        return new TextConfig(format, textColor,
                              new EffectRenderer.VectorOutline(outlineColor, outlineWidth));
    }

    /**
     * Lays out the supplied text using this config's format.
     */
    public TextLayout layout (String text) {
        return graphics().layoutText(text, format);
    }

    /**
     * Creates a canvas image just big enough to hold the supplied text layout. Note that this
     * method does not render the layout into the image, use {@link #toImage} for that.
     */
    public CanvasImage createImage (TextLayout layout) {
        return graphics().createImage(effect.adjustWidth(layout.width()),
                                      effect.adjustHeight(layout.height()));
    }

    /**
     * Renders the supplied layout into the supplied canvas at the specified coordinates, using
     * this config's text color and effect.
     */
    public void render (Canvas canvas, TextLayout layout, float x, float y) {
        effect.render(canvas, layout, textColor, x, y);
    }

    /**
     * Lays out the supplied text, creates an image large enough to accommodate the text, renders
     * the text into it, and returns the image.
     */
    public CanvasImage toImage (String text) {
        return toImage(layout(text));
    }

    /**
     * Creates an image large enough to accommodate the supplied text layout, renders the text into
     * it, and returns the image.
     */
    public CanvasImage toImage (TextLayout layout) {
        CanvasImage image = createImage(layout);
        render(image.canvas(), layout, 0, 0);
        return image;
    }

    /**
     * Creates an image with the supplied text laid out and rendered into it per this config, and
     * returns an image layer containing the image.
     */
    public ImageLayer toLayer (String text) {
        return toLayer(layout(text));
    }

    /**
     * Creates an image with the supplied text layout rendered into it per this config, and returns
     * an image layer containing the image.
     */
    public ImageLayer toLayer (TextLayout layout) {
        return graphics().createImageLayer(toImage(layout));
    }
}
