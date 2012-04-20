//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.PlayN;

import pythagoras.f.Rectangle;

import react.Slot;

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
        Image smiley = PlayN.assets().getImage("smiley.png");
        final Image squares = PlayN.assets().getImage("squares.png");

        Styles wrapped = Styles.make(Style.TEXT_WRAP.is(true));
        Styles greenBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFF99CC66).inset(5)));
        Styles redBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFCC6666).inset(5)));

        ToggleButton toggle, toggle2;
        Label label2;
        Group iface = new Group(AxisLayout.vertical()).add(
            // display some buttons labels and allow visibility toggling
            new Shim(15, 15),
            new Label("Toggling visibility"),
            new Group(AxisLayout.horizontal().gap(15), greenBg).add(
                new Group(AxisLayout.vertical()).add(
                    toggle = new ToggleButton("Toggle Viz").setSelected(true),
                    toggle2 = new ToggleButton("Toggle Icon"),
                    new Button("Disabled").setEnabled(false)),
                new Group(AxisLayout.vertical()).add(
                    new Label("Label 1", redBg),
                    label2 = new Label("Label 2"),
                    new Label("Label 3").setIcon(smiley))),
            // display some labels with varying icon alignment
            new Shim(15, 15),
            new Label("Icon positioning"),
            new Group(AxisLayout.horizontal().gap(10), greenBg).add(
                new Label("Left").setStyles(Style.ICON_POS.left).setIcon(tile(squares, 0)),
                new Label("Right").setStyles(Style.ICON_POS.right).setIcon(tile(squares, 1)),
                new Label("Above").setStyles(Style.ICON_POS.above, Style.HALIGN.center).
                    setIcon(tile(squares, 2)),
                new Label("Below").setStyles(Style.ICON_POS.below, Style.HALIGN.center).
                    setIcon(tile(squares, 3))),
            // display an editable text field
            new Shim(15, 15),
            new Label("Text editing"),
            new Group(AxisLayout.horizontal().gap(10)).add(
                new Field("Editable text").setConstraint(Constraints.fixedWidth(150)),
                new Field("Disabled text").setEnabled(false)));

        final Label flabel2 = label2;
        toggle.clicked().connect(new Slot<ToggleButton>() {
            public void onEmit (ToggleButton b) {
                flabel2.setVisible(b.isSelected());
            }
        });
        toggle2.clicked().connect(new Slot<ToggleButton>() {
            public void onEmit (ToggleButton b) {
                flabel2.setIcon(b.isSelected() ? tile(squares, 0) : null);
            }
        });

        return iface;
    }

    protected Image tile (Image image, int index) {
        final float iwidth = 16, iheight = 16;
        return image.subImage(index*iwidth, 0, iwidth, iheight);
    }
}
