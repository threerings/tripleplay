//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;

import playn.core.Image;
import playn.core.ImmediateLayer;
import playn.core.PlayN;
import playn.core.Surface;
import playn.core.Tint;

import tripleplay.ui.Background;
import tripleplay.ui.util.Scale3;

/**
 * A background constructed by scaling the parts of a source image to fit the target width and
 * height. Uses {@link Scale3}.
 */
public class Scale3Background extends Background
{
    /** Creates a new background using the given image. The image is assumed to be divided into a
     * 3x1 grid of 3 equal pieces.
     *
     * <p>NOTE: the image must be preloaded since we need to determine the stretching factor. If
     * this cannot be arranged using the application resource strategy, callers may consider
     * setting the background style from the images callback.</p>
     */
    public Scale3Background (Image image) {
        if (!image.isReady()) {
            // complain about this, we don't support asynch images
            PlayN.log().warn("Scale3 image not preloaded: " + image);
        }
        _image = image;
        _s3 = new Scale3(image.width(), image.height());
    }

    /** Returns the scale 3 instance for mutation. Be sure to finish mutation prior to binding. */
    public Scale3 scale3 () {
        return _s3;
    }

    @Override
    protected Instance instantiate (final IDimension size) {
        return new LayerInstance(size, new ImmediateLayer.Renderer() {
            // The destination scale 3.
            Scale3 dest = new Scale3(size.width(), size.height(), _s3);
            @Override public void render (Surface surf) {
                surf.save();
                if (alpha != null) {
                    surf.setAlpha(alpha);
                }
                if (_tint != Tint.NOOP_TINT) {
                    surf.setTint(_tint);
                }
                // issue the 3 draw calls
                for (int xx = 0; xx < 3; ++xx) {
                    drawPart(surf, xx);
                }
                if (alpha != null) {
                    surf.setAlpha(1); // alpha is not part of save/restore
                }
                surf.restore();
            }

            protected void drawPart (Surface surf, int x) {
                float dw = dest.xaxis.size(x), dh = dest.height;
                if (dw == 0 || dh == 0) {
                    return;
                }
                surf.drawImage(_image, dest.xaxis.coord(x), 0, dw, dh,
                               _s3.xaxis.coord(x), 0, _s3.xaxis.size(x), _s3.height);
            }
        });
    }

    /**
     * Sets the tint for this background, as {@code ARGB}.
     *
     * <p> <em>NOTE:</em> this will overwrite any value configured via {@link #alpha}. Either
     * include your desired alpha in the high bits of {@code tint} or set {@link #alpha} after
     * calling this method. </p>
     *
     * <p> <em>NOTE:</em> the RGB components of a layer's tint only work on GL-based backends. It
     * is not possible to tint layers using the HTML5 canvas and Flash backends. </p>
     */
    public Scale3Background setTint (int tint) {
        _tint = tint;
        this.alpha = ((tint >> 24) & 0xFF) / 255f;
        return this;
    }

    protected Image _image;
    protected Scale3 _s3;
    protected int _tint = Tint.NOOP_TINT;
}
