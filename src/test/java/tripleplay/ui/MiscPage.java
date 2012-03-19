//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.PlayN;

import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

import react.Values;

import tripleplay.ui.layout.AxisLayout;

/**
 * Displays various UI stuff.
 */
public class MiscPage implements WidgetDemo.Page
{
    public String name () {
        return "Misc";
    }

    public Group createInterface () {
        Image smiley = PlayN.assets().getImage("images/smiley.png");
        Image squares = PlayN.assets().getImage("images/squares.png");

        Styles wrapped = Styles.make(Style.TEXT_WRAP.is(true));
        Styles greenBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFCCFF99, 5)));
        Styles redBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFFF0000, 5)));

        Button toggle;
        Label label2;
        Group iface = new Group(AxisLayout.vertical().gap(15)).add(
            // display some wrapped text
            new Group(AxisLayout.horizontal(), Style.VALIGN.top).add(
                new Label(TEXT1, wrapped).setConstraint(AxisLayout.stretched()),
                new Label(TEXT2, wrapped).setConstraint(AxisLayout.stretched()),
                new Label(TEXT3, wrapped).setConstraint(AxisLayout.stretched())),
            // display some buttons labels and allow visibility toggling
            new Group(AxisLayout.horizontal().gap(15), greenBg).add(
                new Group(AxisLayout.vertical()).add(
                    new Label("Toggle viz:"),
                    toggle = new Button("Toggle"),
                    new Button("Disabled").setEnabled(false)),
                new Group(AxisLayout.vertical()).add(
                    new Label("Label 1", redBg),
                    label2 = new Label("Label 2"),
                    new Label("Label 3").setIcon(smiley))),
            // display some labels with varying icon alignment
            new Group(AxisLayout.horizontal().gap(10), greenBg).add(
                new Label("Left").setStyles(Style.ICON_POS.left).setIcon(squares, getIBounds(0)),
                new Label("Right").setStyles(Style.ICON_POS.right).setIcon(squares, getIBounds(1)),
                new Label("Above").setStyles(Style.ICON_POS.above, Style.HALIGN.center).
                    setIcon(squares, getIBounds(2)),
                new Label("Below").setStyles(Style.ICON_POS.below, Style.HALIGN.center).
                    setIcon(squares, getIBounds(3))),
            new Group(AxisLayout.horizontal().gap(10)).add(
                new Field("Editable text").setConstraint(Constraints.fixedWidth(150)),
                new Field("Disabled text").setEnabled(false)));

        Values.toggler(toggle.clicked(), true).connect(label2.visibleSlot());

        return iface;
    }

    protected IRectangle getIBounds (int index) {
        final float iwidth = 16, iheight = 16;
        return new Rectangle(index*iwidth, 0, iwidth, iheight);
    }

    protected static final String TEXT1 = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";
    protected static final String TEXT2 = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.";
    protected static final String TEXT3 = "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness.";
}
