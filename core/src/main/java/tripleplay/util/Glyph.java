//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.PlayN;
import pythagoras.f.FloatMath;

/**
 * Handles the maintenance of a canvas image and layer for displaying a chunk of pre-rendered
 * graphics.
 */
public class Glyph
{
    public Glyph (GroupLayer parent) {
        _parent = parent;
    }

    /** Ensures that the canvas image is at least the specified dimensions and cleared to all
     * transparent pixels. Also creates and adds the image layer to the containing widget if
     * needed. */
    public void prepare (float width, float height) {
        // recreate our canvas if we need more room than we have (TODO: should we ever shrink it?)
        int cwidth = FloatMath.iceil(width), cheight = FloatMath.iceil(height);
        if (_image == null || _image.width() < cwidth || _image.height() < cheight) {
            _image = PlayN.graphics().createImage(cwidth, cheight);
            if (_layer != null) _layer.setImage(_image);
        } else {
            _image.canvas().clear();
        }
        if (_layer == null) _parent.add(_layer = PlayN.graphics().createImageLayer(_image));
    }

    /** Returns the layer that contains our glyph image. Valid after {@link #prepare}. */
    public ImageLayer layer () {
        return _layer;
    }

    /** Returns the canvas into which drawing may be done. Valid after {@link #prepare}. */
    public Canvas canvas () {
        return _image.canvas();
    }

    /** Destroys the layer and image, removing them from the containing widget. */
    public void destroy () {
        if (_layer != null) {
            _layer.destroy();
            _layer = null;
        }
        _image = null;
    }

    protected final GroupLayer _parent;
    protected CanvasImage _image;
    protected ImageLayer _layer;
}
