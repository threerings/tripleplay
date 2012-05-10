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
import playn.core.ResourceCallback;
import static playn.core.PlayN.*;

import tripleplay.util.PackedFrames;
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

        // test our simple frames
        Image image = assets().getImage("images/spritesheet.png");

        ImageLayer layer = graphics().createImageLayer();
        // ImageLayer layer = graphics().createImageLayer(image);
        SimpleFrames frames = new SimpleFrames(image, 60, 60, 60);
        _anim.repeat(layer).flipbook(layer, new Flipbook(frames, 66));

        GroupLayer box = graphics().createGroupLayer();
        box.add(layer);
        graphics().rootLayer().addAt(box, 0, 100);

        _anim.repeat(box).tweenX(box).to(width-frames.width()).in(2000).easeInOut().then().
            tweenX(box).to(0).in(2000).easeInOut();

        // test our packed frames
        assets().getText("images/packed.json", new ResourceCallback<String>() {
            public void done (String json) {
                GroupLayer box = graphics().createGroupLayer();
                graphics().rootLayer().addAt(box, 200, 200);
                Image image = assets().getImage("images/packed.png");
                _anim.repeat(box).flipbook(
                    box, new Flipbook(new PackedFrames(image, json().parse(json)), 99)).then().
                    setVisible(box, false).then().delay(500).then().setVisible(box, true);
            }

            public void error (Throwable t) {
                t.printStackTrace(System.err);
            }
        });
    }
}
