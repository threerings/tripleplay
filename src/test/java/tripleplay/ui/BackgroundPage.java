//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.PlayN;
import tripleplay.ui.bgs.NullBackground;
import tripleplay.ui.layout.AxisLayout;

public class BackgroundPage implements WidgetDemo.Page
{
    public String name () {
        return "Background";
    }

    public Group createInterface () {
        Image testBg = PlayN.assets().getImage("images/background.png");
        return new Group(AxisLayout.vertical()).add(
            new Group(AxisLayout.horizontal()).add(
                new Group(AxisLayout.horizontal(), Styles.make(Style.BACKGROUND.is(
                    Background.beveled(0xFFCCFF99, 0xFFEEFFBB, 0xFFAADD77, 10)))).add(
                        new Label("Beveled")),
                new Group(AxisLayout.horizontal(), Styles.make(Style.BACKGROUND.is(
                    Background.beveled(0xFFCCFF99, 0xFFEEFFBB, 0xFFAADD77, 0)))).add(
                        new Label("Beveled (no inset)"))),
            new Group(AxisLayout.horizontal()).add(
                new Group(AxisLayout.horizontal(), Styles.make(Style.BACKGROUND.is(
                    Background.solid(0xFFCCFF99, 10)))).add(new Label("Solid")),
                new Group(AxisLayout.horizontal(), Styles.make(Style.BACKGROUND.is(
                    Background.solid(0xFFCCFF99)))).add(new Label("Solid (no inset)"))),
            new Group(AxisLayout.horizontal()).add(
                new Group(AxisLayout.horizontal(), Styles.make(Style.BACKGROUND.is(
                    new NullBackground(10)))).add(new Label("Null")),
                new Group(AxisLayout.horizontal(), Styles.make(Style.BACKGROUND.is(
                    new NullBackground()))).add(new Label("Null (no inset)"))),
            // TODO: is there a convenient way to clip the group or the backgrounds? they bleed out
            new Group(AxisLayout.horizontal()).add(
                new Group(AxisLayout.horizontal(), Styles.make(Style.BACKGROUND.is(
                    Background.image(testBg, 10)))).add(new Label("Image")),
                new Group(AxisLayout.horizontal(), Styles.make(Style.BACKGROUND.is(
                    Background.image(testBg, 0)))).add(new Label("Image (no inset)"))),
            new Group(AxisLayout.horizontal()).add(
                new Group(AxisLayout.horizontal(), Styles.make(Style.BACKGROUND.is(
                    Background.bordered(0xFFEEEEEE, 0xFFFFFF00, 2, 10)))).add(
                        new Label("Bordered (inset 10)")),
                new Group(AxisLayout.horizontal(), Styles.make(Style.BACKGROUND.is(
                    Background.bordered(0xFFEEEEEE, 0xFFFFFF00, 2, 2)))).add(
                        new Label("Bordered (inset 2)")),
                new Group(AxisLayout.horizontal(), Styles.make(Style.BACKGROUND.is(
                    Background.bordered(0xFFEEEEEE, 0xFFFFFF, 2)))).add(
                        new Label("Bordered (no inset)")))
        );
    }
}
