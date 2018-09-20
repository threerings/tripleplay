//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import react.UnitSlot;

import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Group;
import tripleplay.ui.Icon;
import tripleplay.ui.Icons;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.FlowLayout;

import tripleplay.demo.DemoScreen;

public class FlowLayoutDemo extends DemoScreen
{
    @Override protected String name () {
        return "FlowLayout";
    }
    @Override protected String title () {
        return "UI: FlowLayout";
    }

    @Override protected Group createIface (Root root) {
        Group main = new Group(AxisLayout.vertical().offStretch());

        final Group panel = new Group(new FlowLayout(), Styles.make(Style.BACKGROUND.is(
            Background.bordered(0xFFFFFFFF, 0xff000000, 2).inset(4))));

        Group buttons = new Group(AxisLayout.horizontal());
        for (final ElemType type : ElemType.values()) {
            buttons.add(new Button("Add " + type.toString()).onClick(new UnitSlot() {
                @Override public void onEmit () { panel.add(create(type)); }
            }));
        }
        main.add(buttons);

        buttons = new Group(AxisLayout.horizontal());
        buttons.add(new Label("HAlign:"));
        for (final Style.HAlign halign : Style.HAlign.values()) {
            buttons.add(new Button(halign.toString().substring(0, 1)).onClick(new UnitSlot() {
                @Override public void onEmit () { panel.addStyles(Style.HALIGN.is(halign)); }
            }));
        }

        buttons.add(new Label("VAlign:"));
        for (final Style.VAlign valign : Style.VAlign.values()) {
            buttons.add(new Button(valign.toString().substring(0, 1)).onClick(new UnitSlot() {
                @Override public void onEmit () { panel.addStyles(Style.VALIGN.is(valign)); }
            }));
        }
        main.add(buttons);

        main.add(panel.setConstraint(AxisLayout.stretched()));
        return main;
    }

    public Element<?> create (ElemType type) {
        switch (type) {
        case SMILE: return new Label(_smiley);
        case SMILE_TEXT: return new Label("Some Text", _smiley);
        case TEXT: return new Label("Blah blah blah");
        case BUTTON: return new Button("Click to Foo");
        default: throw new AssertionError();
        }
    }

    protected enum ElemType { SMILE, TEXT, SMILE_TEXT, BUTTON }

    protected Icon _smiley = Icons.image(assets().getImage("images/smiley.png"));
}
