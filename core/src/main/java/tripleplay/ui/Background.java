//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.Layer;
import playn.core.Surface;
import static playn.core.PlayN.graphics;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

import tripleplay.ui.bgs.BeveledBackground;
import tripleplay.ui.bgs.BlankBackground;
import tripleplay.ui.bgs.BorderedBackground;
import tripleplay.ui.bgs.ImageBackground;
import tripleplay.ui.bgs.RoundRectBackground;
import tripleplay.ui.bgs.Scale9Background;
import tripleplay.ui.bgs.SolidBackground;

/**
 * A background is responsible for rendering a border and a fill. It is used in conjunction with
 * groups and buttons and any other elements that need a background.
 */
public abstract class Background
{
    /** The (highest) depth at which background layers are rendered. May range from (-11, 10]. */
    public static final float BACKGROUND_DEPTH = -10f;

    /**
     * Creates a null background (transparent).
     */
    public static Background blank () {
        return new BlankBackground();
    }

    /**
     * Creates a solid background of the specified color.
     */
    public static Background solid (int color) {
        return new SolidBackground(color);
    }

    /**
     * Creates a beveled background with the specified colors.
     */
    public static Background beveled (int bgColor, int ulColor, int brColor) {
        return new BeveledBackground(bgColor, ulColor, brColor);
    }

    /**
     * Creates a bordered background with the specified colors and thickness.
     */
    public static Background bordered (int bgColor, int color, float thickness) {
        return new BorderedBackground(bgColor, color, thickness);
    }

    /**
     * Creates a round rect background with the specified color and corner radius.
     */
    public static Background roundRect (int bgColor, float cornerRadius) {
        return new RoundRectBackground(bgColor, cornerRadius);
    }

    /**
     * Creates a round rect background with the specified colors, border width and corner radius.
     */
    public static Background roundRect (int bgColor, float cornerRadius,
                                        int borderColor, float borderWidth) {
        return new RoundRectBackground(bgColor, cornerRadius, borderColor, borderWidth);
    }

    /**
     * Creates an image background with the specified image.
     */
    public static Background image (Image bgimage) {
        return new ImageBackground(bgimage);
    }

    /**
     * Creates a scale9 background with the specified image. See {@link Scale9Background}.
     */
    public static Background scale9 (Image scale9Image) {
        return new Scale9Background(scale9Image);
    }

    /** The insets of this background. */
    public float top, right, bottom, left;

    /** The alpha transparency of this background (or null if no alpha has been configured). */
    public Float alpha;

    /** Returns this background's adjustment to an element's width. */
    public float width () {
        return left + right;
    }

    /** Returns this background's adjustment to an element's height. */
    public float height () {
        return top + bottom;
    }

    /**
     * Configures uniform insets on this background.
     */
    public Background inset (float uniformInset) {
        this.top = uniformInset;
        this.right = uniformInset;
        this.bottom = uniformInset;
        this.left = uniformInset;
        return this;
    }

    /**
     * Configures horizontal and vertical insets on this background.
     */
    public Background inset (float horiz, float vert) {
        this.top = vert;
        this.right = horiz;
        this.bottom = vert;
        this.left = horiz;
        return this;
    }

    /**
     * Configures non-uniform insets on this background.
     */
    public Background inset (float top, float right, float bottom, float left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
        return this;
    }

    /**
     * Configures the alpha transparency of this background.
     */
    public Background alpha (float alpha) {
        this.alpha = alpha;
        return this;
    }

    /**
     * Adds this background's insets to the supplied dimensions. Returns {@code size} for chaining.
     */
    Dimension addInsets (Dimension size) {
        size.width += width();
        size.height += height();
        return size;
    }

    /**
     * Instantiates a delegate background at the supplied size. This allows one to make a composite
     * background which consists of multiple backgrounds arranged in concert.
     */
    protected Instance instantiate (Background delegate, IDimension size) {
        return delegate.instantiate(size);
    }

    /**
     * Instantiates this background using the supplied widget size. The supplied size should
     * include the insets defined for this background.
     */
    protected abstract Instance instantiate (IDimension size);

    protected Layer createSolidLayer (final int color, final float width, final float height) {
        return graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            public void render (Surface surf) {
                if (alpha != null) surf.setAlpha(alpha);
                surf.setFillColor(color).fillRect(0, 0, width, height);
                if (alpha != null) surf.setAlpha(1);
            }
        });
    }

    protected Layer createTiledLayer (Image image, float width, float height) {
        ImageLayer layer = graphics().createImageLayer(image);
        layer.setRepeatX(true);
        layer.setRepeatY(true);
        if (alpha != null) layer.setAlpha(alpha);
        return layer;
    }

    /** An instantiation of a particular background template. Backgrounds are configured as a style
     * property; elements instantiate them at specific dimensions when they are actually used.*/
    protected static abstract class Instance {
        /** Adds this background's layers to the specified group at the specified x/y offset.
         * @param depthAdjust an adjustment to the standard depth at which backgrounds are added.
         * This adjustment is added to the standard background depth (-10). This allows one to
         * control the rendering order of multiple backgrounds on a single widget. */
        public abstract void addTo (GroupLayer parent, float x, float y, float depthAdjust);

        /** Disposes of this background instance when it is no longer valid/needed. */
        public abstract void destroy ();
    }

    protected static class LayerInstance extends Instance {
        public LayerInstance (Layer... layers) {
            _layers = layers;
        }
        @Override public void addTo (GroupLayer parent, float x, float y, float depthAdjust) {
            for (Layer layer : _layers) {
                layer.setDepth(BACKGROUND_DEPTH + depthAdjust);
                parent.addAt(layer, x, y);
            }
        }
        @Override public void destroy () {
            for (Layer layer : _layers) {
                layer.destroy();
            }
        }
        protected Layer[] _layers;
    }
}
