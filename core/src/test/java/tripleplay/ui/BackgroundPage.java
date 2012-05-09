//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.PlayN;
import tripleplay.ui.layout.TableLayout;

public class BackgroundPage implements WidgetDemo.Page
{
    public String name () {
        return "Background";
    }

    public Group createInterface () {
        Image testBg = PlayN.assets().getImage("background.png");
        Image scale9Bg = PlayN.assets().getImage("scale9.png");
        return new Group(new TableLayout(3).gaps(5, 5)).add(
            label("Beveled", Background.beveled(0xFFCCFF99, 0xFFEEFFBB, 0xFFAADD77).inset(10)),
            label("Beveled (no inset)", Background.beveled(0xFFCCFF99, 0xFFEEFFBB, 0xFFAADD77)),
            new Label(),
            label("Solid", Background.solid(0xFFCCFF99).inset(10)),
            label("Solid (no inset)", Background.solid(0xFFCCFF99)),
            new Label(),
            label("Null", Background.blank().inset(10)),
            label("Null (no inset)", Background.blank()),
            new Label(),
            label("Image", Background.image(testBg).inset(10)),
            label("Image (no inset)", Background.image(testBg)),
            new Label(),
            label("Scale 9", Background.scale9(scale9Bg).inset(5)),
            label("Scale 9\nSomewhat\nTaller\nAnd\nWider", Background.scale9(scale9Bg).inset(5)),
            new Label(),
            label("Bordered (inset 10)", Background.bordered(0xFFEEEEEE, 0xFFFFFF00, 2).inset(10)),
            label("Bordered (inset 2)", Background.bordered(0xFFEEEEEE, 0xFFFFFF00, 2).inset(2)),
            label("Bordered (no inset)", Background.bordered(0xFFEEEEEE, 0xFFFFFF, 2)));
    }

    protected Label label (String text, Background bg) {
        return new Label(text).addStyles(Style.HALIGN.center, Style.BACKGROUND.is(bg));
    }
}
