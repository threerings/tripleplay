//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.MathUtil;

import react.Closeable;

import playn.core.Graphics;
import playn.core.Surface;
import playn.core.Texture;
import playn.core.Tile;
import playn.core.TileSource;
import playn.scene.GroupLayer;
import playn.scene.ImageLayer;
import playn.scene.Layer;

import tripleplay.ui.bgs.BlankBackground;
import tripleplay.ui.bgs.CompositeBackground;
import tripleplay.ui.bgs.RoundRectBackground;
import tripleplay.ui.bgs.Scale9Background;
import tripleplay.ui.util.Insets;

/**
 * A background is responsible for rendering a border and a fill. It is used in conjunction with
 * groups and buttons and any other elements that need a background.
 */
public abstract class Background
{
    /** An instantiation of a particular background template. Backgrounds are configured as a style
     * property; elements instantiate them at specific dimensions when they are actually used.*/
    public abstract class Instance implements Closeable {

        /** The size at which this instance was prepared. */
        public final IDimension size;

        /** Returns the background that created this instance. */
        public Background owner () { return Background.this; }

        /** Adds this background's layers to the specified group at the specified x/y offset.
         * @param depthAdjust an adjustment to the standard depth at which backgrounds are added.
         * This adjustment is added to the standard background depth (-10). This allows one to
         * control the rendering order of multiple backgrounds on a single widget. */
        public abstract void addTo (GroupLayer parent, float x, float y, float depthAdjust);

        protected Instance (IDimension size) {
            this.size = new Dimension(size);
        }
    }

    /** The (highest) depth at which background layers are rendered. May range from (-11, 10]. */
    public static final float BACKGROUND_DEPTH = -10f;

    /**
     * Creates a null background (transparent).
     */
    public static Background blank () {
        return new BlankBackground();
    }

    /** Creates a solid background of the specified color. */
    public static Background solid (final int color) {
        return new Background() {
            @Override protected Instance instantiate (final IDimension size) {
                return new LayerInstance(size, new Layer() {
                    @Override protected void paintImpl (Surface surf) {
                        surf.setFillColor(color).fillRect(0, 0, size.width(), size.height());
                    }
                });
            }
        };
    }

    /** Creates a beveled background with the specified colors. */
    public static Background beveled (final int bgColor, final int ulColor, final int brColor) {
        return new Background() {
            @Override protected Instance instantiate (final IDimension size) {
                return new LayerInstance(size, new Layer() {
                    @Override protected void paintImpl (Surface surf) {
                        float width = size.width(), height = size.height();
                        float bot = height, right = width;
                        surf.setFillColor(bgColor).fillRect(0, 0, width, height);
                        surf.setFillColor(ulColor).
                            drawLine(0, 0, right, 0, 2).drawLine(0, 0, 0, bot, 2);
                        surf.setFillColor(brColor).
                            drawLine(right, 0, right, bot, 1).drawLine(1, bot-1, right-1, bot-1, 1).
                            drawLine(0, bot, right, bot, 1).drawLine(right-1, 1, right-1, bot-1, 1);
                    }
                });
            }
        };
    }

    /** Creates a bordered background with the specified colors and thickness. */
    public static Background bordered (final int bgColor, final int color, final float thickness) {
        return new Background() {
            @Override protected Instance instantiate (final IDimension size) {
                return new LayerInstance(size, new Layer() {
                    @Override protected void paintImpl (Surface surf) {
                        float width = size.width(), height = size.height();
                        surf.setFillColor(bgColor).fillRect(0, 0, width, height);
                        surf.setFillColor(color).
                            fillRect(0, 0, width, thickness).
                            fillRect(0, 0, thickness, height).
                            fillRect(width-thickness, 0, thickness, height).
                            fillRect(0, height-thickness, width, thickness);
                    }
                });
            }
        };
    }

    /** Creates a round rect background with the specified color and corner radius. */
    public static Background roundRect (Graphics gfx, int bgColor, float cornerRadius) {
        return new RoundRectBackground(gfx, bgColor, cornerRadius);
    }

    /** Creates a round rect background with the specified parameters. */
    public static Background roundRect (Graphics gfx, int bgColor, float cornerRadius,
                                        int borderColor, float borderWidth) {
        return new RoundRectBackground(gfx, bgColor, cornerRadius, borderColor, borderWidth);
    }

    /** Creates a background with the specified source. */
    public static Background image (final TileSource source) {
        return new Background() {
            @Override protected Instance instantiate (IDimension size) {
                ImageLayer layer = new ImageLayer(source);
                layer.setSize(size.width(), size.height());
                return new LayerInstance(size, layer);
            }
        };
    }

