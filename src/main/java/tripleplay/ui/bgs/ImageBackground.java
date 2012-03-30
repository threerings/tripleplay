//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;
import pythagoras.f.IRectangle;

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.PlayN;

import tripleplay.ui.Background;

/**
 * A background that displays an image.
 */
public class ImageBackground extends Background
{
    public ImageBackground (Image image) {
        this(image, null);
    }

    public ImageBackground (Image image, IRectangle sourceRect) {
        _image = image;
        _sourceRect = sourceRect;
    }

    @Override protected Instance instantiate (IDimension size) {
        ImageLayer layer = PlayN.graphics().createImageLayer(_image);
        if (_sourceRect != null) {
            layer.setSourceRect(_sourceRect.x(), _sourceRect.y(),
                                _sourceRect.width(), _sourceRect.height());
        }
        layer.setSize(size.width(), size.height());
        return new LayerInstance(layer);
    }

    protected final Image _image;
    protected final IRectangle _sourceRect;
}
