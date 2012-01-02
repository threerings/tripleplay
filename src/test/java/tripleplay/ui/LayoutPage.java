//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import tripleplay.ui.layout.TableLayout;
import tripleplay.ui.layout.AbsoluteLayout;
import tripleplay.ui.layout.AxisLayout;
import static tripleplay.ui.layout.TableLayout.COL;

/**
 * Displays some layouts and their configuration options.
 */
public class LayoutPage implements WidgetDemo.Page
{
    public String name () {
        return "Layouts";
    }

    public Group createInterface () {
        TableLayout main = new TableLayout(COL.stretch(), COL).gaps(15, 15);
        TableLayout alignDemo = new TableLayout(
            COL.alignLeft(), COL.alignRight(), COL.stretch()).gaps(5, 5);
        TableLayout fixedDemo = new TableLayout(COL.fixed(), COL, COL.stretch()).gaps(5, 5);
        TableLayout minWidthDemo = new TableLayout(
            COL.minWidth(100), COL.minWidth(100).stretch(), COL).gaps(5, 5);

        Styles greyBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFCCCCCC, 5)));
        Styles greenBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFCCFF99, 5)));

        Group iface = new Group(AxisLayout.vertical().offStretch()).add(
            new Group(main, greyBg).add(
                new Label("This column is stretched"),
                new Label("This column is not"),

                new Group(new TableLayout(COL, COL).gaps(5, 5), greenBg).add(
                    new Label("Upper left"), new Label("Upper right"),
                    new Label("Lower left"), new Label("Lower right")),

                new Group(alignDemo, greenBg).add(
                    new Button("Foo"),
                    new Button("Bar"),
                    new Button("Baz"),
                    new Button("Foozle"),
                    new Button("Barzle"),
                    new Button("Bazzle")),

                new Group(fixedDemo, greenBg).add(
                    new Button("Fixed"),
                    new Button("Free"),
                    new Button("Stretch+free"),
                    new Button("Fixed"),
                    new Button("Free"),
                    new Button("Stretch+free")),

                new Group(minWidthDemo, greenBg).add(
                    new Button("Min"),
                    new Button("M+stretch"),
                    new Button("Free"),
                    new Button("Min"),
                    new Button("M+stretch"),
                    new Button("Free"))),

            new Group(new AbsoluteLayout(), greyBg).add(
                AbsoluteLayout.at(new Label("Absolute Layout"), 10, 10),
                AbsoluteLayout.at(new Label("+50+50"), 50, 50),
                AbsoluteLayout.at(new Button("150x35+150+50"), 150, 50, 150, 35)));

        return iface;
    }
}
