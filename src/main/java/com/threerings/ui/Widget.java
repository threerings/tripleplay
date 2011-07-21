//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import forplay.core.Canvas;
import forplay.core.CanvasLayer;
import forplay.core.ForPlay;
import forplay.core.Layer;

/**
 * The base class for all user interface widgets. Manages the canvas into which a widget is
 * rendered when its state changes, and interacts with the input manager.
 */
public abstract class Widget extends Element
{
    @Override protected void layout () {
        // recreate our canvas if we need more room than we have (TODO: should we ever shrink it?)
        int cwidth = (int)Math.ceil(_size.width), cheight = (int)Math.ceil(_size.height);
        if (_layer == null ||
            _layer.canvas().width() < cwidth || _layer.canvas().height() < cheight) {
            if (_layer != null) _layer.destroy();
            Layer oldLayer = _layer;
            _layer = ForPlay.graphics().createCanvasLayer(cwidth, cheight);
            if (_parent != null) _parent.layerChanged(this, oldLayer, _layer);
        } else {
            _layer.canvas().clear();
        }

        // rerender ourselves into this new canvas
        render(_layer.canvas());
    }

    @Override protected Layer layer () {
        return _layer;
    }

    /**
     * Rerenders this widget into the supplied canvas.
     */
    protected abstract void render (Canvas canvas);

    protected CanvasLayer _layer;
}
