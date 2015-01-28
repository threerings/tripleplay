//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.anim;

import playn.core.Image;
import playn.core.Surface;
import playn.core.Texture;
import playn.scene.GroupLayer;
import playn.scene.Layer;

import react.RFuture;
import react.Slot;

import tripleplay.anim.Flipbook;
import tripleplay.demo.DemoScreen;
import tripleplay.ui.Group;
import tripleplay.ui.Root;
import tripleplay.util.PackedFrames;
import tripleplay.util.SimpleFrames;

public class FramesDemo extends DemoScreen
{
    @Override protected String name () {
        return "Flipbook";
    }
    @Override protected String title () {
        return "Flipbook Demo";
    }

    @Override protected Group createIface (Root root) {
        final float width = size().width(), height = size().height();
        Layer bg = new Layer() {
            @Override protected void paintImpl (Surface surf) {
                surf.setFillColor(0xFFCCCCCC);
                surf.fillRect(0, 0, width, height);
            }
        };
        bg.setDepth(-1);
        layer.add(bg);

        // test our simple frames
        final GroupLayer box = new GroupLayer();
        layer.addAt(box, 0, 100);
        Image sheet = assets().getImage("images/spritesheet.png");
        SimpleFrames frames = new SimpleFrames(sheet, 60, 60, 60);
        iface.anim.repeat(box).flipbook(box, new Flipbook(frames, 66));
        iface.anim.repeat(box).
            tweenX(box).to(width-frames.width()).in(2000).easeInOut().then().
            tweenX(box).to(0).in(2000).easeInOut();

        // test our packed frames
        final Image packed = assets().getImage("images/packed.png");
        assets().getText("images/packed.json").onSuccess(new Slot<String>() {
            public void onEmit (String json) {
                GroupLayer box = new GroupLayer();
                layer.addAt(box, 100, 200);
                iface.anim.repeat(box).flipbook(
                    box, new Flipbook(new PackedFrames(packed, json().parse(json)), 99)).then().
                    setVisible(box, false).then().delay(500).then().setVisible(box, true);

                GroupLayer pbox = new GroupLayer();
                layer.addAt(pbox, 300, 200);
                iface.anim.repeat(pbox).flipbook(
                    pbox, new Flipbook(new PackedFrames(packed, PACKED), 99)).then().
                    setVisible(pbox, false).then().delay(500).then().setVisible(pbox, true);
            }
        });

        return null;
    }

    protected static final float[][] PACKED = {
        { 202.0f, 204.0f },
        {  41.0f,  50.0f }, { 320.0f, 162.0f, 117.0f, 117.0f },
        {  42.0f,  50.0f }, { 438.0f, 162.0f, 117.0f, 117.0f },
        {  43.0f,  50.0f }, { 320.0f, 280.0f, 117.0f, 117.0f },
        {  42.0f,  50.0f }, { 438.0f, 280.0f, 117.0f, 117.0f },
        {  28.0f,  31.0f }, { 176.0f, 162.0f, 143.0f, 161.0f },
        {  41.0f,  28.0f }, { 176.0f, 324.0f, 119.0f, 147.0f },
        {  32.0f,   0.0f }, {   0.0f, 346.0f, 134.0f, 174.0f },
        {  16.0f,  18.0f }, { 402.0f,   0.0f, 166.0f, 143.0f },
        {   0.0f,  45.0f }, { 201.0f,   0.0f, 200.0f, 130.0f },
        {   0.0f,  30.0f }, {   0.0f,   0.0f, 200.0f, 161.0f },
        {  10.0f,  21.0f }, {   0.0f, 162.0f, 175.0f, 183.0f }};
}
