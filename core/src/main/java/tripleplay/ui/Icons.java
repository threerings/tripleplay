//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

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
            image.addCallback(new Callback<Image>() {
                @Override public void onSuccess (Image result) {
                    callback.onSuccess(ImageIcon.this);
                }
                @Override public void onFailure (Throwable cause) {
                    callback.onFailure(cause);
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
}
