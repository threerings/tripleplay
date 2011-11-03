//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.FloatMath;
import pythagoras.f.IDimension;

import playn.core.Canvas;
import playn.core.CanvasLayer;
import playn.core.PlayN;

import tripleplay.ui.Background;

/**
 * A background that displays a bevel around a solid color.
 */
public class BeveledBackground extends Background
{
    /**
     * Creates a beveled background with the specified colors and insets.
     */
    public BeveledBackground (int bgColor, int ulColor, int brColor,
                              float top, float right, float bottom, float left) {
        super(top, right, bottom, left);
        _bgColor = bgColor;
        _ulColor = ulColor;
        _brColor = brColor;
    }

    @Override protected Instance instantiate (IDimension size) {
        // TODO: rewrite this as an active-rendered layer when PlayN supports such things
        float width = size.width(), height = size.height();
        int cwidth = FloatMath.iceil(width), cheight = FloatMath.iceil(height);
        CanvasLayer layer = PlayN.graphics().createCanvasLayer(cwidth, cheight);
        Canvas canvas = layer.canvas();
        float bot = height-1, right=width-1;
        canvas.setFillColor(_bgColor).fillRect(0, 0, width, height);
        canvas.setStrokeColor(_ulColor).
            drawLine(0, 0, right, 0).drawLine(0, 1, right-1, 1).
            drawLine(0, 0, 0, bot).drawLine(1, 0, 1, bot-1);
        canvas.setStrokeColor(_brColor).
            drawLine(right, 0, right, bot).drawLine(1, bot-1, right-1, bot-1).
            drawLine(0, bot, right, bot).drawLine(right-1, 1, right-1, bot-1);
        return new LayerInstance(layer);
    }

    protected int _bgColor, _ulColor, _brColor;
}
