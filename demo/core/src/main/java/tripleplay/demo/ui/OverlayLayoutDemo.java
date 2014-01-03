//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import tripleplay.demo.DemoScreen;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.OverlayLayout;

public class OverlayLayoutDemo extends DemoScreen
{
    @Override protected String name () {
        return "OverlayLayout";
    }

    @Override protected String title () {
        return "UI: OverlayLayout";
    }

    @Override protected Group createIface () {
        Group root = new Group(AxisLayout.vertical().offStretch()).setConstraint(AxisLayout.stretched());

        Group panel = new Group(new OverlayLayout().padding(10.0f, 15.0f, 20.0f, 25.0f),
            Styles.make(Style.BACKGROUND.is(Background.bordered(0xFFFFFFFF, 0xFF000000, 2))));
        panel.add(OverlayLayout.stretch(newSection("Stretched", 0xFFFF0000)
            .addStyles(Style.VALIGN.bottom)));
        panel.add(OverlayLayout.center(newSection("Centered", 0xFFFFFF00)));
        panel.add(OverlayLayout.stretchTop(newSection("Stretched/Top", 0xFFFF00FF)));
        panel.add(OverlayLayout.stretchRight(newSection("Stretched/Right", 0xFF0000FF))
            .addStyles(Style.VALIGN.bottom));
        panel.add(OverlayLayout.centerLeft(newSection("Center/Left", 0xFF00FF00)));
        panel.add(OverlayLayout.centerRight(newSection("Center/Right", 0xFF00FF00), 120.0f, 75.0f));
        panel.add(OverlayLayout.topLeft(newSection("Top/Left", 0xFF00FFFF)));
        panel.add(OverlayLayout.topRight(newSection("Top/Right", 0xFF00FFFF)));
        panel.add(OverlayLayout.bottomLeft(newSection("Bottom/Left", 0xFF00FFFF), 150.0f, 100.0f));

        root.add(panel.setConstraint(AxisLayout.stretched()));
        return root;
    }

    protected Element<?> newSection (String text, int bgColor) {
        Background colorBg = Background.solid(bgColor).inset(5);
        return new Label(text).addStyles(Style.BACKGROUND.is(colorBg));
    }
}
