//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.anim;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.ImageLayer;
import playn.core.Pointer;
import static playn.core.PlayN.*;

import tripleplay.demo.DemoScreen;
import tripleplay.ui.Group;
import tripleplay.util.TextConfig;

public class AnimDemo extends DemoScreen
{
    @Override protected String name () {
        return "Anims";
    }
    @Override protected String title () {
        return "Various Animations";
    }

    @Override protected Group createIface () {
        // demo a repeating animation
        CanvasImage image = graphics().createImage(100, 100);
        image.canvas().setFillColor(0xFFFFCC99);
        image.canvas().fillCircle(50, 50, 50);
        ImageLayer circle = graphics().createImageLayer(image);

        float width = graphics().width();
        anim.addAt(layer, circle, 50, 100).then().
            repeat(circle).tweenX(circle).to(width-150).in(1000).easeInOut().then().
            tweenX(circle).to(50).in(1000).easeInOut();

        // demo the shake animation
        final ImageLayer click = CFG.toLayer("Click to Shake");
        click.addListener(new Pointer.Adapter() {
            public void onPointerStart (Pointer.Event event) {
                anim.shake(click).bounds(-3, 3, -3, 0).cycleTime(25, 25).in(1000);
            }
        });
        layer.addAt(click, (width-click.width())/2, 275);

        return null;
    }

    protected static final TextConfig CFG = new TextConfig(0xFF000000).
        withFont(graphics().createFont("Helvetica", Font.Style.PLAIN, 48));
}
