//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.Point;

import react.Slot;
import react.Value;

import playn.core.Surface;
import playn.scene.Layer;

import tripleplay.demo.DemoScreen;
import tripleplay.ui.Background;
import tripleplay.ui.Composite;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Slider;
import tripleplay.ui.Style;
import tripleplay.ui.layout.AbsoluteLayout;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.util.BoxPoint;
import tripleplay.util.Colors;

public class AbsoluteLayoutDemo extends DemoScreen
{
    @Override protected String name () {
        return "AbsoluteLayout";
    }

    @Override protected String title () {
        return "UI: Absolute Layout";
    }

    @Override protected Group createIface (Root root) {
        final BoxPointWidget position = new BoxPointWidget("Position");
        final BoxPointWidget origin = new BoxPointWidget("Origin");
        final Slider width = new Slider(50, 10, 150);
        final Slider height = new Slider(50, 10, 150);
        Group sizeCtrl = new Group(AxisLayout.horizontal()).add(new Label("Size:"), width, height);
        final Group group = new Group(new AbsoluteLayout());
        group.layer.add(new Layer() {
            private Point pt = new Point();
            @Override protected void paintImpl (Surface surface) {
                IDimension size = group.size();
                position.point.get().resolve(size, pt);
                surface.saveTx();
                surface.setFillColor(Colors.BLACK);
                surface.fillRect(pt.x - 2, pt.y - 2, 5, 5);
                surface.restoreTx();
            }
        }.setDepth(1));
        group.addStyles(Style.BACKGROUND.is(Background.solid(Colors.WHITE)));
        final Group widget = new Group(AxisLayout.horizontal()).addStyles(
            Style.BACKGROUND.is(Background.solid(Colors.CYAN)));
        group.add(widget);
        Slot<Object> updateConstraint = v -> widget.setConstraint(new AbsoluteLayout.Constraint(
            position.point.get(), origin.point.get(),
            new Dimension(width.value.get(), height.value.get())));
        width.value.connect(updateConstraint);
        height.value.connect(updateConstraint);
        position.point.connect(updateConstraint);
        origin.point.connect(updateConstraint);
        updateConstraint.onEmit(null);
        return new Group(AxisLayout.vertical().offStretch()).add(
            new Label("Move the sliders to play with the constraint"),
            new Group(AxisLayout.horizontal()).add(position, origin),
            sizeCtrl, group.setConstraint(AxisLayout.stretched()));
    }

    protected static class BoxPointWidget extends Composite<BoxPointWidget>
    {
        public final Value<BoxPoint> point = Value.create(BoxPoint.TL);
        public final Slider
            nx = new Slider(0, 0, 1), ny = new Slider(0, 0, 1),
            ox = new Slider(0, 0, 100), oy = new Slider(0, 0, 100);

        public BoxPointWidget (String label) {
            setLayout(AxisLayout.vertical());
            initChildren(new Label(label),
                new Group(AxisLayout.horizontal()).add(new Label("N:"), nx, ny),
                new Group(AxisLayout.horizontal()).add(new Label("O:"), ox, oy));
            Slot<Object> update = v -> point.update(new BoxPoint(nx.value.get(), ny.value.get(),
                                                                 ox.value.get(), oy.value.get()));
            nx.value.connect(update);
            ny.value.connect(update);
            ox.value.connect(update);
            oy.value.connect(update);
            addStyles(Style.BACKGROUND.is(Background.solid(Colors.LIGHT_GRAY)));
        }

        @Override protected Class<?> getStyleClass () {
            return BoxPointWidget.class;
        }
    }
}
