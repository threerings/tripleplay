//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.GroupLayer;
import playn.core.Image;
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
     * A simple icon that uses an {@code Image} to get its dimensions and a simple image layer when
     * rendered. The the caller must ensure the image is loaded prior to invoking {@link Icon}
     * methods.
     */
    public static class Preloaded implements Icon {
        /** The image. */
        public final Image image;

        /** Creates a new icon for the given image. */
        public Preloaded (Image image) { this.image = image; }

        @Override public float width () { return getReady().width(); }
        @Override public float height () { return getReady().height(); }
        @Override public Layer render () { return graphics().createImageLayer(getReady()); }

        @Override public void addCallback (final Callback<? super Icon> callback) {
            // delegate to the image's callback
            image.addCallback(new Callback<Image>() {
                @Override public void onSuccess (Image result) {
                    callback.onSuccess(Preloaded.this);
                }
                @Override public void onFailure (Throwable cause) {
                    callback.onFailure(cause);
                }
            });
        }

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
    public static class Loader extends Preloaded
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
            if (image.isReady()) return super.render();
            final GroupLayer gl = PlayN.graphics().createGroupLayer();
            image.addCallback(new Callback<Image>() {
                @Override public void onSuccess (Image result) {
                    if (!gl.destroyed()) { gl.add(Loader.super.render()); }
                }

                @Override public void onFailure (Throwable cause) {}
            });
            return gl;
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
