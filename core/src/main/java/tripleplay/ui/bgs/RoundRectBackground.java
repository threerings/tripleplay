//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;

import playn.core.Canvas;
import playn.core.Graphics;
import playn.scene.ImageLayer;

import tripleplay.ui.Background;

/**
 * Draws a rounded rectangle with optional border as a background.
 */
public class RoundRectBackground extends Background
{
    public RoundRectBackground (Graphics gfx, int bgColor, float radius) {
        this(gfx, bgColor, radius, 0, 0);
    }

    public RoundRectBackground (Graphics gfx, int bgColor, float radius,
                                int borderColor, float borderWidth) {
        this(gfx, bgColor, radius, borderColor, borderWidth, radius);
    }

    public RoundRectBackground (Graphics gfx, int bgColor, float radius,
                                int borderColor, float borderWidth, float borderRadius) {
        _gfx = gfx;
        _bgColor = bgColor;
        _radius = radius;
        _borderColor = borderColor;
        _borderWidth = borderWidth;
        _borderRadius = borderRadius;
    }

    @Override
    protected Instance instantiate (final IDimension size) {
        Canvas canvas = _gfx.createCanvas(size);
        if (_borderWidth > 0) {
            canvas.setFillColor(_borderColor).fillRoundRect(
                0, 0, size.width(), size.height(), _radius);
            // scale the inner radius based on the ratio of the inner height to the full height;
            // this improves the uniformity of the border substantially
            float iwidth = size.width() - 2*_borderWidth, iheight = size.height() - 2*_borderWidth;
            float iradius = _borderRadius * (iheight / size.height());
            canvas.setFillColor(_bgColor).fillRoundRect(
                _borderWidth, _borderWidth, iwidth, iheight, iradius);
        } else {
            canvas.setFillColor(_bgColor).fillRoundRect(0, 0, size.width(), size.height(), _radius);
        }
        ImageLayer layer = new ImageLayer(canvas.toTexture());
        return new LayerInstance(size, layer);
    }

    protected final Graphics _gfx;
    protected final int _bgColor, _borderColor;
    protected final float _radius, _borderWidth, _borderRadius;
}
