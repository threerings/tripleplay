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
import pythagoras.f.Rectangle;

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

    /** Returns our maximum allowed value. */
    public float max () { return _max; }

    /** Returns our minimum allowed value. */
    public float min () { return _min; }

    @Override protected void wasAdded (Elements<?> parent) {
        super.wasAdded(parent);
        invalidate();
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        _sglyph.destroy();
        // the thumb is just an image layer and will be destroyed when we are
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new LayoutData() {
            @Override public Dimension computeSize (float hintX, float hintY) {
                return new Dimension(
                    hintX == 0 ? 100 : hintX,
                    _thumb == null ? THUMB_HEIGHT + BAR_HEIGHT :
                    Math.max(BAR_HEIGHT, _thumb.height()));
            }

            @Override public void layout (float left, float top, float width, float height) {
                _tbounds = new Rectangle(left, top, width, height);
                render(); // render the bar and thumb

                // position the glyph
                float y = top;
                if (_thumb != null) y += (_thumb.height() - BAR_HEIGHT) / 2;
                _sglyph.layer().setTranslation(left, y);
            }
        };
    }

    protected void render () {
        if (_tbounds == null) return; // not laid out yet, can't render

        float width = _tbounds.width;
        float thumb = (value.get() - _min) / _range * width;
        if (_thumb != null) {
            _sglyph.prepare(width, BAR_HEIGHT);
            renderBar(_sglyph.canvas(), 0);
            _thumb.setTranslation(_tbounds.x + thumb, _tbounds.y + (_tbounds.height) / 2);
        } else {
            _sglyph.prepare(width, _tbounds.height);
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
        canvas.fillRect(0, y, _tbounds.width, BAR_HEIGHT); // Bar
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
        if (_tbounds == null || !contains(x, y)) { return; }
        float width = _tbounds.width;
        x = Math.min(width,  x - _tbounds.x);
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
    protected Rectangle _tbounds;
    protected Float _increment;

    protected static final float BAR_HEIGHT = 5;
    protected static final float THUMB_HEIGHT = BAR_HEIGHT * 2, THUMB_WIDTH = 4;
}
