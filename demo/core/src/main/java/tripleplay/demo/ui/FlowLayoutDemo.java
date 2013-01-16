//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import playn.core.Image;
import playn.core.PlayN;

import react.UnitSlot;

import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
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

    @Override protected Group createIface () {
        Group root = new Group(
            AxisLayout.vertical().offStretch()).setConstraint(AxisLayout.stretched());

        final Group panel = new Group(new FlowLayout(), Styles.make(Style.BACKGROUND.is(
            Background.bordered(0xFFFFFFFF, 0xff000000, 2).inset(4))));

        Group buttons = new Group(AxisLayout.horizontal());
        for (ElemType type : ElemType.values()) {
            Button butt = new Button("Add " + type.toString());
            buttons.add(butt);
            final ElemType ftype = type;
            butt.clicked().connect(new UnitSlot() {
                @Override public void onEmit () {
                    panel.add(create(ftype));
                }
            });
        }
        root.add(buttons);

        buttons = new Group(AxisLayout.horizontal());
        buttons.add(new Label("HAlign:"));
        for (Style.HAlign halign : Style.HAlign.values()) {
            Button butt = new Button(halign.toString().substring(0, 1));
            buttons.add(butt);
            final Style.HAlign fhalign = halign;
            butt.clicked().connect(new UnitSlot() {
                @Override public void onEmit () {
                    panel.addStyles(Style.HALIGN.is(fhalign));
                }
            });
        }

        buttons.add(new Label("VAlign:"));
        for (Style.VAlign valign : Style.VAlign.values()) {
            Button butt = new Button(valign.toString().substring(0, 1));
            buttons.add(butt);
            final Style.VAlign fvalign = valign;
            butt.clicked().connect(new UnitSlot() {
                @Override public void onEmit () {
                    panel.addStyles(Style.VALIGN.is(fvalign));
                }
            });
        }
        root.add(buttons);

        root.add(panel.setConstraint(AxisLayout.stretched()));
        return root;
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

    protected enum ElemType { SMILE, TEXT, SMILE_TEXT, BUTTON };

    protected Image _smiley = PlayN.assets().getImage("images/smiley.png");
}