    /** Creates a centered background with the specified texture tile. */
    public static Background centered (final Tile tile) {
        return new Background() {
            @Override protected Instance instantiate (IDimension size) {
                final float x = MathUtil.ifloor((size.width()-tile.width())/2);
                final float y = MathUtil.ifloor((size.height()-tile.height())/2);
                return new LayerInstance(size, new Layer() {
                    protected void paintImpl (Surface surf) {
                        surf.draw(tile, x, y);
                    }
                });
            }
        };
    }

    /** Creates a cropped centered background with the specified texture tile. */
    public static Background cropped (final Tile tile) {
        return new Background() {
            @Override protected Instance instantiate (IDimension size) {
                final float swidth = size.width(),   sheight = size.height();
                final float iwidth = tile.width(), iheight = tile.height();
                final float cwidth = Math.min(swidth, iwidth), cheight = Math.min(sheight, iheight);
                final float sx = (swidth > iwidth) ? 0 : (iwidth - swidth)/2;
                final float sy = (sheight > iheight) ? 0 : (iheight - sheight)/2;
                return new LayerInstance(size, new Layer() {
                    protected void paintImpl (Surface surf) {
                        float dy = 0;
                        while (dy < sheight) {
                            float dheight = Math.min(cheight, sheight-dy);
                            float dx = 0;
                            while (dx < swidth) {
                                float dwidth = Math.min(cwidth, swidth-dx);
                                surf.draw(tile, dx, dy, dwidth, dheight, sx, sy, dwidth, dheight);
                                dx += cwidth;
                            }
                            dy += cheight;
                        }
                    }
                });
            }
        };
    }

    /** Creates a tiled background with the specified texture. */
    public static Background tiled (final Texture tex) {
        return new Background() {
            @Override protected Instance instantiate (final IDimension size) {
                return new LayerInstance(size, new Layer() {
                    protected void paintImpl (Surface surf) {
                        float width = size.width(), height = size.height();
                        float twidth = tex.displayWidth, theight = tex.displayHeight;
                        for (float y = 0; y < height; y += theight) {
                            float h = Math.min(height-y, theight);
                            for (float x = 0; x < width; x += twidth) {
                                float w = Math.min(width-x, twidth);
                                surf.draw(tex, x, y, w, h, 0, 0, w, h);
                            }
                        }
                    }
                });
            }
        };
    }

    /** Creates a scale9 background with the specified texture tile.
      * See {@link Scale9Background}. */
    public static Scale9Background scale9 (Tile tile) {
        return new Scale9Background(tile);
    }

    /** Creates a composite background with the specified backgrounds.
      * See {@link CompositeBackground}. */
    public static Background composite (Background... constituents) {
        return new CompositeBackground(constituents);
    }

    /** Instantiates a background at the supplied size. */
    public static Instance instantiate (Background delegate, IDimension size) {
        return delegate.instantiate(size);
    }

    /** The insets of this background, added to get the overall Element size. */
    public Insets insets = Insets.ZERO;

    /** The alpha transparency of this background (or null if no alpha has been configured). */
    public Float alpha;

    /** Configures insets on this background. */
    public Background insets (Insets insets) {
        this.insets = insets;
        return this;
    }

    /** Configures uniform insets on this background. */
    public Background inset (float uniformInset) {
        insets = Insets.uniform(uniformInset);
        return this;
    }

    /** Configures horizontal and vertical insets on this background. */
    public Background inset (float horiz, float vert) {
        insets = Insets.symmetric(horiz, vert);
        return this;
    }

    /** Configures non-uniform insets on this background. */
    public Background inset (float top, float right, float bottom, float left) {
        insets = new Insets(top, right, bottom, left);
        return this;
    }

    /** Sets the left inset for this background. */
    public Background insetLeft (float left) {
        insets = insets.mutable().left(left);
        return this;
    }

    /** Sets the right inset for this background. */
    public Background insetRight (float right) {
        insets = insets.mutable().right(right);
        return this;
    }

    /** Sets the top inset for this background. */
    public Background insetTop (float top) {
        insets = insets.mutable().top(top);
        return this;
    }

    /** Sets the bottom inset for this background. */
    public Background insetBottom (float bottom) {
        insets = insets.mutable().bottom(bottom);
        return this;
    }

    /** Configures the alpha transparency of this background. */
    public Background alpha (float alpha) {
        this.alpha = alpha;
        return this;
    }

    /** Instantiates this background using the supplied widget size. The supplied size should
      * include the insets defined for this background. */
    protected abstract Instance instantiate (IDimension size);

    protected class LayerInstance extends Instance {
        public LayerInstance (IDimension size, Layer layer) {
            super(size);
            _layer = layer;
            if (alpha != null) _layer.setAlpha(alpha);
        }
        @Override public void addTo (GroupLayer parent, float x, float y, float depthAdjust) {
            _layer.setDepth(BACKGROUND_DEPTH + depthAdjust);
            _layer.transform().translate(x, y); // adjust any existing transform
            parent.add(_layer);
        }
        @Override public void close () {
            _layer.close();
        }
        protected Layer _layer;
    }
}
