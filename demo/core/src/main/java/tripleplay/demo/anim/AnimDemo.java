//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.anim;

import pythagoras.f.FloatMath;

import playn.core.Canvas;
import playn.core.Font;
import playn.core.Texture;
import playn.scene.ImageLayer;
import playn.scene.Pointer;

import tripleplay.anim.AnimGroup;
import tripleplay.anim.Animation;
import tripleplay.anim.Animator;
import tripleplay.demo.DemoScreen;
import tripleplay.demo.TripleDemo;
import tripleplay.ui.Group;
import tripleplay.ui.Root;
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

    @Override protected Group createIface (Root root) {
        // demo a repeating animation
        Canvas canvas = graphics().createCanvas(100, 100);
        canvas.setFillColor(0xFFFFCC99).fillCircle(50, 50, 50);
        ImageLayer circle = new ImageLayer(canvas.toTexture());

        float width = size().width();
        iface.anim.addAt(layer, circle, 50, 100).then().
            repeat(circle).tweenX(circle).to(width-150).in(1000).easeInOut().then().
            tweenX(circle).to(50).in(1000).easeInOut();

        // demo the shake animation
        final ImageLayer click = StyledText.span(graphics(), "Click to Shake", STYLE).toLayer();
        click.events().connect(new Pointer.Listener() {
            @Override public void onStart (Pointer.Interaction iact) {
                if (_shaker != null) _shaker.complete();
                else _shaker = iface.anim.
                    shake(click).bounds(-3, 3, -3, 0).cycleTime(25, 25).in(1000).then().
                    action(_clear).handle();
            }
            protected final Runnable _clear = new Runnable() {
                public void run () { _shaker = null; }};
            protected Animation.Handle _shaker;
        });
        layer.addAt(click, (width-click.width())/2, 275);

        // demo animation groups
        Canvas ball = graphics().createCanvas(40, 40);
        ball.setFillColor(0xFF99CCFF).fillCircle(20, 20, 20);
        Texture balltex = ball.toTexture();
        ImageLayer[] balls = new ImageLayer[6];
        for (int ii = 0; ii < balls.length; ii++) {
            layer.addAt(balls[ii] = new ImageLayer(balltex), 170+ii*50, 350);
        }
        iface.anim.repeat(layer).add(dropBalls(balls, 0, 1)).then().
            add(dropBalls(balls, 1, 2)).then().
            add(dropBalls(balls, 3, 3));

        // test barrier delay
        Canvas sqimg = graphics().createCanvas(50, 50);
        sqimg.setFillColor(0xFF99CCFF).fillRect(0, 0, 50, 50);
        final ImageLayer square = new ImageLayer(sqimg.toTexture());
        square.setOrigin(25, 25);
        layer.addAt(square, 50, 300);
        square.events().connect(new Pointer.Listener() {
            @Override public void onStart (Pointer.Interaction iact) {
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
    protected Animator _banim = new Animator(paint);

    protected static final TextStyle STYLE = TextStyle.DEFAULT.withFont(new Font("Helvetica", 48));
}
