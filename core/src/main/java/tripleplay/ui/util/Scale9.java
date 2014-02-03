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
        Axis.clamp(xaxis = new Axis(width, source.xaxis), width);
        Axis.clamp(yaxis = new Axis(height, source.yaxis), height);
    }
}
