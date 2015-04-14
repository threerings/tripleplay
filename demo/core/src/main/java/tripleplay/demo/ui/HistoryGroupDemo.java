//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import react.Slot;
import react.UnitSlot;
import tripleplay.demo.DemoScreen;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Field;
import tripleplay.ui.Group;
import tripleplay.ui.HistoryGroup;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.SizableGroup;
import tripleplay.ui.Slider;
import tripleplay.ui.Style;
import tripleplay.ui.Stylesheet;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.BorderLayout;
import tripleplay.util.Colors;

public class HistoryGroupDemo extends DemoScreen
{
    @Override
    protected String name () {
        return "History Group";
    }

    @Override
    protected String title () {
        return "UI: History Group";
    }

    @Override protected Group createIface (Root root) {
        final Field prefix = new Field("Love Potion Number ");
        Button add10 = new Button("+10");
        Button add100 = new Button("+100");
        HistoryGroup.Labels history = new HistoryGroup.Labels();
        final SizableGroup historyBox = new SizableGroup(new BorderLayout());
        historyBox.add(history.setConstraint(BorderLayout.CENTER));
        Slider width = new Slider(150, 25, 1024);
        Group top = new Group(AxisLayout.horizontal()).add(
            prefix.setConstraint(AxisLayout.stretched()), add10, add100, width);
        width.value.connectNotify(new Slot<Float>() {
            @Override public void onEmit (Float val) {
                historyBox.preferredSize.updateWidth(val);
            }
        });
        add10.clicked().connect(addSome(history, prefix, 10));
        add100.clicked().connect(addSome(history, prefix, 100));
        history.setStylesheet(Stylesheet.builder().add(Label.class,
            Style.BACKGROUND.is(Background.composite(
                Background.blank().inset(0, 2),
                Background.bordered(Colors.WHITE, Colors.BLACK, 1).inset(10))),
            Style.TEXT_WRAP.on, Style.HALIGN.left).create());
        history.addStyles(Style.BACKGROUND.is(Background.beveled(
            Colors.CYAN, Colors.brighter(Colors.CYAN), Colors.darker(Colors.CYAN)).inset(5)));
        _lastNum = 0;
        return new Group(AxisLayout.vertical()).add(
            top, historyBox.setConstraint(AxisLayout.stretched())).addStyles(
            Style.BACKGROUND.is(Background.blank().inset(5)));
    }

    protected UnitSlot addSome (final HistoryGroup.Labels group, final Field prefix, final int num) {
        return new UnitSlot() {
            @Override public void onEmit () {
                for (int ii = 0; ii < num; ++ii) {
                    group.addItem(prefix.text.get() + String.valueOf(++_lastNum));
                }
            }
        };
    }

    protected int _lastNum;
}
