//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import playn.core.Font;
import react.Slot;
import tripleplay.demo.DemoScreen;
import tripleplay.ui.Background;
import tripleplay.ui.Element;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Selector;
import tripleplay.ui.Shim;
import tripleplay.ui.Style;
import tripleplay.ui.Stylesheet;
import tripleplay.ui.ToggleButton;
import tripleplay.ui.layout.AxisLayout;

import static playn.core.PlayN.graphics;

public class SelectorDemo extends DemoScreen
{
    @Override protected String name () {
        return "Selector";
    }

    @Override protected String title () {
        return "UI: Selector";
    }

    @Override protected Group createIface () {
        Group main = new Group(AxisLayout.vertical()), buttons;
        Selector sel;

        String font = Style.FONT.getDefault(main).name();
        Style.Binding<Font> hdr = Style.FONT.is(graphics().createFont(font, Font.Style.BOLD, 14));
        main.setStylesheet(Stylesheet.builder().add(Label.class,
            Style.FONT.is(graphics().createFont(font, Font.Style.PLAIN, 12))).create());

        main.add(new Label("Simple").addStyles(hdr));
        main.add(new Label("A single parent with buttons - at most one is selected."));
        main.add(buttons = new Group(AxisLayout.horizontal()).add(
            mkButt("A"), mkButt("B"), mkButt("C")));
        sel = new Selector(buttons, buttons.childAt(0));
        main.add(hookup("Selection:", sel));
        main.add(new Shim(10, 10));

        main.add(new Label("Mixed").addStyles(hdr));
        main.add(new Label("A single parent with two groups - one from each may be selected."));
        main.add(buttons = new Group(AxisLayout.horizontal()).add(
            mkButt("Alvin"), mkButt("Simon"), mkButt("Theodore"),
            mkButt("Alpha"), mkButt("Sigma"), mkButt("Theta")));
        sel = new Selector().add(buttons.childAt(0), buttons.childAt(1), buttons.childAt(2));
        main.add(hookup("Chipmunk:", sel));
        sel = new Selector().add(buttons.childAt(3), buttons.childAt(4), buttons.childAt(5));
        main.add(hookup("Greek Letter:", sel));
        main.add(new Shim(10, 10));

        Style.Binding<Background> box = Style.BACKGROUND.is(
            Background.bordered(0xffffffff, 0xff000000, 1).inset(5));
        main.add(new Label("Multiple parents").addStyles(hdr));
        main.add(new Label("At most one button may be selected."));
        main.add(buttons = new Group(AxisLayout.horizontal()).add(
            new Group(AxisLayout.vertical(), box).add(mkButt("R1C1"), mkButt("R2C1")),
            new Group(AxisLayout.vertical(), box).add(mkButt("R1C2"), mkButt("R2C2"))));
        sel = new Selector().add((Group)buttons.childAt(0)).add((Group)buttons.childAt(1));
        main.add(hookup("Selection:", sel));

        return main;
    }

    protected Group hookup (String name, Selector sel) {
        final Label label = new Label();
        sel.selected.connect(new Slot<Element<?>>() {
            @Override public void onEmit (Element<?> event) {
                update(label, (ToggleButton)event);
            }
        });
        update(label, (ToggleButton)sel.selected.get());
        return new Group(AxisLayout.horizontal()).add(new Label(name), label);
    }

    protected void update (Label label, ToggleButton sel) {
        label.text.update(sel == null ? "<None>" : sel.text.get());
    }

    protected ToggleButton mkButt (String label) {
        return new ToggleButton(label);
    }
}
