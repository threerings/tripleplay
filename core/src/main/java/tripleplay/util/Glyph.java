//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.PlayN;
import playn.core.TextLayout;
import pythagoras.f.IDimension;

/**
 * Handles the maintenance of a canvas image and layer for displaying a chunk of pre-rendered
 * graphics.
 */
public class Glyph
    implements Destroyable
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
     * transparent pixels. Also creates and adds the image layer to the parent layer if
     * needed. */
    public void prepare (IDimension dim) {
        prepare(dim.width(), dim.height());
    }

    /** Ensures that the canvas image is at least the specified dimensions and cleared to all
     * transparent pixels. Also creates and adds the image layer to the parent layer if
     * needed. */
    public void prepare (float width, float height) {
        // recreate our canvas if we need more room than we have (TODO: should we ever shrink it?)
        ImageLayer layer = _layer.get();
        if (_image == null || _image.width() < width || _image.height() < height) {
            _image = PlayN.graphics().createImage(width, height);
            if (layer != null) layer.setImage(_image);
        } else {
            _image.canvas().clear();
        }
        if (layer == null) {
            layer = _layer.set(PlayN.graphics().createImageLayer(_image));
            if (_depth != null) layer.setDepth(_depth);
            _parent.add(layer);
        }
        _preparedWidth = width;
        _preparedHeight = height;
    }

    /** Returns the layer that contains our glyph image. Valid after {@link #prepare}. */
    public ImageLayer layer () {
        return _layer.get();
    }

    /** Returns the canvas into which drawing may be done. Valid after {@link #prepare}. */
    public Canvas canvas () {
        return _image.canvas();
    }

    /** Destroys the layer and image, removing them from the containing widget. */
    @Override public void destroy () {
        _layer.clear();
        _image = null;
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
    public void renderText (StyledText.Plain text) {
        prepare(text.width(), text.height());
        text.render(canvas(), 0, 0);
        _layer.get().setTranslation(text.style.effect.offsetX(), text.style.effect.offsetY());
    }

    /** @deprecated Use {@link #renderText(StyledText)}. */
    @Deprecated public void renderText (TextConfig config, String text) {
        renderText(config, config.layout(text));
    }

    /** @deprecated Use {@link #renderText(StyledText)}. */
    @Deprecated public void renderText (TextConfig config, TextLayout layout) {
        prepare(config.effect.adjustWidth(layout.width()),
                config.effect.adjustHeight(layout.height()));
        config.render(canvas(), layout, 0, 0);
        _layer.get().setTranslation(config.effect.offsetX(), config.effect.offsetY());
    }

    protected final GroupLayer _parent;
    protected final Float _depth;
    protected CanvasImage _image;
    protected Ref<ImageLayer> _layer = Ref.<ImageLayer>create(null);
    protected float _preparedWidth, _preparedHeight;
}
