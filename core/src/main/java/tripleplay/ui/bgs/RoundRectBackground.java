//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;

import playn.core.CanvasImage;
import playn.core.ImageLayer;
import static playn.core.PlayN.graphics;

import tripleplay.ui.Background;

/**
 * Draws a rounded rectangle with optional border as a background.
 */
public class RoundRectBackground extends Background
{
    public RoundRectBackground (int bgColor, float radius) {
        this(bgColor, radius, 0, 0);
    }

    public RoundRectBackground (int bgColor, float radius, int borderColor, float borderWidth) {
        this(bgColor, radius, borderColor, borderWidth, radius);
    }

    public RoundRectBackground (int bgColor, float radius,
                                int borderColor, float borderWidth, float borderRadius) {
        _bgColor = bgColor;
        _radius = radius;
        _borderColor = borderColor;
        _borderWidth = borderWidth;
        _borderRadius = borderRadius;
    }

    @Override
    protected Instance instantiate (final IDimension size) {
        CanvasImage image = graphics().createImage(size.width(), size.height());
        if (_borderWidth > 0) {
            image.canvas().setFillColor(_borderColor);
            image.canvas().fillRoundRect(0, 0, size.width(), size.height(), _radius);
            // scale the inner radius based on the ratio of the inner height to the full height;
            // this improves the uniformity of the border substantially
            float iwidth = size.width() - 2*_borderWidth, iheight = size.height() - 2*_borderWidth;
            float iradius = _borderRadius * (iheight / size.height());
            image.canvas().setFillColor(_bgColor);
            image.canvas().fillRoundRect(_borderWidth, _borderWidth, iwidth, iheight, iradius);
        } else {
            image.canvas().setFillColor(_bgColor);
            image.canvas().fillRoundRect(0, 0, size.width(), size.height(), _radius);
        }
        ImageLayer layer = graphics().createImageLayer(image);
        if (alpha != null) layer.setAlpha(alpha);
        return new LayerInstance(size, layer);
    }

    protected final int _bgColor, _borderColor;
    protected final float _radius, _borderWidth, _borderRadius;
}
