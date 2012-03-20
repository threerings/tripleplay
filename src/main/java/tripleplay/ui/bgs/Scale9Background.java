//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import java.util.NoSuchElementException;

import playn.core.Image;
import playn.core.ImmediateLayer;
import playn.core.PlayN;
import playn.core.Surface;
import pythagoras.f.IDimension;
import tripleplay.ui.Background;

/**
 * A background constructed by scaling the parts of a source image to fit the target width
 * and height. First the source image is divided into a 3x3 grid. Of the resulting 9 parts, the
 * corners are drawn without scaling to the destination, the top and bottom center pieces are
 * copied with horizontal scaling, the left and right center pieces are copied with vertical
 * scaling, and the center piece is copied with both horizontal and vertical scaling.
 */
public class Scale9Background extends Background
{
    /**
     * Creates a new background using the given image and insets. The subdivision of the image
     * into a 3x3 grid is automatic.
     */
    public Scale9Background (Image image, float top, float right, float bottom, float left) {
        super(top, right, bottom, left);
        _image = image;
        _sx = new Axis3(image.width());
        _sy = new Axis3(image.height());
    }

    @Override
    protected Instance instantiate (final IDimension size) {
        return new LayerInstance(PlayN.graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            // The axes of our destination surface.
            Axis3 dx = new Axis3(size.width(), _sx), dy = new Axis3(size.height(), _sy);
            public void render (Surface surf) {
                // issue the 9 draw calls
                for (int yy = 0; yy < 3; ++yy) {
                    for (int xx = 0; xx < 3; ++xx) {
                        drawPart(surf, xx, yy);
                    }
                }
            }

            protected void drawPart (Surface surf, int x, int y) {
                surf.drawImage(_image,
                    dx.coord(x), dy.coord(y), dx.size(x), dy.size(y),
                    _sx.coord(x), _sy.coord(y), _sx.size(x), _sy.size(y));
            }
        }));
    }

    /**
     * A horizontal or vertical axis, broken up into 3 chunks.
     */
    protected static class Axis3
    {
        /** The lengths of the 3 chunks. */
        public final float[] lengths;

        /**
         * Creates a new axis equally splitting the given length.
         */
        public Axis3 (int length) {
            int d = length / 3;
            lengths = new float[] {d, length - 2 * d, d};
        }

        /**
         * Creates a new axis with the given total length and 0th and 2nd lengths copied from a
         * source axis.
         */
        public Axis3 (float length, Axis3 src) {
            lengths = new float[] {src.size(0), length - src.size(0) - src.size(2), src.size(2)};
        }

        /**
         * Gets the coordinate of the given chunk, 0 - 2.
         */
        public float coord (int idx) {
            switch (idx) {
            case 0: return 0;
            case 1: return lengths[0];
            case 2: return lengths[1] + lengths[0];
            default: throw new NoSuchElementException();
            }
        }

        /**
         * Gets the size of the given chunk, 0 - 2.
         */
        public float size (int idx) {
            return lengths[idx];
        }
    }

    protected Image _image;

    /** The axes of our source image. */
    protected final Axis3 _sx, _sy;
}
