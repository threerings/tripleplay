//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import pythagoras.f.IDimension;

import react.Closeable;

import playn.core.Canvas;
import playn.core.Graphics;
import playn.scene.CanvasLayer;
import playn.scene.GroupLayer;
import playn.scene.ImageLayer;

/**
 * Handles the maintenance of a canvas image and layer for displaying a chunk of pre-rendered
 * graphics.
 */
public class Glyph implements Closeable
{
    public Glyph (GroupLayer parent) {
        _parent = parent;
        _depth = null;
    }

    public Glyph (GroupLayer parent, float depth) {
        _parent = parent;
        _depth = depth;
    }

    /** Ensures that the canvas image is at least the specified dimensions and cleared to all
      * transparent pixels. Also creates and adds the image layer to the parent layer if needed. */
    public void prepare (Graphics gfx, IDimension dim) {
        prepare(gfx, dim.width(), dim.height());
    }

    /** Ensures that the canvas image is at least the specified dimensions and cleared to all
      * transparent pixels. Also creates and adds the image layer to the parent layer if needed. */
    public void prepare (Graphics gfx, float width, float height) {
        CanvasLayer layer = _layer;
        if (layer == null) {
            layer = new CanvasLayer(gfx, width, height);
            if (_depth != null) layer.setDepth(_depth);
            _parent.add(layer);
            _layer = layer;
        } else if (layer.width() != width || layer.height() != height) {
            layer.resize(width, height);
        }
        _preparedWidth = width;
        _preparedHeight = height;
    }

    /** Returns the layer that contains our glyph image. Valid after {@link #prepare}. */
    public ImageLayer layer () {
        return _layer;
    }

    /** Starts a drawing session into this glyph's canvas. Call {@link #end} when drawing is done.
      * Valid after {@link #prepare}. */
    public Canvas begin () {
        return _layer.begin().clear();
    }

    /** Completes a drawing sesion into this glyph's canvas and uploads the image data to GPU */
    public void end () {
        _layer.end();
    }

    /** Disposes the layer and image, removing them from the containing widget. */
    @Override public void close () {
        if (_layer != null) {
            _layer.close();
            _layer = null;
        }
        _preparedWidth = 0;
        _preparedHeight = 0;
    }

    /**
     * Returns the width of the last call to {@link #prepare}, or zero if the glyph is not
     * prepared. The canvas should be at least this width, or null if the glyph is not prepared.
     */
    public float preparedWidth () {
        return _preparedWidth;
    }

    /**
     * Returns the height of the last call to {@link #prepare}, or zero if the glyph is not
     * prepared. The canvas should be at least this height, or null if the glyph is not prepared.
     */
    public float preparedHeight () {
        return _preparedHeight;
    }

    /**
     * Prepares the canvas and renders the supplied text at 0, 0 using the given config.
     */
    public void renderText (Graphics gfx, StyledText.Plain text) {
        renderText(gfx, text, 0, 0);
    }

    /**
     * Prepares the canvas and renders the supplied text at {@code x, y} using the given config.
     */
    public void renderText (Graphics gfx, StyledText.Plain text, int x, int y) {
        prepare(gfx, text.width(), text.height());
        Canvas canvas = begin();
        text.render(canvas, x, y);
        end();
        _layer.setTranslation(text.style.effect.offsetX(), text.style.effect.offsetY());
    }

    protected final GroupLayer _parent;
    protected final Float _depth;
    protected CanvasLayer _layer;
    protected float _preparedWidth, _preparedHeight;
}
