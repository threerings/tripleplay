//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.Layer;
import playn.core.util.Callback;
import static playn.core.PlayN.graphics;

/**
 * Contains icon related utility methods, mostly basic icon factories.
 */
public class Icons
{
    /**
     * Creates an icon using the supplied image.
     */
    public static Icon image (final Image image) {
        return new Icon() {
            @Override public float width () {
                return image.width();
            }
            @Override public float height () {
                return image.height();
            }
            @Override public Layer render () {
                return graphics().createImageLayer(image);
            }
            @Override public void addCallback (final Callback<? super Icon> callback) {
                final Icon icon = this;
                image.addCallback(new Callback<Image>() {
                    @Override public void onSuccess (Image result) {
                        callback.onSuccess(icon);
                    }
                    @Override public void onFailure (Throwable cause) {
                        callback.onFailure(cause);
                    }
                });
            }
        };
    }
}
