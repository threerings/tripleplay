//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import pythagoras.f.Dimension;

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
            Styles.make(Style.BACKGROUND.is(Background.bordered(0xFFFFFFFF, 0xff000000, 2).inset(4))));
        panel.add(OverlayLayout.stretched(newSection("Background", 0xFFFF0000)));
        panel.add(OverlayLayout.center(newSection("Centered", 0xFFFFFF00)));
        panel.add(OverlayLayout.at(newSection("Center/Right", 0xFF00FF00), 100.0f, 75.0f,
            Style.HAlign.RIGHT, Style.VAlign.CENTER));
        panel.add(OverlayLayout.at(newSection("Stretched/Right", 0xFF0000FF), false, true,
            Style.HAlign.RIGHT, Style.VAlign.CENTER));
        panel.add(OverlayLayout.at(newSection("Bottom/Left", 0xFF00FFFF), Style.HAlign.LEFT,
            Style.VAlign.BOTTOM));

        root.add(panel.setConstraint(AxisLayout.stretched()));
        return root;
    }

    protected Element<?> newSection (String text, int bgColor) {
        Background colorBg = Background.solid(bgColor).inset(5);
        return new Label(text).addStyles(Style.BACKGROUND.is(colorBg));
    }
}
