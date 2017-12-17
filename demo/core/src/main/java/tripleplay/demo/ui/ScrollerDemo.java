//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import pythagoras.f.IDimension;
import pythagoras.f.Point;

import react.Slot;

import playn.core.Surface;
import playn.scene.Layer;
import playn.scene.LayerUtil;
import playn.scene.Pointer;

import tripleplay.demo.DemoScreen;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Scroller;
import tripleplay.ui.Scroller.Behavior;
import tripleplay.ui.Shim;
import tripleplay.ui.SizableWidget;
import tripleplay.ui.Slider;
import tripleplay.ui.Style;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.util.Colors;

public class ScrollerDemo extends DemoScreen
{
    @Override protected String name () {
        return "Scroller";
    }

    @Override protected String title () {
        return "UI: Scroller";
    }

    @Override protected Group createIface (Root root) {
        final Slider width = new Slider(100, 100, 5000);
        final Slider height = new Slider(100, 100, 5000);
        final Slider xpos = new Slider(0, 0, 1);
        final Slider ypos = new Slider(0, 0, 1);
        final Content content = new Content();
        final Scroller scroll = new Scroller(content);
        final Label click = new Label();

        // updates the size of the content
        Slot<Object> updateSize = v -> ((Content)scroll.content).preferredSize.
            update(width.value.get(), height.value.get());
        width.value.connect(updateSize);
        height.value.connect(updateSize);

        // updates the scroll offset
        Slot<Object> updatePos = v -> {
            float x = xpos.value.get() * scroll.hrange.max();
            float y = ypos.value.get() * scroll.vrange.max();
            scroll.scroll(x, y);
        };
        xpos.value.connect(updatePos);
        ypos.value.connect(updatePos);

        Button behB = new Button(Behavior.BOTH.name()).onClick(source -> {
            Behavior[] behs = Behavior.values();
            Behavior beh = Behavior.valueOf(source.text.get());
            beh = behs[(beh.ordinal() + 1) % behs.length];
            scroll.setBehavior(beh);
            source.text.update(beh.name());
            xpos.setVisible(beh.hasHorizontal());
            ypos.setVisible(beh.hasVertical());
            updateSize.onEmit(null);
        });

        scroll.contentClicked().connect(e -> {
            Point pt = LayerUtil.screenToLayer(content.layer, e.x(), e.y());
            click.text.update(pt.x + ", " + pt.y);
        });

        scroll.addListener(new Scroller.Listener() {
            @Override public void viewChanged (IDimension contentSize, IDimension scrollSize) {}
            @Override public void positionChanged (float x, float y) {
                update(xpos, x, scroll.hrange);
                update(ypos, y, scroll.vrange);
            }

            void update (Slider pos, float val, Scroller.Range range) {
                if (range.max() > 0) pos.value.update(val / range.max());
            }
        });

        // background so we can see when the content is smaller
        scroll.addStyles(Style.BACKGROUND.is(Background.solid(Colors.LIGHT_GRAY).inset(10)));

        updatePos.onEmit(null);
        updateSize.onEmit(null);

        return new Group(AxisLayout.vertical().offStretch()).add(
            new Group(AxisLayout.horizontal()).add(
                new Label("Size:"), new Shim(15, 1), width, new Label("x"), height, behB),
            new Group(AxisLayout.horizontal()).add(
                new Label("Pos:"), new Shim(15, 1), xpos, ypos),
            new Group(AxisLayout.horizontal()).add(
                new Label("Click:"), new Shim(15, 1), click),
            new Group(AxisLayout.horizontal().offStretch()).setConstraint(AxisLayout.stretched()).
                add(scroll.setConstraint(AxisLayout.stretched())));
    }

    protected static class Content extends SizableWidget<Content> {
        public final float tick = 100;

        public Content () {
            layer.add(new Layer() {
                @Override protected void paintImpl (Surface surf) {
                    surf.setFillColor(0xFFFFFFFF);
                    surf.fillRect(0, 0, _size.width, _size.height);

                    float left = 1, top = 1, right = _size.width, bot = _size.height;
                    surf.setFillColor(0xFF7f7F7F);
                    for (float x = 0; x < _size.width; x += tick) {
                        surf.drawLine(x, top, x, bot, 1);
                    }
                    for (float y = 0; y < _size.height; y += tick) {
                        surf.drawLine(left, y, right, y, 1);
                    }

                    surf.setFillColor(0xFFFF7F7F);
                    surf.drawLine(left - 1, top, right, top, 2);
                    surf.drawLine(right - 1, top - 1, right - 1, bot, 2);
                    surf.drawLine(left, top - 1, left, bot, 2);
                    surf.drawLine(left - 1, bot - 1, right, bot - 1, 2);
                }
            });
        }

        @Override protected Class<?> getStyleClass () {
            return Content.class;
        }
    }
}
