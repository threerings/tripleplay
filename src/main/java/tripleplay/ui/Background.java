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
     * Creates a null background (transparent) with the specified insets.
     */
    public static Background blank (float inset) {
        return blank(inset, inset, inset, inset);
    }

    /**
     * Creates a null background (transparent) with the specified insets.
     */
    public static Background blank (float top, float right, float bottom, float left) {
        return new BlankBackground(top, right, bottom, left);
    }

    /**
     * Creates a solid background of the specified color, with no insets.
     */
    public static Background solid (int color) {
        return solid(color, 0, 0, 0, 0);
    }

    /**
     * Creates a solid background of the specified color and (uniform) insets.
     */
    public static Background solid (int color, float inset) {
        return solid(color, inset, inset, inset, inset);
    }

    /**
     * Creates a solid background of the specified color and insets.
     */
    public static Background solid (int color, float top, float right, float bottom, float left) {
        return new SolidBackground(color, top, right, bottom, left);
    }

    /**
     * Creates a beveled background with the specified colors and no insets.
     */
    public static Background beveled (int bgColor, int ulColor, int brColor) {
        return beveled(bgColor, ulColor, brColor, 0, 0, 0, 0);
    }

    /**
     * Creates a beveled background with the specified colors and uniform insets.
     */
    public static Background beveled (int bgColor, int ulColor, int brColor, float inset) {
        return beveled(bgColor, ulColor, brColor, inset, inset, inset, inset);
    }

    /**
     * Creates a beveled background with the specified colors and insets.
     */
    public static Background beveled (int bgColor, int ulColor, int brColor,
                                      float top, float right, float bottom, float left) {
        return new BeveledBackground(bgColor, ulColor, brColor, top, left, bottom, right);
    }

    /**
     * Creates a bordered background with the specified colors and thickness and no insets.
     */
    public static Background bordered (int bgColor, int color, int thickness) {
        return bordered(bgColor, color, thickness, 0, 0, 0, 0);
    }

    /**
     * Creates a bordered background with the specified colors, thickness and uniform insets.
     */
    public static Background bordered (int bgColor, int color, int thickness, float inset) {
        return bordered(bgColor, color, thickness, inset, inset, inset, inset);
    }

    /**
     * Creates a bordered background with the specified colors, thickness and insets.
     */
    public static Background bordered (int bgColor, int color, int thickness,
                                      float top, float right, float bottom, float left) {
        return new BorderedBackground(bgColor, color, thickness, top, left, bottom, right);
    }

    /**
     * Creates an image background with the specified image and no insets.
     */
    public static Background image (Image bgimage) {
        return image(bgimage, 0);
    }

    /**
     * Creates an image background with the specified image and uniform insets.
     */
    public static Background image (Image bgimage, float inset) {
        return image(bgimage, inset, inset, inset, inset);
    }

    /**
     * Creates an image background with the specified image and insets.
     */
    public static Background image (Image bgimage, float top, float right, float bottom, float left) {
        return new ImageBackground(bgimage, top, right, bottom, left);
    }

    /**
     * Creates a scale9 background with the specified image and no insets.
     * @see Scale9Background
     */
    public static Background scale9 (Image scale9Image) {
        return scale9(scale9Image, 0);
    }

    /**
     * Creates a scale9 background with the specified image and uniform insets.
     * @see Scale9Background
     */
    public static Background scale9 (Image scale9Image, float inset) {
        return scale9(scale9Image, inset, inset, inset, inset);
    }

    /**
     * Creates a scale9 background with the specified image and insets.
     * @see Scale9Background
     */
    public static Background scale9 (Image scale9Image,
                                     float top, float right, float bottom, float left) {
        return new Scale9Background(scale9Image, top, right, bottom, left);
    }

    /** The insets of this background. */
    public final float top, right, bottom, left;

    /** Returns this background's adjustment to an element's width. */
    public float width () {
        return left + right;
    }

    /** Returns this background's adjustment to an element's height. */
    public float height () {
        return top + bottom;
    }

    /**
     * Adds this background's insests to the supplied dimensions. Returns {@code size} for chaning.
     */
    public Dimension addInsets (Dimension size) {
        size.width += width();
        size.height += height();
        return size;
    }

    /**
     * Instantiates this background using the supplied widget size. The supplied size should
     * include the insets defined for this backround.
     */
    protected abstract Instance instantiate (IDimension size);

    protected Background (float top, float right, float bottom, float left) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
    }

    protected static Layer createSolidLayer (final int color, final float width, final float height) {
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
