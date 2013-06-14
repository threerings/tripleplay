//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Asserts;
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.ImageLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import static playn.core.PlayN.graphics;
import static pythagoras.f.FloatMath.ceil;

/**
 * Encapsulates a styled text configuration and provides utility methods for rendering it in
 * various ways.
 */
public class TextConfig
{
    public final TextFormat format;
    public final int textColor;
    public final EffectRenderer effect;
    public final boolean underlined;

    public TextConfig (int textColor) {
        this(new TextFormat(), textColor, EffectRenderer.NONE);
    }

    public TextConfig (TextFormat format, int textColor) {
        this(format, textColor, EffectRenderer.NONE);
    }

    public TextConfig (TextFormat format, int textColor, EffectRenderer effect) {
        this(format, textColor, effect, false);
    }

    public TextConfig (TextFormat format, int textColor, EffectRenderer effect,
        boolean underlined) {
        this.format = Asserts.checkNotNull(format);
        this.textColor = textColor;
        this.effect = Asserts.checkNotNull(effect);
        this.underlined = underlined;
    }

    @Override public boolean equals (Object other) {
        if (!(other instanceof TextConfig)) return false;

        TextConfig that = (TextConfig)other;
        return format.equals(that.format) && effect.equals(that.effect) &&
            underlined == that.underlined && textColor == that.textColor;
    }

    @Override public int hashCode () {
        return format.hashCode() ^ effect.hashCode() ^ (underlined ? 1 : 0) ^ textColor;
    }

    /**
     * Returns a new text config which uses the specified text format.
     */
    public TextConfig withFormat (TextFormat format) {
        return new TextConfig(format, textColor, effect, underlined);
    }

    /**
     * Returns a new text config updated to use the specified font.
     */
    public TextConfig withFont (Font font) {
        return new TextConfig(format.withFont(font), textColor, effect, underlined);
    }

    /**
     * Returns a new text config with the wrap width and alignment configured as specified.
     */
    public TextConfig withWrapping (float wrapWidth, TextFormat.Alignment align) {
        return new TextConfig(format.withWrapping(wrapWidth, align), textColor, effect, underlined);
    }

    /**
     * Returns a new text config with the wrap width configured as specified.
     */
    public TextConfig withWrapWidth (float wrapWidth) {
        return new TextConfig(format.withWrapWidth(wrapWidth), textColor, effect, underlined);
    }

    /**
     * Returns a new text config the alignment configured as specified.
     */
    public TextConfig withAlignment (TextFormat.Alignment align) {
        return new TextConfig(format.withAlignment(align), textColor, effect, underlined);
    }

    /**
     * Returns a new text config which uses the specified text color.
     */
    public TextConfig withColor (int textColor) {
        return new TextConfig(format, textColor, effect, underlined);
    }

    /**
     * Returns a new text config which uses a shadow effect.
     */
    public TextConfig withShadow (int shadowColor, float shadowX, float shadowY) {
        return new TextConfig(format, textColor,
                              new EffectRenderer.Shadow(shadowColor, shadowX, shadowY), underlined);
    }

    /**
     * Returns a new text config which uses a pixel outline effect.
     */
    public TextConfig withOutline (int outlineColor) {
        return new TextConfig(format, textColor,
                              new EffectRenderer.PixelOutline(outlineColor), underlined);
    }

    /**
     * Returns a new text config which uses a vector outline effect.
     */
    public TextConfig withOutline (int outlineColor, float outlineWidth) {
        return new TextConfig(format, textColor,
                              new EffectRenderer.VectorOutline(outlineColor, outlineWidth),
                              underlined);
    }

    public TextConfig withUnderline (boolean underlined) {
        return new TextConfig(format, textColor, effect, underlined);
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
        return graphics().createImage(effect.adjustWidth(ceil(layout.width())),
                                      effect.adjustHeight(ceil(layout.height())));
    }

    /**
     * Renders the supplied layout into the supplied canvas at the specified coordinates, using
     * this config's text color and effect.
     */
    public void render (Canvas canvas, TextLayout layout, float x, float y) {
        effect.render(canvas, layout, textColor, underlined, x, y);
    }

    /**
     * Renders the supplied layout into the supplied canvas at the specified coordinates, using
     * this config's text color and effect. The text will be centered on the specified x
     * coordinate.
     */
    public void renderCX (Canvas canvas, TextLayout layout, float x, float y) {
        float width = effect.adjustWidth(layout.width());
        effect.render(canvas, layout, textColor, underlined, Math.round(x - width/2), y);
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
