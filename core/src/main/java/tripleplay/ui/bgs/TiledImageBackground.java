//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;

import playn.core.Image;
import playn.core.ImmediateLayer;
import playn.core.Layer;
import playn.core.Surface;
import static playn.core.PlayN.graphics;

import tripleplay.ui.Background;

/**
 * Displays a background image, tiled in the space defined for the background.
 */
public class TiledImageBackground extends Background
{
    public TiledImageBackground (Image image) {
        _image = image;
    }

    @Override protected Instance instantiate (final IDimension size) {
        Layer layer = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            public void render (Surface surf) {
                if (alpha != null) surf.setAlpha(alpha);
                for (float y = 0; y < size.height(); y += _image.height()) {
                    for (float x = 0; x < size.width(); x += _image.width()) {
                        surf.drawImage(_image, x, y);
                    }
                }
            }
        });
        return new LayerInstance(size, layer);
    }

    protected final Image _image;
}
