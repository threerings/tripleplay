//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Canvas;
import playn.core.Pointer;

import pythagoras.f.Dimension;

import react.UnitSlot;
import react.Value;

public class Slider extends Widget<Slider>
{
    /** The value of the slider. */
    public final Value<Float> value;

    public Slider (float value, float min, float max) {
        enableInteraction();
        this.value = Value.create(value);
        _min = min;
        _max = max;
        _range = _max - _min;
        this.value.connect(new UnitSlot () {
            @Override public void onEmit () {
                if (isAdded()) {
                    render();
                }
            }
        });
    }

    public Slider setIncrement (float increment) {
        _increment = increment;
        return this;
    }

    public float max () { return _max; }

    public float min () { return _min; }

    @Override protected void wasAdded (Elements<?> parent) {
        super.wasAdded(parent);
        invalidate();
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        if (_bginst != null) {
            _bginst.destroy();
            _bginst = null;
        }
        _sglyph.destroy();
        _bg = null;
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        Dimension dim = new Dimension(hintX, THUMB_HEIGHT + BAR_HEIGHT);
        if (dim.width == 0) dim.width = 100;

        // determine our background
        return resolveStyle(Style.BACKGROUND).addInsets(dim);
    }

    @Override protected void layout () {
        // determine our background
        _bg = resolveStyle(Style.BACKGROUND);
        if (_bginst != null) _bginst.destroy();
        if (_size.width > 0 && _size.height > 0) {
            _bginst = _bg.instantiate(_size);
            _bginst.addTo(layer);
        }

        render();
        _sglyph.layer().setTranslation(_bg.left, _bg.top);
    }

    protected void render () {
        if (_bg == null) {
            // not laid out yet, can't render
            return;
        }

        float width = _size.width - _bg.width();
        _sglyph.prepare(width, _size.height - _bg.height());
        render(_sglyph.canvas(), (value.get() - _min) / _range * width);
    }

    protected void render (Canvas canvas, float thumbCenterPixel) {
        canvas.setFillColor(0xFF000000);
        canvas.fillRect(0, THUMB_HEIGHT, _size.width - _bg.width(), BAR_HEIGHT); // Bar
        canvas.fillRect(thumbCenterPixel - THUMB_WIDTH / 2, 0, THUMB_WIDTH, THUMB_HEIGHT);
    }

    @Override protected void onPointerStart (Pointer.Event event, float x, float y) {
        super.onPointerStart(event, x, y);
        handlePointer(x, y);
    }

    @Override protected void onPointerDrag (Pointer.Event event, float x, float y) {
        super.onPointerDrag(event, x, y);
        handlePointer(x, y);
    }

    @Override protected void onPointerEnd (Pointer.Event event, float x, float y) {
        super.onPointerEnd(event, x, y);
        handlePointer(x, y);
    }

    protected void handlePointer (float x, float y) {
        if (_bg == null || !contains(x, y)) { return; }
        float width = _size.width - _bg.width();
        x = Math.min(width,  x - _bg.left);
        float pos = Math.max(x, 0) / width * _range;
        if (_increment != null) {
            float i = _increment;
            pos = i * Math.round(pos / i);
        }
        value.update(_min + pos);
    }

    protected final float _min, _max, _range;
    protected final Glyph _sglyph = new Glyph();
    protected Background _bg;
    protected Background.Instance _bginst;

    protected Float _increment;

    protected static final float BAR_HEIGHT = 5;
    protected static final float THUMB_HEIGHT = BAR_HEIGHT * 2, THUMB_WIDTH = 4;
}
