package tripleplay.demo.ui;

import playn.core.ImmediateLayer;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.Pointer.Event;
import playn.core.Surface;
import pythagoras.f.IDimension;
import pythagoras.f.Point;
import react.Slot;
import react.UnitSlot;
import tripleplay.demo.DemoScreen;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
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

    @Override protected Group createIface () {
        final Slider width = new Slider(100, 100, 5000);
        final Slider height = new Slider(100, 100, 5000);
        final Slider xpos = new Slider(0, 0, 1);
        final Slider ypos = new Slider(0, 0, 1);
        final Content content = new Content();
        final Scroller scroll = new Scroller(content, iface);
        final Label click = new Label();

        // updates the size of the content
        final UnitSlot updateSize = new UnitSlot() {
            @Override public void onEmit () {
                ((Content)scroll.content).preferredSize.update(
                    width.value.get(), height.value.get());
            }
        };
        width.value.connect(updateSize);
        height.value.connect(updateSize);

        // updates the scroll offset
        UnitSlot updatePos = new UnitSlot() {
            @Override public void onEmit () {
                float x = xpos.value.get() * scroll.hbar.max();
                float y = ypos.value.get() * scroll.vbar.max();
                scroll.scroll(x, y);
            }
        };
        xpos.value.connect(updatePos);
        ypos.value.connect(updatePos);

        Button beh = new Button(Behavior.BOTH.name());
        beh.clicked().connect(new Slot<Button>() {
            @Override public void onEmit (Button event) {
                Behavior[] behs = Behavior.values();
                Behavior beh = Behavior.valueOf(event.text.get());
                beh = behs[(beh.ordinal() + 1) % behs.length];
                scroll.setBehavior(beh);
                event.text.update(beh.name());
                xpos.setVisible(beh.hasHorizontal());
                ypos.setVisible(beh.hasVertical());
                updateSize.onEmit();
            }
        });

        scroll.contentClicked().connect(new Slot<Pointer.Event>() {
            @Override public void onEmit (Event e) {
                Point pt = Layer.Util.screenToLayer(content.layer, e.x(), e.y());
                click.text.update(pt.x + ", " + pt.y);
            }
        });

        scroll.addListener(new Scroller.Listener() {
            @Override public void viewChanged (IDimension contentSize, IDimension scrollSize) {}
            @Override public void positionChanged (float x, float y) {
                update(xpos, x, scroll.hbar);
                update(ypos, y, scroll.vbar);
            }

            void update (Slider pos, float val, Scroller.Bar bar) {
                if (bar.max() > 0) pos.value.update(val / bar.max());
            }
        });

        // background so we can see when the content is smaller
        scroll.addStyles(Style.BACKGROUND.is(Background.solid(Colors.LIGHT_GRAY).inset(10)));

        updatePos.onEmit();
        updateSize.onEmit();

        return new Group(AxisLayout.vertical().offStretch()).add(
            new Group(AxisLayout.horizontal()).add(
                new Label("Size:"), new Shim(15, 1), width, new Label("x"), height, beh),
            new Group(AxisLayout.horizontal()).add(
                new Label("Pos:"), new Shim(15, 1), xpos, ypos),
            new Group(AxisLayout.horizontal()).add(
                new Label("Click:"), new Shim(15, 1), click),
            new Group(AxisLayout.horizontal().offStretch()).setConstraint(AxisLayout.stretched()).
                add(scroll.setConstraint(AxisLayout.stretched())));
    }

    protected static class Content extends SizableWidget<Content>
        implements ImmediateLayer.Renderer
    {
        public final float tick = 100;

        public Content () {
            layer.add(PlayN.graphics().createImmediateLayer(this));
        }

        @Override public void render (Surface surf) {
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

        @Override protected Class<?> getStyleClass () {
            return Content.class;
        }
    }
}
