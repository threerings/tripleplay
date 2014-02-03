//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import java.util.Map;

import com.google.common.collect.Maps;

import react.UnitSlot;

import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Layout;
import tripleplay.ui.Shim;
import tripleplay.ui.SizableGroup;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.BorderLayout;
import tripleplay.ui.layout.FlowLayout;
import tripleplay.util.DimensionValue;

import tripleplay.demo.DemoScreen;

/**
 * Displays BorderLayout stuff.
 */
public class BorderLayoutDemo extends DemoScreen
{
    @Override public String name () {
        return "BorderLayout";
    }
    @Override public String title () {
        return "UI: BorderLayout";
    }

    @Override protected Group createIface () {
        Group buttons = new Group(
            AxisLayout.horizontal(),
            Styles.make(Style.BACKGROUND.is(Background.solid(0xFFFFFFFF).inset(5))));

        for (final String edge : Panel.EDGES) {
            buttons.add(new Button(edge).onClick(new UnitSlot() {
                @Override public void onEmit () {
                    _panel.toggleEdge(edge);
                }
            }));
        }

        buttons.add(new Shim(10, 1)).add(new Button("Toggle Gaps").onClick(new UnitSlot() {
            @Override public void onEmit () {
                setPanel(_panel.useGroups, _panel.gaps == 0 ? 5 : 0);
            }
        }));

        buttons.add(new Shim(10, 1)).add(new Button("Toggle Sizing").onClick(new UnitSlot() {
            @Override public void onEmit () {
                setPanel(!_panel.useGroups, _panel.gaps);
            }
        }));

        _root = new Group(AxisLayout.vertical().offStretch()).setConstraint(AxisLayout.stretched());
        _root.add(buttons);
        setPanel(false, 0);
        return _root;
    }

    protected void setPanel (boolean useGroups, float gaps) {
        if (_panel != null) _root.remove(_panel);
        _panel = new Panel(useGroups, gaps);
        _panel.setConstraint(AxisLayout.stretched());
        _root.add(0, _panel);
    }

    public static class Panel extends Group {
        public static final String NORTH = "North";
        public static final String SOUTH = "South";
        public static final String WEST = "West";
        public static final String EAST = "East";
        public static final String CENTER = "Center";
        public static final String[] EDGES = {NORTH, SOUTH, WEST, EAST, CENTER};

        public final boolean useGroups;
        public final float gaps;
        public final Map<String, Element<?>> edges = Maps.newHashMap();

        public Panel (boolean useGroups, float gaps) {
            super(new BorderLayout(gaps));
            this.useGroups = useGroups;
            this.gaps = gaps;

            add(newSection(NORTH, BorderLayout.NORTH, 0xFFFFFF00, 2).addStyles(Style.VALIGN.top));
            add(newSection(SOUTH, BorderLayout.SOUTH, 0xFFFFCC33, 2).addStyles(Style.VALIGN.bottom));
            add(newSection(WEST, BorderLayout.WEST, 0xFF666666, 1).addStyles(Style.HALIGN.left));
            add(newSection(EAST, BorderLayout.EAST, 0xFF6699CC, 1).addStyles(Style.HALIGN.right));
            add(newSection(CENTER, BorderLayout.CENTER, 0xFFFFCCCC, 0));
        }

        public void toggleEdge (String name) {
            edges.get(name).setVisible(!edges.get(name).isVisible());
        }

        protected Element<?> newSection (String text, Layout.Constraint constraint, int bgColor,
            int flags) {
            Element<?> e;
            if (useGroups) {
                Background colorBg = Background.solid(bgColor);
                SizableGroup g = new SizableGroup(new FlowLayout());
                g.addStyles(Style.BACKGROUND.is(colorBg));

                if ((flags & 1) != 0) g.add(getSizer(g, "W+", 10, 0), getSizer(g, "W-", -10, 0));
                if ((flags & 2) != 0) g.add(getSizer(g, "H+", 0, 10), getSizer(g, "H-", 0, -10));
                e = g.setConstraint(constraint);

            } else {
                Background colorBg = Background.solid(bgColor).inset(5);
                e = new Label(text).addStyles(Style.BACKGROUND.is(colorBg)).
                    setConstraint(constraint);
            }
            edges.put(text, e);
            return e;
        }
    }

    protected static Button getSizer (SizableGroup g, String text, float dw, float dh) {
        return new Button(text).onClick(getSizer(g.preferredSize, dw, dh));
    }

    protected static UnitSlot getSizer (final DimensionValue base, final float dw, final float dh) {
        return new UnitSlot() {
            @Override public void onEmit () {
                base.update(Math.max(0, base.get().width() + dw),
                            Math.max(0, base.get().height() + dh));
            }
        };
    }

    protected Group _root;
    protected Panel _panel;
}
