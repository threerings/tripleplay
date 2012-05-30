//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.Map;

import com.google.common.collect.Maps;

import react.UnitSlot;

import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.BorderLayout;

/**
 * Displays BorderLayout stuff.
 */
public class BorderLayoutPage implements WidgetDemo.Page
{
    public String name () {
        return "BorderLayout";
    }

    public Group createInterface () {
        Group buttons = new Group(
            AxisLayout.horizontal(),
            Styles.make(Style.BACKGROUND.is(Background.solid(0xFFFFFFFF).inset(5))));

        for (String edge : Panel.EDGES) {
            Button butt = new Button(edge);
            buttons.add(butt);
            final String fedge = edge;
            butt.clicked().connect(new UnitSlot() {
                @Override public void onEmit () {
                    _panel.toggleEdge(fedge);
                }
            });
        }

        Button gaps = new Button("Toggle Gaps");
        buttons.add(new Shim(10, 1)).add(gaps);
        gaps.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                setPanel(_panel.useGroups, _panel.gaps == 0 ? 5 : 0);
            }
        });

        Button useGroups = new Button("Toggle Groups");
        buttons.add(new Shim(10, 1)).add(useGroups);
        useGroups.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                setPanel(!_panel.useGroups, _panel.gaps);
            }
        });

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

            add(newSection(NORTH, BorderLayout.NORTH, 0xFFFFFF00));
            add(newSection(SOUTH, BorderLayout.SOUTH, 0xFFFFCC33));
            add(newSection(WEST, BorderLayout.WEST, 0xFF666666));
            add(newSection(EAST, BorderLayout.EAST, 0xFF6699CC));
            add(newSection(CENTER, BorderLayout.CENTER, 0xFFFFCCCC));
        }

        public void toggleEdge (String name) {
            edges.get(name).setVisible(!edges.get(name).isVisible());
        }

        protected Element<?> newSection (String text, Layout.Constraint constraint, int bgColor) {
            Element<?> e;
            if (useGroups) {
                Background whiteBg = Background.solid(0xFFFFFFFF).inset(5);
                Label l = new Label(text).addStyles(Style.BACKGROUND.is(whiteBg));
                Background colorBg = Background.solid(bgColor);
                e = new Group(AxisLayout.vertical().offStretch(), Style.BACKGROUND.is(colorBg)).
                    add(l).setConstraint(constraint);
            } else {
                Background colorBg = Background.solid(bgColor).inset(5);
                e = new Label(text).addStyles(Style.BACKGROUND.is(colorBg)).setConstraint(constraint);
            }
            edges.put(text, e);
            return e;
        }
    }

    protected Group _root;
    protected Panel _panel;
}
