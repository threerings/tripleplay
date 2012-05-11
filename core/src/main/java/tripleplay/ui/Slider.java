//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Canvas;
import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.PlayN;
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

    /**
     * Constrains the possible slider values to the given increment. For example, an increment of
     * 1 would mean the possible sliders values are {@link #min()}, {@code min() + 1}, etc, up to
     * {@link #max()}. Note this only affects internal updates from pointer or mouse handling.
     * The underlying {@link #value} may be updated arbitrarily.
     */
    public Slider setIncrement (float increment) {
        _increment = increment;
        return this;
    }

    /**
     * Sets an image to use as a thumb. The center of the image will be placed on the value of the
     * slider. This will suppress the rendering of the default thumb (a black box).
     */
    public Slider setThumb (Image image) {
        if (image == null) {
            return setThumb(image, 0, 0);
        }
        return setThumb(image, image.width() / 2, image.height() / 2);
    }

    /**
     * Sets an image to use as a thumb, with the given hot spot coordinates. The hot spot of the
     * image will be placed on the value of ths slider. This will suppress the rendering of the
     * default thumb (a black box).
     */
    public Slider setThumb (Image image, float hotspotX, float hotspotY) {
        if (_thumb != null) {
            _thumb.destroy();
            _thumb = null;
        }
        if (image != null) {
            _thumb = PlayN.graphics().createImageLayer(image);
            _thumb.setOrigin(hotspotX, hotspotY);
            layer.add(_thumb);
            _thumb.setDepth(1);
        }
        invalidate();
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

        // the thumb is just an image layer and will be destroyed when we are
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        Dimension dim = new Dimension(
            hintX == 0 ? 100 : hintX,
            _thumb == null ? THUMB_HEIGHT + BAR_HEIGHT : Math.max(BAR_HEIGHT, _thumb.height()));

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

        // render the bar and thumb
        render();

        // position the glyph
        float y = _bg.top;
        if (_thumb != null) y += (_thumb.height() - BAR_HEIGHT) / 2;
        _sglyph.layer().setTranslation(_bg.left, y);
    }

    protected void render () {
        if (_bg == null) {
            // not laid out yet, can't render
            return;
        }

        float width = _size.width - _bg.width();
        float thumb = (value.get() - _min) / _range * width;
        if (_thumb != null) {
            _sglyph.prepare(width, BAR_HEIGHT);
            renderBar(_sglyph.canvas(), 0);
            _thumb.setTranslation(_bg.left + thumb,
                _bg.top + (_size.height - _bg.height()) / 2);
        } else {
            _sglyph.prepare(width, _size.height - _bg.height());
            render(_sglyph.canvas(), thumb);
        }
    }

    /** Renders the bar and default thumb. */
    protected void render (Canvas canvas, float thumbCenterPixel) {
        renderBar(canvas, THUMB_HEIGHT);
        canvas.setFillColor(0xFF000000);
        canvas.fillRect(thumbCenterPixel - THUMB_WIDTH / 2, 0, THUMB_WIDTH, THUMB_HEIGHT);
    }

    /** Renders the bar at the given offset. */
    protected void renderBar (Canvas canvas, float y) {
        canvas.setFillColor(0xFF000000);
        canvas.fillRect(0, y, _size.width - _bg.width(), BAR_HEIGHT); // Bar
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

    protected ImageLayer _thumb;
    protected Background _bg;
    protected Background.Instance _bginst;

    protected Float _increment;

    protected static final float BAR_HEIGHT = 5;
    protected static final float THUMB_HEIGHT = BAR_HEIGHT * 2, THUMB_WIDTH = 4;
}
