//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;

import playn.core.ImmediateLayer;
import playn.core.Surface;
import static playn.core.PlayN.graphics;

import tripleplay.ui.Background;

/**
 * A background that shows a line around a solid color.
 */
public class BorderedBackground extends Background
{
    public BorderedBackground (int bgColor, int borderColor, float thickness) {
        _bgColor = bgColor;
        _borderColor = borderColor;
        _thickness = thickness;
    }

    @Override
    protected Instance instantiate (final IDimension size) {
        return new LayerInstance(graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            public void render (Surface surf) {
                float width = size.width(), height = size.height();
                float bot = height-1, right = width-1;
                surf.setAlpha(alpha);
                surf.setFillColor(_bgColor).fillRect(0, 0, width, height);
                surf.setFillColor(_borderColor).
                    drawLine(0, 0, right, 0, _thickness).
                    drawLine(right, 0, right, bot, _thickness).
                    drawLine(right, bot, 0, bot, _thickness).
                    drawLine(0, bot, 0, 0, _thickness);
                surf.setAlpha(1);
            }
        }));
    }

    protected final int _bgColor, _borderColor;
    protected final float _thickness;
}
