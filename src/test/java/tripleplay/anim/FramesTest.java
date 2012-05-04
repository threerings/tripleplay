//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.Image;
import playn.core.ImmediateLayer;
import playn.core.Surface;
import static playn.core.PlayN.*;

import tripleplay.util.SimpleFrames;

class FramesTest extends AnimTests.Test
{
    public void init () {
        final float width = graphics().width(), height = graphics().height();
        ImmediateLayer bg = graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            public void render (Surface surf) {
                surf.setFillColor(0xFFCCCCCC);
                surf.fillRect(0, 0, width, height);
            }
        });
        bg.setDepth(-1);
        graphics().rootLayer().add(bg);

        Image image = assets().getImage("images/spritesheet.png");
        SimpleFrames frames = new SimpleFrames(image, 60, 60, 60);

        ImageLayer layer = graphics().createImageLayer(frames.frame(0));
        // ImageLayer layer = graphics().createImageLayer(image);
        _anim.repeat(layer).flipbook(layer, frames, 66);

        GroupLayer box = graphics().createGroupLayer();
        box.add(layer);
        graphics().rootLayer().addAt(box, 0, 100);

        _anim.repeat(box).tweenX(box).to(width-layer.width()).in(2000).easeInOut().then().
            tweenX(box).to(0).in(2000).easeInOut();
    }
}
