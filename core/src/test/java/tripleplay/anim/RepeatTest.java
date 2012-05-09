//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import playn.core.CanvasImage;
import playn.core.ImageLayer;
import static playn.core.PlayN.*;

class RepeatTest extends AnimTests.Test
{
    public void init () {
        CanvasImage image = graphics().createImage(100, 100);
        image.canvas().setFillColor(0xFFFFCC99);
        image.canvas().fillCircle(50, 50, 50);
        ImageLayer layer = graphics().createImageLayer(image);
        graphics().rootLayer().addAt(layer, 0, 100);

        float width = graphics().width();
        _anim.repeat(layer).tweenX(layer).to(width-100).in(1000).easeInOut().then().
            tweenX(layer).to(0).in(1000).easeInOut();
    }
}
