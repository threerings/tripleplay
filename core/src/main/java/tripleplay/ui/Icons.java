//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.CanvasImage;
import playn.core.GroupLayer;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.util.Callback;

import static playn.core.PlayN.graphics;

/**
 * Contains icon related utility classes and methods, mostly basic icon factories.
 */
public class Icons
{
    /**
     * Base class for image icons just handling the image and the callback.
     */
    public static abstract class ImageIcon implements Icon {
        /** The image. */
        public final Image image;

        /** Creates a new icon for the given image. */
        public ImageIcon (Image image) { this.image = image; }

        @Override public void addCallback (final Callback<? super Icon> callback) {
            // delegate to the image's callback
            image.addCallback(new Callback.Chain<Image>(callback) {
                @Override public void onSuccess (Image result) {
                    callback.onSuccess(ImageIcon.this);
                }
            });
        }
    }

    /**
     * A simple icon that uses an {@code Image} to get its dimensions and a simple image layer when
     * rendered. The the caller must ensure the image is loaded prior to invoking {@link Icon}
     * methods.
     */
    public static class Preloaded extends ImageIcon {
        /** Creates a new icon for the given image. */
        public Preloaded (Image image) { super(image); }

        @Override public float width () { return getReady().width(); }
        @Override public float height () { return getReady().height(); }
        @Override public Layer render () { return graphics().createImageLayer(getReady()); }

        protected Image getReady () {
            if (!image.isReady()) {
                // issue a warning since this will normally cause display artifacts
                // TODO: remove and use assert(isReady())?
                Log.log.warning("Rendering image icon that isn't ready",
                    "icon", this, "image", image);
            }
            return image;
        }
    }

    /**
     * An icon that supports asynchronous images.
     */
    public static class Loader extends ImageIcon
    {
        /** The dimensions. */
        public final float width, height;

        /**
         * Creates a new icon with the given image and dimensions. The image dimensions are
         * ignored. If the image is not yet loaded at render time, a blank layer will be returned
         * that will be automatically updated once the image is loaded.
         */
        public Loader (Image image, float width, float height) {
            super(image);
            this.width = width;
            this.height = height;
        }

        @Override public float width () { return width; }
        @Override public float height () { return height; }

        @Override public Layer render () {
            ImageLayer il = PlayN.graphics().createImageLayer(image);
            if (!image.isReady()) il.setSize(width, height);
            return il;
        }
    }

    /**
     * Defers to another icon. Subclasses decide how to modify the width and height and how to
     * use the rendered layer. The base takes care of the callback. By default, returns the
     * size and layer without modification.
     */
    public abstract static class Aggregated
        implements Icon
    {
        /** Icon that is deferred to. */
        public final Icon icon;

        /** Creates a new aggregated icon that defers to the given one. */
        public Aggregated (Icon icon) {
            this.icon = icon;
        }

        @Override public float width () { return icon.width(); }
        @Override public float height () { return icon.height(); }
        @Override public Layer render () { return icon.render(); }
        @Override public void addCallback (final Callback<? super Icon> callback) {
            icon.addCallback(new Callback.Chain<Icon>(callback) {
                @Override public void onSuccess (Icon result) {
                    callback.onSuccess(Aggregated.this);
                }
            });
        }
    }

    /**
     * Aggregates and scales another icon.
     */
    public static class Scaled extends Aggregated
        implements Icon
    {
        /** The scale to apply to the size and layer. */
        public final float scale;

        /**
         * Creates a new icon that scale the given icon to the given scale.
         */
        public Scaled (Icon icon, float scale) {
            super(icon);
            this.scale = scale;
        }

        @Override public float width () { return super.width() * scale; }
        @Override public float height () { return super.height() * scale; }
        @Override public Layer render () {
            Layer layer = super.render().setScale(scale);
            layer.setOrigin(layer.originX() * scale, layer.originY() * scale);
            return layer;
        }
    }

    /**
     * Nests another icon with an offset. The nesting is necessary since users of icon need to
     * control the layer translation directly.
     */
    public static class Offset extends Aggregated
        implements Icon
    {
        /** The horizontal offset. */
        public final float tx;

        /** The vertical offset. */
        public final float ty;

        /**
         * Creates a new icon that nests and offsets the given icon by the given amounts.
         */
        public Offset (Icon icon, float tx, float ty) {
            super(icon);
            this.tx = tx;
            this.ty = ty;
        }

        @Override public Layer render () {
            GroupLayer layer = PlayN.graphics().createGroupLayer();
            layer.addAt(super.render(), tx, ty);
            return layer;
        }
    }

    /**
     * Creates an icon using the supplied image. The the caller must ensure the image is loaded
     * prior to invoking {@link Icon} methods. See {@link Preloaded}.
     */
    public static Icon image (Image image) {
        return new Preloaded(image);
    }

    /**
     * Creates an icon using the supplied image and dimensions. If the icon is rendered prior to
     * the image being ready, a blank layer is returned that will automatically be populated
     * once the image is loaded.
     */
    public static Icon loader (Image image, float width, float height) {
        return new Loader(image, width, height);
    }

    /**
     * Creates an icon that applies the given scale to the given icon.
     */
    public static Icon scaled (Icon icon, float scale) {
        return new Scaled(icon, scale);
    }

    /**
     * Creates an icon that nests and offsets the given icon by the given translation.
     */
    public static Icon offset (Icon icon, float tx, float ty) {
        return new Offset(icon, tx, ty);
    }

    /**
     * Creates a solid square icon of the given size.
     */
    public static Icon solid (int color, float size) {
        CanvasImage image = PlayN.graphics().createImage(1, 1);
        image.canvas().setFillColor(color).fillRect(0, 0, 1, 1);
        return scaled(image(image), size);
    }
}
