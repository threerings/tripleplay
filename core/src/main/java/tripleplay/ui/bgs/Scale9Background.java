//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;

import playn.core.Image;
import playn.core.ImmediateLayer;
import playn.core.PlayN;
import playn.core.Surface;
import playn.core.Tint;

import tripleplay.ui.Background;
import tripleplay.ui.util.Scale9;

/**
 * A background constructed by scaling the parts of a source image to fit the target width and
 * height. Uses {@link Scale9}.
 */
public class Scale9Background extends Background
{
    /** Creates a new background using the given image. The image is assumed to be divided into
     * a 3x3 grid of 9 equal pieces.
     *
     * <p>NOTE: the image must be preloaded since we need to determine the stretching factor.
     * If this cannot be arranged using the application resource strategy, callers may consider
     * setting the background style from the images callback.</p>
     */
    public Scale9Background (Image image) {
        if (!image.isReady()) {
            // complain about this, we don't support asynch images
            PlayN.log().warn("Scale9 image not preloaded: " + image);
        }
        _image = image;
        _s9 = new Scale9(image.width(), image.height());
    }

    /** Returns the scale 9 instance for mutation. Be sure to finish mutation prior to binding. */
    public Scale9 scale9 () {
        return _s9;
    }

    @Override
    protected Instance instantiate (final IDimension size) {
        return new LayerInstance(size, new ImmediateLayer.Renderer() {
            // The destination scale 9.
            Scale9 dest = new Scale9(size.width(), size.height(), _s9);
            public void render (Surface surf) {
                surf.save();
                if (alpha != null) surf.setAlpha(alpha);
                if (_tint != Tint.NOOP_TINT) surf.setTint(_tint);
                // issue the 9 draw calls
                for (int yy = 0; yy < 3; ++yy) for (int xx = 0; xx < 3; ++xx) {
                    drawPart(surf, xx, yy);
                }
                if (alpha != null) surf.setAlpha(1); // alpha is not part of save/restore
                surf.restore();
            }

            protected void drawPart (Surface surf, int x, int y) {
                float dw = dest.xaxis.size(x), dh = dest.yaxis.size(y);
                if (dw == 0 || dh == 0) return;
                surf.drawImage(
                    _image, dest.xaxis.coord(x), dest.yaxis.coord(y), dw, dh,
                    _s9.xaxis.coord(x), _s9.yaxis.coord(y), _s9.xaxis.size(x), _s9.yaxis.size(y));
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
    public Scale9Background setTint (int tint) {
        _tint = tint;
        this.alpha = ((tint >> 24) & 0xFF) / 255f;
        return this;
    }

    protected Image _image;
    protected Scale9 _s9;
    protected int _tint = Tint.NOOP_TINT;
}
