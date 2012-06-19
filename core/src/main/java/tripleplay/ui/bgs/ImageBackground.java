//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;

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
        _image = image;
    }

    @Override protected Instance instantiate (IDimension size) {
        ImageLayer layer = PlayN.graphics().createImageLayer(_image);
        if (alpha != null) layer.setAlpha(alpha);
        layer.setSize(size.width(), size.height());
        return new LayerInstance(layer);
    }

    protected final Image _image;
}
