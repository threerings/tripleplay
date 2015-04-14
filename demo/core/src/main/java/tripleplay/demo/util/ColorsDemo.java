//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.util;

import playn.core.Canvas;

import tripleplay.demo.DemoScreen;
import tripleplay.ui.Group;
import tripleplay.ui.Icon;
import tripleplay.ui.Icons;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Style;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.TableLayout;
import tripleplay.util.Colors;

public class ColorsDemo extends DemoScreen
{
    @Override protected String name () {
        return "Colors";
    }

    @Override protected String title () {
        return "Util: Colors";
    }

    @Override protected Group createIface (Root root) {
        return new Group(AxisLayout.vertical(), Style.HALIGN.center).add(
            new Group(new TableLayout(TableLayout.COL.fixed().alignRight(),
                                      TableLayout.COL.fixed().alignLeft()).gaps(1, 5)).add(
                new Label("White"), createLabel(Colors.WHITE),
                new Label("Light Gray"), createLabel(Colors.LIGHT_GRAY),
                new Label("Gray"), createLabel(Colors.GRAY),
                new Label("Dark Gray"), createLabel(Colors.DARK_GRAY),
                new Label("Black"), createLabel(Colors.BLACK),
                new Label("Red"), createLabel(Colors.RED),
                new Label("Pink"), createLabel(Colors.PINK),
                new Label("Orange"), createLabel(Colors.ORANGE),
                new Label("Yellow"), createLabel(Colors.YELLOW),
                new Label("Green"), createLabel(Colors.GREEN),
                new Label("Magenta"), createLabel(Colors.MAGENTA),
                new Label("Cyan"), createLabel(Colors.CYAN),
                new Label("Blue"), createLabel(Colors.BLUE)));
    }

    protected Label createLabel (int baseColor) {
        return new Label(createSampler(baseColor));
    }

    protected Icon createSampler (int baseColor) {
        int size = 16;
        Canvas canvas = graphics().createCanvas(size * 17, size);
        int lighter = baseColor;
        for (int ii = 0; ii <= 8; ++ii) {
            canvas.setFillColor(lighter);
            canvas.fillRect(size * (ii + 8), 0, size, size);
            lighter = Colors.brighter(lighter);
        }
        int darker = baseColor;
        for (int ii = 0; ii < 8; ++ii) {
            canvas.setFillColor(darker);
            canvas.fillRect(size * (7 - ii), 0, size, size);
            darker = Colors.darker(darker);
        }

        canvas.setStrokeColor(Colors.BLACK);
        canvas.strokeRect(size * 8, 0, size - 1, size - 1);
        return Icons.image(canvas.toTexture());
    }
}
