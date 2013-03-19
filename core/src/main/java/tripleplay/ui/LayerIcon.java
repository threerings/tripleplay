//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Layer;
import playn.core.util.Callback;

/** A label icon that displays a layer. */
public class LayerIcon
    implements Icon
{
    public LayerIcon (Layer.HasSize layer) {
        this(layer, layer.width(), layer.height());
    }

    public LayerIcon (Layer layer, float width, float height) {
        _layer = layer;
        _width = width;
        _height = height;
    }

    @Override public float width () {
        return _width;
    }

    @Override public float height () {
        return _height;
    }

    @Override public Layer render () {
        return _layer;
    }

    @Override public void addCallback (Callback<? super Icon> callback) {
        callback.onSuccess(this);
    }

    protected Layer _layer;
    protected float _width, _height;
}
