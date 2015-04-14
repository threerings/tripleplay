//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.anim;

import pythagoras.f.Point;

import react.Slot;

import playn.core.*;
import playn.scene.GroupLayer;
import playn.scene.ImageLayer;
import playn.scene.Layer;

import tripleplay.anim.Flicker;
import tripleplay.demo.DemoScreen;
import tripleplay.ui.Group;
import tripleplay.ui.Root;
import tripleplay.util.StyledText;
import tripleplay.util.TextStyle;

/**
 * Demonstrates the flicker.
 */
public class FlickerDemo extends DemoScreen
{
    @Override protected String name () {
        return "Flicker";
    }
    @Override protected String title () {
        return "Flicker Demo";
    }

    @Override protected Group createIface (Root root) {
        final float width = 410, height = 400;
        GroupLayer clip = new GroupLayer(410, 400);
        layer.addAt(clip, (size().width()-width)/2, (size().height()-height)/2);

        final GroupLayer scroll = new GroupLayer();
        clip.add(scroll);
        // add a bunch of image layers to our root layer
        float y = 0;
        for (int ii = 0; ii < IMG_COUNT; ii++) {
            Canvas image = graphics().createCanvas(width, IMG_HEIGHT);
            StringBuffer text = new StringBuffer();
            if (ii == 0) text.append("Tap & fling");
            else if (ii == IMG_COUNT-1) text.append("Good job!");
            else for (int tt = 0; tt < 25; tt++) text.append(ii);
            StyledText.span(graphics(), text.toString(), TEXT).render(image, 0, 0);
            ImageLayer layer = new ImageLayer(image.toTexture());
            scroll.addAt(layer, 0, y);
            y += layer.scaledHeight();
        }

        Flicker flicker = new Flicker(0, height-IMG_HEIGHT*IMG_COUNT, 0) {
            @Override protected float friction () { return 0.001f; }
        };
        clip.events().connect(flicker);
        flicker.changed.connect(new Slot<Flicker>() {
            public void onEmit (Flicker flicker) { scroll.setTy(flicker.position); }
        });
        closeOnHide(paint.connect(flicker.onPaint));

        return null;
    }

    protected static final float IMG_HEIGHT = 100;
    protected static final int IMG_COUNT = 20;
    protected static final TextStyle TEXT = TextStyle.DEFAULT.withFont(new Font("Helvetiva", 72));
}
