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
import playn.core.PlayN;
import playn.core.Surface;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.Rectangle;

import tripleplay.ui.bgs.BeveledBackground;
import tripleplay.ui.bgs.BlankBackground;
import tripleplay.ui.bgs.BorderedBackground;
import tripleplay.ui.bgs.ImageBackground;
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
    public static Background bordered (int bgColor, int color, int thickness) {
        return new BorderedBackground(bgColor, color, thickness);
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
     * Adds this background's insests to the supplied dimensions. Returns {@code size} for chaning.
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
     * include the insets defined for this backround.
     */
    protected abstract Instance instantiate (IDimension size);

    protected static Layer createSolidLayer (
        final int color, final float width, final float height) {
        return PlayN.graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            public void render (Surface surf) {
                surf.setFillColor(color).fillRect(0, 0, width, height);
            }
        });
    }

    protected static Layer createTiledLayer (Image image, float width, float height) {
        ImageLayer layer = PlayN.graphics().createImageLayer(image);
        layer.setRepeatX(true);
        layer.setRepeatY(true);
        return layer;
    }

    /** An instantiation of a particular background template. Backgrounds are configured as a style
     * property; elements instantiate them at specific dimensions when they are actually used.*/
    protected static abstract class Instance {
        /** Adds this background's layers to the specified group. */
        public abstract void addTo (GroupLayer parent);

        /** Disposes of this background instance when it is no longer valid/needed. */
        public abstract void destroy ();
    }

    protected static class LayerInstance extends Instance {
        public LayerInstance (Layer... layers) {
            _layers = layers;
            for (Layer layer : _layers) {
                layer.setDepth(BACKGROUND_DEPTH);
            }
        }
        @Override public void addTo (GroupLayer parent) {
            for (Layer layer : _layers) {
                parent.add(layer);
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
