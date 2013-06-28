//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import playn.core.Image;
import playn.core.Log;
import playn.core.PlayN;

import react.Function;
import react.IntValue;
import react.UnitSlot;

import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.CheckBox;
import tripleplay.ui.Constraints;
import tripleplay.ui.Field;
import tripleplay.ui.Group;
import tripleplay.ui.Icon;
import tripleplay.ui.Icons;
import tripleplay.ui.ImageButton;
import tripleplay.ui.Label;
import tripleplay.ui.LongPressButton;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.ui.ToggleButton;
import tripleplay.ui.ValueLabel;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.TableLayout;

import tripleplay.demo.DemoScreen;

/**
 * Displays various UI stuff.
 */
public class MiscDemo extends DemoScreen
{
    @Override protected String name () {
        return "General";
    }
    @Override protected String title () {
        return "UI: General";
    }

    @Override protected Group createIface () {
        Icon smiley = Icons.image(PlayN.assets().getImage("images/smiley.png"));
        final Image squares = PlayN.assets().getImage("images/squares.png");

        CheckBox toggle, toggle2;
        Label label2;
        Field editable, disabled;
        Button setField;
        Group iface = new Group(new TableLayout(2).gaps(10, 10)).add(
            new Label("Toggling visibility"),
            new Label("Buttons"),
            // labels, visibility and icon toggling
            new Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                new Group(AxisLayout.vertical()).add(
                    new Group(AxisLayout.horizontal()).add(
                        toggle = new CheckBox(),
                        new Label("Toggle Viz")),
                    new Group(AxisLayout.horizontal()).add(
                        toggle2 = new CheckBox(),
                        new Label("Toggle Icon"))),
                new Group(AxisLayout.vertical()).add(
                    new Label("Label 1").addStyles(REDBG),
                    label2 = new Label("Label 2"),
                    new Label("Label 3", smiley))),
            // buttons, toggle buttons, wirey uppey
            buttonsSection(squares),

            new Label("Icon positioning"),
            new Label("Text editing"),
            // labels with varying icon alignment
            new Group(AxisLayout.horizontal().gap(10), GREENBG).add(
                new Label("Left", tileIcon(squares, 0)).setStyles(Style.ICON_POS.left),
                new Label("Right", tileIcon(squares, 1)).setStyles(Style.ICON_POS.right),
                new Label("Above", tileIcon(squares, 2)).setStyles(Style.ICON_POS.above,
                                                                   Style.HALIGN.center),
                new Label("Below", tileIcon(squares, 3)).setStyles(Style.ICON_POS.below,
                                                                   Style.HALIGN.center)),
            // an editable text field
            new Group(AxisLayout.vertical()).add(
                new Group(AxisLayout.horizontal().gap(10)).add(
                    editable = new Field("Editable text").setConstraint(Constraints.fixedWidth(150)),
                    disabled = new Field("Disabled text").setEnabled(false)),
                setField = new Button("Set -> ")));

        toggle.checked.update(true);
        toggle.checked.connect(label2.visibleSlot());
        toggle2.checked.map(new Function<Boolean,Icon>() {
            public Icon apply (Boolean checked) {
                return checked ? tileIcon(squares, 0) : null;
            }
        }).connect(label2.icon.slot());

        final Field source = editable, target = disabled;
        setField.clicked().connect(new UnitSlot() {
            public void onEmit () {
                PlayN.log().info("Setting text to " + source.text.get());
                target.text.update(source.text.get());
            }
        });
        return iface;
    }

    protected Group buttonsSection (Image squares) {
        ToggleButton toggle3 = new ToggleButton("Toggle Enabled");
        Button disabled = new Button("Disabled");
        toggle3.selected.connectNotify(disabled.enabledSlot());
        toggle3.selected.map(new Function<Boolean,String>() {
            public String apply (Boolean selected) { return selected ? "Enabled" : "Disabled"; }
        }).connectNotify(disabled.text.slot());

        final Label pressResult = new Label();
        final IntValue clickCount = new IntValue(0);
        return new Group(AxisLayout.vertical().offEqualize()).add(
            new Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                toggle3, AxisLayout.stretch(disabled)),
            new Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                new LongPressButton("Long Pressable").onLongPress(new UnitSlot() {
                    public void onEmit () { pressResult.text.update("Long pressed"); }
                }).onClick(new UnitSlot() {
                    public void onEmit () { pressResult.text.update("Clicked"); }
                }), AxisLayout.stretch(pressResult)),
            new Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                new Label("Image button"),
                new ImageButton(tile(squares, 0), tile(squares, 1)).onClick(new UnitSlot() {
                    public void onEmit () { clickCount.increment(1); }
                }),
                new ValueLabel(clickCount)));
    }

    protected Image tile (Image image, int index) {
        final float iwidth = 16, iheight = 16;
        return image.subImage(index*iwidth, 0, iwidth, iheight);
    }
    protected Icon tileIcon (Image image, int index) {
        return Icons.image(tile(image, index));
    }

    protected static final Styles GREENBG = Styles.make(
        Style.BACKGROUND.is(Background.solid(0xFF99CC66).inset(5)));
    protected static final Styles REDBG = Styles.make(
        Style.BACKGROUND.is(Background.solid(0xFFCC6666).inset(5)));
}
