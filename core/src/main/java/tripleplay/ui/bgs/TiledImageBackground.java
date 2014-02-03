//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
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
                // avoid rendering an unready image (which will have zero width/height) which will
                // cause the infinite loopage
                if (!_image.isReady()) return;
                if (alpha != null) surf.setAlpha(alpha);
                try {
                    float width = size.width(), height = size.height();
                    for (float y = 0; y < height; y += _image.height()) {
                        float h = Math.min(height-y, _image.height());
                        for (float x = 0; x < width; x += _image.width()) {
                            float w = Math.min(width-x, _image.width());
                            surf.drawImage(_image, x, y, w, h, 0, 0, w, h);
                        }
                    }
                } finally {
                    if (alpha != null) surf.setAlpha(1);
                }
            }
        });
        return new LayerInstance(size, layer);
    }

    protected final Image _image;
}
