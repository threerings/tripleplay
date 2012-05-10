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
        GroupLayer box = graphics().createGroupLayer();
        graphics().rootLayer().addAt(box, 0, 100);
        Image image = assets().getImage("images/spritesheet.png");
        SimpleFrames frames = new SimpleFrames(image, 60, 60, 60);
        _anim.repeat(box).flipbook(box, new Flipbook(frames, 66));
        _anim.repeat(box).tweenX(box).to(width-frames.width()).in(2000).easeInOut().then().
            tweenX(box).to(0).in(2000).easeInOut();

        // test our packed frames
        final Image packed = assets().getImage("images/packed.png");
        assets().getText("images/packed.json", new ResourceCallback<String>() {
            public void done (String json) {
                GroupLayer box = graphics().createGroupLayer();
                graphics().rootLayer().addAt(box, 100, 200);
                _anim.repeat(box).flipbook(
                    box, new Flipbook(new PackedFrames(packed, json().parse(json)), 99)).then().
                    setVisible(box, false).then().delay(500).then().setVisible(box, true);
            }

            public void error (Throwable t) {
                t.printStackTrace(System.err);
            }
        });
        GroupLayer pbox = graphics().createGroupLayer();
        graphics().rootLayer().addAt(pbox, 300, 200);
        _anim.repeat(pbox).flipbook(
            pbox, new Flipbook(new PackedFrames(packed, PACKED), 99)).then().
            setVisible(pbox, false).then().delay(500).then().setVisible(pbox, true);
    }

    protected int[][] PACKED = {
        {202,204},
        { 41, 50}, {320,162,117,117},
        { 42, 50}, {438,162,117,117},
        { 43, 50}, {320,280,117,117},
        { 42, 50}, {438,280,117,117},
        { 28, 31}, {176,162,143,161},
        { 41, 28}, {176,324,119,147},
        { 32,  0}, {  0,346,134,174},
        { 16, 18}, {402,  0,166,143},
        {  0, 45}, {201,  0,200,130},
        {  0, 30}, {  0,  0,200,161},
        { 10, 21}, {  0,162,175,183}};
}
