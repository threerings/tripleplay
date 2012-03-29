//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.PlayN;

import pythagoras.f.IRectangle;

import tripleplay.util.Objects;

/**
 * Wraps up an image and source bounds and provides methods to create the layer.
 */
public class Icon
{
    /** The image to be used in rendering. */
    public final Image image;

    /** The bounds within the image confining the icon, or null if the whole image is the icon. */
    public final IRectangle bounds;

    /**
     * Creates a new icon with the given image.
     */
    public Icon (Image image) {
        this(image, null);
    }

    /**
     * Creates a new icon with the given image and bounds.
     */
    public Icon (Image image, IRectangle bounds) {
        this.image = image;
        this.bounds = bounds;
    }

    /**
     * Returns the width of the icon.
     */
    public float width () {
        return bounds == null ? image.width() : bounds.width();
    }

    /**
     * Returns the height of the icon.
     */
    public float height () {
        return bounds == null ? image.height() : bounds.height();
    }

    /**
     * Sets the image and if appropriate, the width, height and source rect, of the given image
     * layer.
     */
    public Icon setToLayer (ImageLayer layer) {
        layer.setImage(image);
        if (bounds != null) {
            layer.setWidth(bounds.width());
            layer.setHeight(bounds.height());
            layer.setSourceRect(bounds.x(), bounds.y(),
                                bounds.width(), bounds.height());
        }
        return this;
    }

    /**
     * Creates or updates an image layer with this icon in it. A new layer is allocated only if
     * the given layer is null. Otherwise the given layer is reset to this icon.
     */
    public ImageLayer createLayer (ImageLayer layer) {
        if (layer == null) layer = PlayN.graphics().createImageLayer();
        setToLayer(layer);
        return layer;
    }

    @Override public boolean equals (Object o) {
        if (!(o instanceof Icon)) return false;
        Icon oicon = (Icon)o;
        return Objects.equal(image, oicon.image) && Objects.equal(bounds, oicon.bounds);
    }

    @Override public int hashCode () {
        return image.hashCode() ^ (bounds == null ? 0 : bounds.hashCode());
    }
}
