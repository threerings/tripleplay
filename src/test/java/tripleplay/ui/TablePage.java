//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import tripleplay.ui.TableLayout.Column;

/**
 * Displays tables and their configuration options.
 */
public class TablePage implements WidgetDemo.Page
{
    public String name () {
        return "Tables";
    }

    public Group createInterface () {
        TableLayout main = new TableLayout(new Column().stretch(), new Column()).gaps(15, 15);
        TableLayout aligndemo = new TableLayout(
            new Column().alignLeft(), new Column().alignRight(), new Column().stretch()).gaps(5, 5);
        TableLayout fixeddemo = new TableLayout(
            new Column().fixed(), new Column(), new Column().stretch()).gaps(5, 5);

        Styles greyBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFCCCCCC, 5)));
        Styles greenBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFCCFF99, 5)));

        Group iface = new Group(main, greyBg).add(
            new Label("This column is stretched"),
            new Label("This column is not"),

            new Group(new TableLayout(2).gaps(5, 5), greenBg).add(
                new Label("Upper left"), new Label("Upper right"),
                new Label("Lower left"), new Label("Lower right")),

            new Group(aligndemo, greenBg).add(
                new Button("Foo"),
                new Button("Bar"),
                new Button("Baz"),
                new Button("Foozle"),
                new Button("Barzle"),
                new Button("Bazzle")),

            new Group(fixeddemo, greenBg).add(
                new Button("Fixed"),
                new Button("Free"),
                new Button("Stretch+free"),
                new Button("Fixed"),
                new Button("Free"),
                new Button("Stretch+free")));

        return iface;
    }
}
