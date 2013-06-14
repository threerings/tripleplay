//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;

import playn.core.Image;
import playn.core.ImmediateLayer;
import playn.core.Surface;

import tripleplay.ui.Background;

/**
 * Displays a background image, centered in the space defined for the background and cropped.
 */
public class CroppedImageBackground extends Background
{
    public CroppedImageBackground (Image image) {
        _image = image;
    }

    @Override protected Instance instantiate (IDimension size) {
        final float swidth = size.width(),   sheight = size.height();
        float iwidth = _image.width(), iheight = _image.height();
        final float sx, sy, dx, dy;
        if (swidth > iwidth) {
            sx = 0;
            dx = (swidth - iwidth)/2;
        } else {
            sx = (iwidth - swidth)/2;
            dx = 0;
        }
        if (sheight > iheight) {
            sy = 0;
            dy = (sheight - iheight)/2;
        } else {
            sy = (iheight - sheight)/2;
            dy = 0;
        }
        return new LayerInstance(size, new ImmediateLayer.Renderer() {
            public void render (Surface surf) {
                if (alpha != null) surf.setAlpha(alpha);
                surf.drawImage(_image, dx, dy, swidth, sheight, sx, sy, swidth, sheight);
            }
        });
    }

    protected final Image _image;
}
