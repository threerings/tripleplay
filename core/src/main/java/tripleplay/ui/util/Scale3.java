//
//Triple Play - utilities for use in PlayN-based games
//Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
//http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;


/**
* Facilitates the rendering of "scale-3" images, that is, images that are designed as a 3x1 grid
* such that each of the 3 pieces are fixed or stretched horizontally to fit a designated area. The
* left and right center pieces are stretched vertically, and the center piece is scaled both
* horizontally and vertically.
* <p>By default, the cells are assumed to be of equal size (hence scale-3 image widths are
* normally a multiple of 3). By using {@link #xaxis}, this partitioning can be controlled directly.
* For example, if the horizontal middle of an image is a single pixel, this code will do that and
* automatically grow the left and right edges if necessary:
* <pre><code>
*     Scale3 s3 = ...;
*     s3.xaxis.resize(1, 1);
* </code></pre></p>
* <p>NOTE: Asynchronous loading of images is not supported. The caller must preload images or
* the behavior is undefined.</p>
*/
public class Scale3
{     /** The x-axis of the 3x1 grid. */
     public final Axis xaxis;

     /** The height of the 3x1 grid. */
     public float height;

     /** Creates a new scale to match the given width and height. Each horizontal
      * sequence is divided equally between the given values. */
     public Scale3 (float width, float height) {
         xaxis = new Axis(width);
         this.height = height;
     }

     /** Creates a new scale to render the given scale onto a target of the given width and
      * height. */
     public Scale3 (float width, float height, Scale3 source) {
         Axis.clamp(xaxis = new Axis(width, source.xaxis), width);
         this.height = height;
     }
}
