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

import tripleplay.anim.AnimGroup;
import tripleplay.anim.Animation;
import tripleplay.demo.DemoScreen;
import tripleplay.ui.Group;
import tripleplay.util.StyledText;
import tripleplay.util.TextStyle;

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
        image.canvas().setFillColor(0xFFFFCC99).fillCircle(50, 50, 50);
        ImageLayer circle = graphics().createImageLayer(image);

        float width = graphics().width();
        anim.addAt(layer, circle, 50, 100).then().
            repeat(circle).tweenX(circle).to(width-150).in(1000).easeInOut().then().
            tweenX(circle).to(50).in(1000).easeInOut();

        // demo the shake animation
        final ImageLayer click = StyledText.span("Click to Shake", STYLE).toLayer();
        click.addListener(new Pointer.Adapter() {
            @Override public void onPointerStart (Pointer.Event event) {
                anim.shake(click).bounds(-3, 3, -3, 0).cycleTime(25, 25).in(1000);
            }
        });
        layer.addAt(click, (width-click.width())/2, 275);

        // demo animation groups
        CanvasImage ball = graphics().createImage(40, 40);
        ball.canvas().setFillColor(0xFF99CCFF).fillCircle(20, 20, 20);
        ImageLayer[] balls = new ImageLayer[6];
        for (int ii = 0; ii < balls.length; ii++) {
            layer.addAt(balls[ii] = graphics().createImageLayer(ball), 170+ii*50, 350);
        }
        anim.repeat(layer).add(dropBalls(balls, 0, 1)).then().
            add(dropBalls(balls, 1, 2)).then().
            add(dropBalls(balls, 3, 3));

        return null;
    }

    protected Animation dropBalls (ImageLayer[] balls, int offset, int count) {
        float startY = 350;
        AnimGroup group = new AnimGroup();
        for (int ii = 0; ii < count; ii++) {
            ImageLayer ball = balls[ii+offset];
            group.tweenY(ball).to(startY+100).in(1000).easeIn().then().
                tweenY(ball).to(startY).in(1000).easeOut();
        }
        return group.toAnim();
    }

    protected static final TextStyle STYLE = new TextStyle().
        withFont(graphics().createFont("Helvetica", Font.Style.PLAIN, 48));
}
