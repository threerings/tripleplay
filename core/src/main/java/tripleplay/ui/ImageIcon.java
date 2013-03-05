//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.util.Callback;

public class ImageIcon implements Icon
{
    public ImageIcon (Image image) {
        _image = image;
    }

    @Override public float width () {
        return _image.width();
    }

    @Override public float height () {
        return _image.height();
    }

    @Override public Layer layer () {
        return (_layer == null || _layer.destroyed()) ?
            (_layer = PlayN.graphics().createImageLayer(_image)) : _layer;
    }

    @Override public void addCallback (final Callback<? super Icon> callback) {
        _image.addCallback(new Callback<Image>() {
            @Override public void onSuccess (Image result) {
                callback.onSuccess(ImageIcon.this);
            }

            @Override public void onFailure (Throwable cause) {
                callback.onFailure(cause);
            }
        });
    }

    protected ImageLayer _layer;
    protected final Image _image;
}
