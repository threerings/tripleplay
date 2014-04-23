//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;

/**
 * Facilitates the rendering of "scale-9" images, that is, images that are designed as a 3x3 grid
 * such that each of the 9 pieces is fixed or stretched in one or both directions to fit a
 * designated area. The corners are drawn without scaling, the top and bottom center pieces are
 * scaled horizontally, the left and right center pieces are scaled vertically, and the center
 * piece is scaled both horizontally and vertically.
 * <p>By default, the cells are assumed to be of equal size (hence scale-9 image dimensions are
 * normally a multiple of 3). By using {@link #xaxis} and {@link #yaxis}, this partitioning can be
 * controlled directly. For example, if the horizontal middle of an image is a single pixel, this
 * code will do that and automatically grow the left and right edges if necessary:
 * <pre><code>
 *     Scale9 s9 = ...;
 *     s9.xaxis.resize(1, 1);
 * </code></pre></p>
 * <p>NOTE: Asynchronous loading of images is not supported. The caller must preload images or
 * the behavior is undefined.</p>
 */
public class Scale9
{
    /** A horizontal or vertical axis, broken up into 3 chunks. */
    public static class Axis
    {
        /** Creates a new axis equally splitting the given length. */
        public Axis (float length) {
            float d = length / 3;
            _lengths = new float[] {d, length - 2 * d, d};
            _offsets = new float[] {0, _lengths[0], _lengths[0] + _lengths[1]};
        }

        /** Creates a new axis with the given total length and 0th and 2nd lengths copied from a
         * source axis. */
        public Axis (float length, Axis src) {
            _lengths = new float[] {src.size(0), length - (src.size(0) + src.size(2)), src.size(2)};
            _offsets = new float[] {0, src.size(0), length - src.size(2)};
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
        public Axis set (int idx, float coord, float size) {
            _offsets[idx] = coord;
            _lengths[idx] = size;
            return this;
        }

        /** Sets the size of the given chunk, shifting neighbors. */
        public Axis resize (int idx, float size) {
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
                _offsets[2] += excess;
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
     * Ensures that the {@code Axis} passed in does not exceed the length given. An equal chunk
     * will be removed from the outer chunks if it is too long. The given axis is modified and
     * returned.
     */
    public static Axis clamp (Axis axis, float length) {
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

    /** The axes of the 3x3 grid. */
    public final Axis xaxis, yaxis;

    /** Creates a new scale to match the given width and height. Each horizontal and vertical
     * sequence is divided equally between the given values. */
    public Scale9 (float width, float height) {
        xaxis = new Axis(width);
        yaxis = new Axis(height);
    }

    /** Creates a new scale to render the given scale onto a target of the given width and
     * height. */
    public Scale9 (float width, float height, Scale9 source) {
        clamp(xaxis = new Axis(width, source.xaxis), width);
        clamp(yaxis = new Axis(height, source.yaxis), height);
    }
}
