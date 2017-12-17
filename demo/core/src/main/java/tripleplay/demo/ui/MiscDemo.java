//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import pythagoras.f.IDimension;
import pythagoras.f.MathUtil;
import pythagoras.f.Rectangle;

import react.IntValue;

import playn.core.Image;
import playn.core.Surface;
import playn.scene.Layer;
import playn.scene.Pointer;

import tripleplay.ui.*;
import tripleplay.ui.layout.*;
import tripleplay.util.Colors;

import tripleplay.demo.DemoScreen;
import tripleplay.demo.TripleDemo;

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
    @Override protected Group createIface (Root root) {
        Icon smiley = Icons.image(assets().getImage("images/smiley.png"));
        final Image squares = assets().getImage("images/squares.png");
        final CapturedRoot capRoot = iface.addRoot(new CapturedRoot(
            iface, AxisLayout.horizontal(), stylesheet(), TripleDemo.game.defaultBatch));

        CheckBox toggle, toggle2;
        Label label2;
        Field editable, disabled;
        Button setField;
        final Box box;

        Group iface = new Group(AxisLayout.horizontal().stretchByDefault()).add(
            // left column
            new Group(AxisLayout.vertical()).add(
                // labels, visibility and icon toggling
                new Label("Toggling visibility"),
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
                new Shim(5, 10),

                // labels with varying icon alignment
                new Label("Icon positioning"),
                new Group(AxisLayout.horizontal().gap(10), GREENBG).add(
                    new Label("Left", tileIcon(squares, 0)).setStyles(Style.ICON_POS.left),
                    new Label("Right", tileIcon(squares, 1)).setStyles(Style.ICON_POS.right),
                    new Label("Above", tileIcon(squares, 2)).setStyles(Style.ICON_POS.above,
                                                                       Style.HALIGN.center),
                    new Label("Below", tileIcon(squares, 3)).setStyles(Style.ICON_POS.below,
                                                                       Style.HALIGN.center)),
                new Shim(5, 10),

                // box transitions
                new Label("Box transitions"),
                box = new Box(new Label("I'm in a box").addStyles(GREENBG)).addStyles(REDBG).
                    // we fix the size of the box because it would otherwise be unconstrained in
                    // this layout; if the box is allowed to change size, the UI will be
                    // revalidated at the end of the transition and it will snap to the size of the
                    // new contents, which is jarring
                    setConstraint(Constraints.fixedSize(200, 40)),
                new Group(AxisLayout.horizontal().gap(5)).add(
                    new Button("Fade").onClick(b -> {
                        Label nlabel = new Label("I'm faded!").addStyles(GREENBG);
                        box.transition(nlabel, new Box.Fade(500));
                    }),
                    new Button("Flip").onClick(b -> {
                        Label nlabel = new Label("I'm flipped!").addStyles(GREENBG);
                        box.transition(nlabel, new Box.Flip(500));
                    })),
                new Shim(5, 10),

                // a captured root's widget
                new Label("Root capture"),
                new Group(AxisLayout.vertical()).addStyles(
                    Style.BACKGROUND.is(Background.solid(Colors.RED).inset(10))).add(
                        capRoot.createWidget())),

            // right column
            new Group(AxisLayout.vertical()).add(
                // buttons, toggle buttons, wirey uppey
                new Label("Buttons"),
                buttonsSection(squares),
                new Shim(5, 10),

                // an editable text field
                new Label("Text editing"),
                editable = new Field("Editable text").setConstraint(Constraints.fixedWidth(150)),
                setField = new Button("Set -> "),
                disabled = new Field("Disabled text").setEnabled(false),
                new Shim(5, 10),

                new Label("Dialog"),
                new Button("Show dialog").onClick(b -> showDialog())
            )
        );

        capRoot.add(new Group(AxisLayout.vertical()).
                    addStyles(Style.BACKGROUND.is(Background.blank().inset(10))).
                    add(new Label("Captured Root!"), new Button("Captured Button"))).pack();

        // add a style animation to the captured root (clicking on cap roots NYI)
        this.iface.anim.repeat(root.layer).delay(1000).then().action(new Runnable() {
            int cycle;
            @Override public void run () {
                capRoot.addStyles(Style.BACKGROUND.is(cycle++ % 2 == 0 ?
                    Background.solid(Colors.WHITE).alpha(.5f) : Background.blank()));
            }
        });

        toggle.selected().update(true);
        toggle.selected().connect(label2::setVisible);
        toggle2.selected().map(checked -> checked ? tileIcon(squares, 0) : null).
            connect(label2.icon::update);

        final Field source = editable, target = disabled;
        setField.clicked().connect(b -> {
            log().info("Setting text to " + source.text.get());
            target.text.update(source.text.get());
        });
        return iface;
    }

    protected Group buttonsSection (Image squares) {
        ToggleButton toggle3 = new ToggleButton("Toggle Enabled");
        Button disabled = new Button("Disabled");
        toggle3.selected().connectNotify(disabled::setEnabled);
        toggle3.selected().map(s -> s ? "Enabled" : "Disabled").
            connectNotify(disabled.text::update);

        class ThrobButton extends Button {
            public ThrobButton (String title) {
                super(title);
            }
            public void throb () {
                root().iface.anim.
                    tweenScale(layer).to(1.2f).in(300.0f).easeIn().then().
                    tweenScale(layer).to(1.0f).in(300.0f).easeOut();
            }
            @Override protected void layout () {
                super.layout();
                float ox = MathUtil.ifloor(_size.width/2), oy = MathUtil.ifloor(_size.height/2);
                layer.setOrigin(ox, oy);
                layer.transform().translate(ox, oy);
            }
        }
        final ThrobButton throbber = new ThrobButton("Throbber");

        final Label pressResult = new Label();
        final IntValue clickCount = new IntValue(0);
        final Box box = new Box();
        return new Group(AxisLayout.vertical().offEqualize()).add(
            new Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                toggle3, AxisLayout.stretch(disabled)),
            new Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                new LongPressButton("Long Pressable").
                    onLongPress(b -> pressResult.text.update("Long pressed")).
                    onClick(b ->pressResult.text.update("Clicked")),
                AxisLayout.stretch(pressResult)),
            new Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                new Label("Image button"),
                new ImageButton(tile(squares, 0), tile(squares, 1)).
                    onClick(b -> clickCount.increment(1)),
                new Label(clickCount)),
            new Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                new Button("Fill Box").onClick(
                    b -> box.set(new Label(box.contents() == null ? "Filled" : "Refilled"))),
                box),
            new Group(AxisLayout.horizontal().gap(15), GREENBG).add(
                throbber.onClick(b -> throbber.throb()))
            );
    }

    protected void showDialog () {
        // create a layer to absorb all clicks that don't hit our dialog
        Layer shadow = new ModalShadow(size());
        layer.add(shadow);

        // create a root to display our dialog
        Root root = iface.createRoot(AxisLayout.vertical().gap(10), stylesheet(), layer);
        root.addStyles(
            Style.BACKGROUND.is(Background.bordered(0xFFCCCCCC, 0xFFCC99FF, 5).inset(10)),
            Style.VALIGN.top);
        root.add(
            new Label("Hi! I'm a modal dialog!").addStyles(
                Style.FONT.is(DemoScreen.TITLE_FONT)),
            new Label("This here is some very exciting dialog text.\n" +
                      "Who doesn't love to read text in a modal dialog?").
                addStyles(Style.TEXT_WRAP.is(true)),
            new Button("OK!").onClick(b -> {
                shadow.close();
                iface.disposeRoot(root);
            }));
        root.packToWidth(4*size().width()/5);
        root.setLocation((size().width()-root.size().width())/2,
                         (size().height()-root.size().height())/2);
    }

    protected Image.Region tile (Image image, int index) {
        final float iwidth = 16, iheight = 16;
        return image.region(index*iwidth, 0, iwidth, iheight);
    }
    protected Icon tileIcon (Image image, int index) {
        return Icons.image(tile(image, index));
    }

    protected static final Styles GREENBG = Styles.make(
        Style.BACKGROUND.is(Background.solid(0xFF99CC66).inset(5)));
    protected static final Styles REDBG = Styles.make(
        Style.BACKGROUND.is(Background.solid(0xFFCC6666).inset(5)));
}
