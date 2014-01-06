//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.anim;

import pythagoras.f.FloatMath;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.ImageLayer;
import playn.core.Pointer;
import playn.core.util.Clock;
import static playn.core.PlayN.*;

import tripleplay.anim.AnimGroup;
import tripleplay.anim.Animation;
import tripleplay.anim.Animator;
import tripleplay.demo.DemoScreen;
import tripleplay.ui.Group;
import tripleplay.util.StyledText;
import tripleplay.util.TextStyle;

public class AnimDemo extends DemoScreen
{
    @Override public void paint (Clock clock) {
        super.paint(clock);
        _banim.paint(clock);
    }

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

        // test barrier delay
        CanvasImage sqimg = graphics().createImage(50, 50);
        sqimg.canvas().setFillColor(0xFF99CCFF).fillRect(0, 0, 50, 50);
        final ImageLayer square = graphics().createImageLayer(sqimg);
        square.setOrigin(25, 25);
        layer.addAt(square, 50, 300);
        square.addListener(new Pointer.Adapter() {
            @Override public void onPointerStart (Pointer.Event event) {
                square.setInteractive(false);
                _banim.tweenXY(square).to(50, 350);
                _banim.delay(250).then().tweenRotation(square).to(FloatMath.PI).in(500);
                _banim.addBarrier(1000);
                _banim.tweenXY(square).to(50, 300);
                _banim.delay(250).then().tweenRotation(square).to(0).in(500);
                _banim.addBarrier();
                _banim.action(new Runnable() {
                    public void run () { square.setInteractive(true); }
                });
            }
        });

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

    // a separate animator used for testing barriers
    protected Animator _banim = new Animator();

    protected static final TextStyle STYLE = new TextStyle().
        withFont(graphics().createFont("Helvetica", Font.Style.PLAIN, 48));
}
