//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import playn.core.Image;
import playn.core.PlayN;
import playn.core.Pointer;
import react.Slot;
import react.UnitSlot;
import react.Value;
import tripleplay.anim.Animation;
import tripleplay.anim.Animator;
import tripleplay.demo.DemoScreen;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Element;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Menu;
import tripleplay.ui.Menu.AnimFn;
import tripleplay.ui.MenuHost;
import tripleplay.ui.MenuItem;
import tripleplay.ui.Shim;
import tripleplay.ui.Style;
import tripleplay.ui.layout.AxisLayout;

public class MenuDemo extends DemoScreen
{
    @Override protected String name () {
        return "Menus";
    }

    @Override protected String title () {
        return "UI: Menu";
    }

    @Override protected Group createIface () {
        final MenuHost menuHost = new MenuHost(iface, _root);
        final Button direction = new Button("Select a direction \u25BC");
        final Button tree = new Button("Select a tree \u25BC");
        final Button type = new Button("Select a type \u25BC");
        direction.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                MenuHost.Pop pop = new MenuHost.Pop(direction,
                    createMenu("Directions", "North", "South", "East", "West")).toRight(2).toTop(0);
                pop.menu.itemTriggered().connect(updater(direction));
                addIcons(pop.menu);
                menuHost.popup(pop);
            }
        });
        tree.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                MenuHost.Pop pop = new MenuHost.Pop(tree,
                    createMenu("Trees", "Elm", "Ash", "Maple", "Oak")).toBottom(2).toLeft(0);
                pop.menu.itemTriggered().connect(updater(tree));
                menuHost.popup(pop);
            }
        });
        type.clicked().connect(new UnitSlot() {
            @Override public void onEmit () {
                MenuHost.Pop pop = new MenuHost.Pop(type,
                    createMenu(null, "Road", "Street", "Boulevard", "Avenue")).toBottom(2).toLeft(0);
                pop.menu.itemTriggered().connect(updater(type));
                pop.menu.addStyles(Menu.OPENER.is(new AnimFn() {
                    @Override public Animation go (Menu menu, Animator animator) {
                        // TODO: fix short delay where menu is visible at this scale
                        menu.layer.setScale(1, .25f);
                        return animator.tweenScaleY(menu.layer).to(1).easeOut().in(125);
                    }
                }));
                menuHost.popup(pop);
            }
        });

        TrackingLabel subject = new TrackingLabel(menuHost, "Subject \u25BC") {
            @Override public Menu createMenu () {
                return addIcons(MenuDemo.this.createMenu(
                    null, "The giant", "Jack", "The goose", "Jack's mum"));
            }
        };
        TrackingLabel verb = new TrackingLabel(menuHost, "Verb \u25BC") {
            @Override public Menu createMenu () {
                return addIcons(showText(MenuDemo.this.createMenu(
                    null, "climbs", "lays", "crushes", "hugs"),
                        MenuItem.ShowText.WHEN_ACTIVE));
            }
        };
        TrackingLabel object = new TrackingLabel(menuHost, "Object \u25BC") {
            @Override public Menu createMenu () {
                return MenuDemo.this.createMenu(
                    null, "the beanstalk", "people", "golden eggs", "the boy");
            }
        };

        TrackingLabel depth = new TrackingLabel(menuHost, "Floors \u25BC") {
            @Override public Menu createMenu () {
                Menu menu = new Menu(AxisLayout.horizontal().offStretch(), Style.VALIGN.top);
                Group g1 = new Group(AxisLayout.vertical(), Style.VALIGN.top);
                g1.add(new Group(AxisLayout.horizontal()).add(
                    new MenuItem("1A"), new MenuItem("1B"), new MenuItem("1C")));
                g1.add(new Group(AxisLayout.horizontal()).add(
                    new MenuItem("2A"), new MenuItem("2B")));
                g1.add(new Group(AxisLayout.horizontal()).add(
                    new MenuItem("3A"), new MenuItem("3B"), new MenuItem("3C")));
                Group g2 = new Group(AxisLayout.vertical(), Style.HALIGN.right);
                g2.add(new MenuItem("Roof", tile(0)), new MenuItem("Basement", tile(1)));
                return menu.add(g1, g2);
            }
        };

        return new Group(AxisLayout.vertical().offStretch()).add(
            new Label("Button popups"),
            new Group(AxisLayout.horizontal()).add(direction, tree, type),
            new Shim(1, 20),
            new Label("Continuous Tracking"),
            new Group(AxisLayout.horizontal()).add(subject, verb, object),
            new Shim(1, 20),
            new Label("Intermedate groups"),
            new Group(AxisLayout.horizontal()).add(depth));
    }

    protected Slot<MenuItem> updater (final Button button) {
        return updater(button.text, button.icon);
    }

    protected Slot<MenuItem> updater (final Value<String> text, final Value<Image> icon) {
        return new Slot<MenuItem>() {
            @Override public void onEmit (MenuItem item) {
                text.update(item.text.get() + " \u25BC");
                icon.update(item.icon.get());
            }
        };
    }

    protected Menu addIcons (Menu menu) {
        int tile = 0;
        for (Element<?> item : menu) {
            if (item instanceof MenuItem) {
                ((MenuItem)item).icon.update(tile(tile++));
            }
        }
        return menu;
    }

    protected Menu showText (Menu menu, MenuItem.ShowText showText) {
        for (Element<?> item : menu) {
            if (item instanceof MenuItem) {
                ((MenuItem)item).showText(showText);
            }
        }
        return menu;
    }

    protected Image tile (int index) {
        final float iwidth = 16, iheight = 16;
        return _squares.subImage(index*iwidth, 0, iwidth, iheight);
    }

    protected Menu createMenu (String title, String... items) {
        Menu menu = new Menu(AxisLayout.vertical().offStretch().gap(3));
        if (title != null) menu.add(new Label(title).addStyles(Style.COLOR.is(0xFFFFFFFF),
            Style.BACKGROUND.is(Background.beveled(0xFF8F8F8F, 0xFF4F4F4F, 0xFFCFCFCF).inset(4))));
        for (String item : items) menu.add(new MenuItem(item));
        return menu;
    }

    protected abstract class TrackingLabel extends Label
    {
        public MenuHost menuHost;

        public TrackingLabel (MenuHost menuHost, String text) {
            super(text, null);
            this.menuHost = menuHost;
            enableInteraction();
        }

        public abstract Menu createMenu ();

        @Override public void onPointerStart (Pointer.Event ev, float x, float y) {
            MenuHost.Pop pop = new MenuHost.Pop(this, createMenu()).
                    atEventPos(ev).relayEvents(layer);
            pop.menu.itemTriggered().connect(updater(text, icon));
            menuHost.popup(pop);
        }
    }

    protected Image _squares = PlayN.assets().getImage("images/squares.png");
}
