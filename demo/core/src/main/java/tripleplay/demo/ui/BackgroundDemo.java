//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import react.RFuture;
import react.Slot;

import playn.core.Image;
import playn.core.Texture;

import tripleplay.ui.Background;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Style;
import tripleplay.ui.layout.TableLayout;
import tripleplay.util.Colors;
import tripleplay.demo.DemoScreen;

public class BackgroundDemo extends DemoScreen
{
    @Override protected String name () {
        return "Backgrounds";
    }
    @Override protected String title () {
        return "UI: Backgrounds";
    }

    @Override protected Group createIface (Root root) {
        final Label s91, s92;
        Image testBg = assets().getImage("images/background.png");
        Group group = new Group(new TableLayout(3).gaps(5, 5)).add(
            label("Beveled", Background.beveled(0xFFCCFF99, 0xFFEEFFBB, 0xFFAADD77).inset(10)),
            label("Beveled (no inset)", Background.beveled(0xFFCCFF99, 0xFFEEFFBB, 0xFFAADD77)),
            label("Composite", Background.composite(
                Background.solid(Colors.BLUE).inset(5),
                Background.bordered(Colors.WHITE, Colors.BLACK, 2).inset(12),
                Background.image(testBg).inset(5))),
            label("Solid", Background.solid(0xFFCCFF99).inset(10)),
            label("Solid (no inset)", Background.solid(0xFFCCFF99)),
            new Label(),
            label("Null", Background.blank().inset(10)),
            label("Null (no inset)", Background.blank()),
            new Label(),
            label("Image", Background.image(testBg).inset(10)),
            label("Image (no inset)", Background.image(testBg)),
            new Label(),
            s91 = label("Scale 9"),
            s92 = label("Scale 9\nSomewhat\nTaller\nAnd\nWider"),
            new Label(),
            label("Bordered (inset 10)", Background.bordered(0xFFEEEEEE, 0xFFFFFF00, 2).inset(10)),
            label("Bordered (inset 2)", Background.bordered(0xFFEEEEEE, 0xFFFFFF00, 2).inset(2)),
            label("Bordered (no inset)", Background.bordered(0xFFEEEEEE, 0xFFFFFF00, 2)),
            label("Round rect", Background.roundRect(
                graphics(), 0xFFEEEEEE, 10, 0xFFFFFF00, 5).inset(10)),
            label("Round rect (no inset)", Background.roundRect(
                graphics(), 0xFFEEEEEE, 10, 0xFFFFFF00, 5)),
            label("Round rect (no border)", Background.roundRect(
                graphics(), 0xFFEEEEEE, 10).inset(10)));

        // we have to wait for our scale9 image to load before we can create its bg
        assets().getImage("images/scale9.png").textureAsync().onSuccess(new Slot<Texture>() {
            public void onEmit (Texture tex) {
                Background bg = Background.scale9(tex).inset(5);
                s91.addStyles(Style.BACKGROUND.is(bg));
                s92.addStyles(Style.BACKGROUND.is(bg));
            }
        });

        return group;
    }

    protected Label label (String text) {
        return new Label(text).addStyles(Style.HALIGN.center, Style.TEXT_WRAP.on);
    }
    protected Label label (String text, Background bg) {
        return label(text).addStyles(Style.BACKGROUND.is(bg));
    }
}
