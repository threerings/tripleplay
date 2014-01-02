//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import pythagoras.f.Dimension;
import pythagoras.f.Point;

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

        Group panel = new Group(new OverlayLayout(), Styles.make(Style.BACKGROUND.is(
            Background.bordered(0xFFFFFFFF, 0xff000000, 2).inset(4))));
        panel.add(newSection("first", new OverlayLayout.Constraint(new Point(100.0f, 50.0f),
            new Dimension(200.0f, 100.0f), Style.HAlign.CENTER, Style.VAlign.CENTER), 0xFFFFFF00));

        root.add(panel.setConstraint(AxisLayout.stretched()));
        return root;
    }

    protected Element<?> newSection (String text, OverlayLayout.Constraint constraint, int bgColor) {
        Background colorBg = Background.solid(bgColor).inset(5);
        Element<?> e = new Label(text).addStyles(Style.BACKGROUND.is(colorBg)).
            setConstraint(constraint);
        return e;
    }
}
