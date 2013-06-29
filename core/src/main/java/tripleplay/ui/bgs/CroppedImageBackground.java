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
 * Displays a background image, centered in the space defined for the background and cropped. If
 * the image is smaller than the needed space, in either dimension, it will be tiled.
 */
public class CroppedImageBackground extends Background
{
    public CroppedImageBackground (Image image) {
        _image = image;
    }

    @Override protected Instance instantiate (IDimension size) {
        final float swidth = size.width(),   sheight = size.height();
        final float iwidth = _image.width(), iheight = _image.height();
        final float cwidth = Math.min(swidth, iwidth), cheight = Math.min(sheight, iheight);
        final float sx, sy;
        if (swidth > iwidth) {
            sx = 0;
        } else {
            sx = (iwidth - swidth)/2;
        }
        if (sheight > iheight) {
            sy = 0;
        } else {
            sy = (iheight - sheight)/2;
        }
        return new LayerInstance(size, new ImmediateLayer.Renderer() {
            public void render (Surface surf) {
                if (alpha != null) surf.setAlpha(alpha);
                float dy = 0;
                while (dy < sheight) {
                    float dheight = Math.min(cheight, sheight-dy);
                    float dx = 0;
                    while (dx < swidth) {
                        float dwidth = Math.min(cwidth, swidth-dx);
                        surf.drawImage(_image, dx, dy, dwidth, dheight, sx, sy, dwidth, dheight);
                        dx += cwidth;
                    }
                    dy += cheight;
                }
            }
        });
    }

    protected final Image _image;
}
