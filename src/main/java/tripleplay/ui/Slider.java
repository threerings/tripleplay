//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Canvas;
import playn.core.CanvasLayer;

import pythagoras.f.Dimension;

import react.UnitSlot;
import react.Value;
import react.ValueView;

public class Slider extends Widget<Slider>
{
    /** The value of the slider. */
    public final Value<Float> value;

    public Slider(float value, float min, float max) {
        this.value = Value.create(value);
        _min = min;
        _max = max;
        _range = _max - _min;
        this.value.connect(new UnitSlot () {
            @Override public void onEmit () {
                invalidate();
            }
        });
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        return new Dimension(hintX == 0 ? 100 : hintX, THUMB_HEIGHT + BAR_HEIGHT);
    }

    @Override protected void layout () {
        float width = _size.width, height = _size.height;

        _sliderLayer = prepareCanvas(_sliderLayer, width, height);
        render(_sliderLayer.canvas(), (value.get() - _min) / _range * width);
    }

    protected void render (Canvas canvas, float thumbCenterPixel) {
        canvas.setFillColor(0xFF000000);
        canvas.fillRect(0, THUMB_HEIGHT, _size.width(), BAR_HEIGHT);// Bar
        canvas.fillRect(thumbCenterPixel - THUMB_WIDTH / 2, 0, THUMB_WIDTH, THUMB_HEIGHT);
    }

    @Override protected void onPointerStart (float x, float y) {
        super.onPointerStart(x, y);
        handlePointer(x, y);
    }

    @Override protected void onPointerDrag (float x, float y) {
        super.onPointerDrag(x, y);
        handlePointer(x, y);
    }

    @Override protected void onPointerEnd (float x, float y) {
        super.onPointerEnd(x, y);
        handlePointer(x, y);
    }

    protected void handlePointer (float x, float y) {
        if (!contains(x, y)) { return; }
        value.update(Math.max(x, 0) / size().width() * _range + _min);
    }

    protected CanvasLayer _sliderLayer;
    protected final float _min, _max, _range;

    protected static final float BAR_HEIGHT = 5;
    protected static final float THUMB_HEIGHT = BAR_HEIGHT * 2, THUMB_WIDTH = 4;
}
