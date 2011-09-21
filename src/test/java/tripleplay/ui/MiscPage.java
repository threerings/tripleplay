//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.PlayN;

import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

import react.Functions;
import react.Signals;

/**
 * Displays various UI stuff.
 */
public class MiscPage implements WidgetDemo.Page
{
    public String name () {
        return "Misc";
    }

    public Group createInterface () {
        Image smiley = PlayN.assetManager().getImage("images/smiley.png");
        Image squares = PlayN.assetManager().getImage("images/squares.png");

        Styles wrapped = Styles.make(Style.TEXT_WRAP.is(true));
        Styles alignTop = Styles.make(Style.VALIGN.is(Style.VAlign.TOP));
        Styles greenBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFCCFF99, 5)));

        Button toggle;
        Slider slider;
        Label label2, sliderValue;
        Group iface = new Group(AxisLayout.vertical().gap(15)).add(
            // display some wrapped text
            new Group(AxisLayout.horizontal(), alignTop).add(
                new Label(wrapped).setConstraint(AxisLayout.stretched()).setText(TEXT1),
                new Label(wrapped).setConstraint(AxisLayout.stretched()).setText(TEXT2),
                new Label(wrapped).setConstraint(AxisLayout.stretched()).setText(TEXT3)),
            // display some buttons labels and allow visibility toggling
            new Group(AxisLayout.horizontal().gap(15), greenBg).add(
                new Group(AxisLayout.vertical()).add(
                    new Label().setText("Toggle viz:"),
                    toggle = new Button().setText("Toggle"),
                    new Button().setText("Disabled").setEnabled(false)),
                new Group(AxisLayout.vertical()).add(
                    new Label().setText("Label 1"),
                    label2 = new Label().setText("Label 2"),
                    new Label().setIcon(smiley).setText("Label 3"))),
            // display some labels with varying icon alignment
            new Group(AxisLayout.horizontal().gap(10), greenBg).add(
                new Label(Styles.make(Style.ICON_POS.is(Style.Pos.LEFT))).
                    setText("Left").setIcon(squares, getIBounds(0)),
                new Label(Styles.make(Style.ICON_POS.is(Style.Pos.RIGHT))).
                    setText("Right").setIcon(squares, getIBounds(1)),
                new Label(Styles.make(Style.ICON_POS.is(Style.Pos.ABOVE),
                                      Style.HALIGN.is(Style.HAlign.CENTER))).
                    setText("Above").setIcon(squares, getIBounds(2)),
                new Label(Styles.make(Style.ICON_POS.is(Style.Pos.BELOW),
                                      Style.HALIGN.is(Style.HAlign.CENTER))).
                    setText("Below").setIcon(squares, getIBounds(3))),
            // TODO: move this to a separate slider page
            new Group(AxisLayout.vertical()).add(
                slider = new Slider(0, -1, 1),
                sliderValue = new Label("0")));

        Signals.toggler(toggle.clicked(), true).connect(label2.visibleSlot());

        slider.value.map(Functions.TO_STRING).connect(sliderValue.textSlot());

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
