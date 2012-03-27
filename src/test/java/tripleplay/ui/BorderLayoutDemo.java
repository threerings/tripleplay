//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.Map;

import com.google.common.collect.Maps;

import playn.core.Color;
import playn.core.Game;
import playn.core.PlayN;
import playn.java.JavaPlatform;
import react.UnitSlot;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Group;
import tripleplay.ui.Interface;
import tripleplay.ui.Label;
import tripleplay.ui.Layout;
import tripleplay.ui.Root;
import tripleplay.ui.SimpleStyles;
import tripleplay.ui.Style;
import tripleplay.ui.Styles;
import tripleplay.ui.Stylesheet;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.BorderLayout;

public class BorderLayoutDemo implements Game
{
    public static void main (String[] args) {
        JavaPlatform.register();
        PlayN.run(new BorderLayoutDemo());
    }

    public final Interface iface = new Interface();

    @Override
    public void init () {
        // define our root stylesheet
        Stylesheet rootSheet = SimpleStyles.newSheetBuilder().
            add(Label.class, Style.HALIGN.center, Style.VALIGN.center).
            create();

        // create our demo interface
        _root = iface.createRoot(AxisLayout.vertical().offStretch(), rootSheet,
                                 PlayN.graphics().rootLayer()).
            setSize(PlayN.graphics().width(), PlayN.graphics().height()).
            addStyles(Style.BACKGROUND.is(Background.solid(0xff000000)), Style.VALIGN.top);

        for (String edge : Panel.EDGES) {
            Button butt = new Button(edge);
            _butts.add(butt);
            final String fedge = edge;
            butt.clicked().connect(new UnitSlot() {
                @Override
                public void onEmit () {
                    _panel.toggleEdge(fedge);
                }
            });
        }

        Button gaps = new Button("Toggle Gaps");
        _butts.add(new Shim(10, 1)).add(gaps);
        gaps.clicked().connect(new UnitSlot() {
            @Override
            public void onEmit () {
                setPanel(_panel.useGroups, _panel.gaps == 0 ? 5 : 0);
            }
        });

        Button useGroups = new Button("Toggle Groups");
        _butts.add(new Shim(10, 1)).add(useGroups);
        useGroups.clicked().connect(new UnitSlot() {
            @Override
            public void onEmit () {
                setPanel(!_panel.useGroups, _panel.gaps);
            }
        });

        _root.add(_butts);
        setPanel(false, 0);
    }

    @Override
    public void update (float delta) {
        iface.update(delta);
    }

    @Override
    public void paint (float alpha) {
        iface.paint(alpha);
    }

    @Override
    public int updateRate () {
        return 30;
    }

    protected void setPanel (boolean useGroups, float gaps) {
        if (_panel != null) {
            _root.remove(_panel);
            _panel = null;
        }
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

            add(newSection(NORTH, BorderLayout.NORTH, Color.rgb(255, 255, 0)));
            add(newSection(SOUTH, BorderLayout.SOUTH, Color.rgb(255, 200, 0)));
            add(newSection(WEST, BorderLayout.WEST, Color.rgb(128, 128, 128)));
            add(newSection(EAST, BorderLayout.EAST, Color.rgb(0, 0, 255)));
            add(newSection(CENTER, BorderLayout.CENTER, Color.rgb(255, 175, 175)));
        }

        public void toggleEdge (String name) {
            edges.get(name).setVisible(!edges.get(name).isVisible());
        }

        protected Element<?> newSection (String text, Layout.Constraint constraint, int bgColor) {
            Element<?> e;
            if (useGroups) {
                Label l = new Label(text, Styles.make(Style.BACKGROUND.is(
                    Background.solid(Color.rgb(255, 255, 255), 5))));

                e = new Group(AxisLayout.vertical().offStretch(), Style.BACKGROUND.is(
                    Background.solid(bgColor))).add(l).setConstraint(constraint);
            } else {
                e = new Label(text, Styles.make(Style.BACKGROUND.is(
                    Background.solid(bgColor, 5)))).setConstraint(constraint);
            }
            edges.put(text, e);
            return e;
        }
    }

    protected Root _root;
    protected Panel _panel;
    protected Group _butts = new Group(AxisLayout.horizontal(),
        Styles.make(Style.BACKGROUND.is(Background.solid(Color.rgb(255, 255, 255), 5))));
}
