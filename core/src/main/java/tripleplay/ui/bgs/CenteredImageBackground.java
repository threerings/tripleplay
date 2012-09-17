//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;
import pythagoras.f.MathUtil;

import playn.core.Image;
import playn.core.ImmediateLayer;
import playn.core.Surface;
import static playn.core.PlayN.graphics;

import tripleplay.ui.Background;

/**
 * Displays a background image, centered in the space defined for the background. BEWARE: this may
 * mean blank space around the edges or that the background spills over the edges of the element's
 * bounds. You probably want {@link Scale9Background} or {@link ImageBackground}. This background
 * is only useful in special circumstances.
 */
public class CenteredImageBackground extends Background
{
    public CenteredImageBackground (Image image) {
        _image = image;
    }

    @Override protected Instance instantiate (IDimension size) {
        final float x = MathUtil.ifloor((size.width()-_image.width())/2);
        final float y = MathUtil.ifloor((size.height()-_image.height())/2);
        return new LayerInstance(graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            public void render (Surface surf) {
                if (alpha != null) surf.setAlpha(alpha);
                surf.drawImage(_image, x, y);
            }
        }));
    }

    protected final Image _image;
}
