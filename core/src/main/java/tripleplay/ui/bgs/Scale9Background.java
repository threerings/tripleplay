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

import tripleplay.ui.Background;

/**
 * A background constructed by scaling the parts of a source image to fit the target width and
 * height. First the source image is divided into a 3x3 grid. Of the resulting 9 parts, the corners
 * are drawn without scaling to the destination, the top and bottom center pieces are copied with
 * horizontal scaling, the left and right center pieces are copied with vertical scaling, and the
 * center piece is copied with both horizontal and vertical scaling.
 *
 * <p>By using {@link #xaxis} and {@link #yaxis}, the partitioning of the image can be
 * controlled directly. For example, if the horizontal middle of an image is a single pixel:
 * <pre>{@code
 *     Scale9Background bkg = ...;
 *     bkg.xaxis.resize(1, 1);
 * }</pre></p>
 */
public class Scale9Background extends Background
{
    /** A horizontal or vertical axis, broken up into 3 chunks. */
    public static class Axis3
    {
        /** Creates a new axis equally splitting the given length. */
        public Axis3 (float length) {
            float d = length / 3;
            _lengths = new float[] {d, length - 2 * d, d};
            _offsets = new float[] {0, _lengths[0], _lengths[0] + _lengths[1]};
        }

        /** Creates a new axis with the given total length and 0th and 2nd lengths copied from a
         * source axis. */
        public Axis3 (float length, Axis3 src) {
            _lengths = new float[] {src.size(0), length - src.size(0) - src.size(2), src.size(2)};
            _offsets = new float[] {0, _lengths[0], _lengths[0] + _lengths[1]};
        }

        /** Returns the coordinate of the given chunk, 0 - 2. */
        public float coord (int idx) {
            return _offsets[idx];
        }

        /** Returns the size of the given chunk, 0 - 2. */
        public float size (int idx) {
            return _lengths[idx];
        }

        /** Sets the size and location of the given chunk, 0 - 2. */
        public Axis3 set (int idx, float coord, float size) {
            _offsets[idx] = coord;
            _lengths[idx] = size;
            return this;
        }

        /** Sets the size of the given chunk, shifting neighbors. */
        public Axis3 resize (int idx, float size) {
            float excess = _lengths[idx] - size;
            _lengths[idx] = size;
            switch (idx) {
            case 0:
                _offsets[1] -= excess;
                _lengths[1] += excess;
                break;
            case 1:
                float half = excess * .5f;
                _lengths[0] += half;
                _lengths[2] += half;
                _offsets[1] += half;
                _offsets[2] -= half;
                break;
            case 2:
                _offsets[2] -= excess;
                _lengths[1] += excess;
                break;
            }
            return this;
        }

        /** The positions of the 3 chunks. */
        protected final float[] _offsets;

        /** The lengths of the 3 chunks. */
        protected final float[] _lengths;
    }

    /**
     * Will ensure that the Axis3 passed in does not exceed the length given. An equal chunk will
     * be removed from the outer chunks if it is too long. The given Axis3 is modified and returned.
     */
    public static Axis3 clamp (Axis3 axis, float length) {
        float left = axis.size(0);
        float middle = axis.size(1);
        float right = axis.size(2);
        if (left + middle + right > length && middle > 0 && left + right < length) {
            // the special case where for some reason the total is too wide, but the middle is non
            // zero, and it can absorb the extra all on its own.
            axis.set(1, left, length - left - right);
            axis.set(2, length - right, right);
        } else if (left + right > length) {
            // eat equal chunks out of each end so that we don't end up overlapping
            float remove = (left + right - length) / 2;
            axis.set(0, 0, left - remove);
            axis.set(1, left - remove, 0);
            axis.set(2, left - remove, right - remove);
        }
        return axis;
    }

    /** The axes of our source image. */
    public final Axis3 xaxis, yaxis;

    /** Creates a new background using the given image. The subdivision of the image into a 3x3
     * grid is automatic.
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
        xaxis = new Axis3(image.width());
        yaxis = new Axis3(image.height());
    }

    @Override
    protected Instance instantiate (final IDimension size) {
        return new LayerInstance(size, new ImmediateLayer.Renderer() {
            // The axes of our destination surface.
            Axis3 dx = clamp(new Axis3(size.width(), xaxis), size.width());
            Axis3 dy = clamp(new Axis3(size.height(), yaxis), size.height());
            public void render (Surface surf) {
                surf.save();
                if (alpha != null) surf.setAlpha(alpha);
                // issue the 9 draw calls
                for (int yy = 0; yy < 3; ++yy) for (int xx = 0; xx < 3; ++xx) {
                    drawPart(surf, xx, yy);
                }
                if (alpha != null) surf.setAlpha(1); // alpha is not part of save/restore
                surf.restore();
            }

            protected void drawPart (Surface surf, int x, int y) {
                if (dx.size(x) ==0 || dy.size(y) == 0) return;
                surf.drawImage(_image,
                               dx.coord(x), dy.coord(y), dx.size(x), dy.size(y),
                               xaxis.coord(x), yaxis.coord(y), xaxis.size(x), yaxis.size(y));
            }
        });
    }

    protected Image _image;
}
