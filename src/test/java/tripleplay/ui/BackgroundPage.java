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
        Image testBg = PlayN.assets().getImage("images/background.png");
        Image scale9Bg = PlayN.assets().getImage("images/scale9.png");
        return new Group(new TableLayout(3).gaps(5, 5)).add(
            label("Beveled", Background.beveled(0xFFCCFF99, 0xFFEEFFBB, 0xFFAADD77, 10)),
            label("Beveled (no inset)", Background.beveled(0xFFCCFF99, 0xFFEEFFBB, 0xFFAADD77, 0)),
            new Label(),
            label("Solid", Background.solid(0xFFCCFF99, 10)),
            label("Solid (no inset)", Background.solid(0xFFCCFF99)),
            new Label(),
            label("Null", Background.blank(10)),
            label("Null (no inset)", Background.blank(0)),
            new Label(),
            label("Image", Background.image(testBg, 10)),
            label("Image (no inset)", Background.image(testBg, 0)),
            new Label(),
            label("Scale 9", Background.scale9(scale9Bg, 5)),
            label("Scale 9\nSomewhat\nTaller\nAnd\nWider", Background.scale9(scale9Bg, 5)),
            new Label(),
            label("Bordered (inset 10)", Background.bordered(0xFFEEEEEE, 0xFFFFFF00, 2, 10)),
            label("Bordered (inset 2)", Background.bordered(0xFFEEEEEE, 0xFFFFFF00, 2, 2)),
            label("Bordered (no inset)", Background.bordered(0xFFEEEEEE, 0xFFFFFF, 2)));
    }

    protected Label label (String text, Background bg) {
        return new Label(text).addStyles(Style.HALIGN.center, Style.BACKGROUND.is(bg));
    }
}
