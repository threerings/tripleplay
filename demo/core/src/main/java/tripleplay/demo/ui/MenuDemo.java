//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import pythagoras.f.FloatMath;
import pythagoras.f.Rectangle;

import react.Slot;
import react.Value;

import playn.core.Canvas;
import playn.core.Image;
import playn.scene.Pointer;

import tripleplay.anim.Animation;
import tripleplay.anim.Animator;
import tripleplay.demo.DemoScreen;
import tripleplay.ui.*;
import tripleplay.ui.Root;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.TableLayout;
import tripleplay.ui.util.BoxPoint;
import tripleplay.util.Colors;

public class MenuDemo extends DemoScreen
{
    @Override protected String name () {
        return "Menus";
    }

    @Override protected String title () {
        return "UI: Menu";
    }

    @Override protected Group createIface (Root root) {
        final MenuHost menuHost = new MenuHost(iface, root);
        BoxPoint popRight = new BoxPoint(1, 0, 2, 0);
        BoxPoint popUnder = new BoxPoint(0, 1, 0, 2);
        Button direction = new Button("Select a direction \u25BC").
            addStyles(MenuHost.TRIGGER_POINT.is(MenuHost.relative(popRight))).
            onClick(new Slot<Button>() {
                @Override public void onEmit (Button self) {
                    MenuHost.Pop pop = new MenuHost.Pop(self,
                        createMenu("Directions", "North", "South", "East", "West"));
                    pop.menu.itemTriggered().connect(updater(self));
                    addIcons(pop.menu);
                    menuHost.popup(pop);
                }
            });
        Button tree = new Button("Select a tree \u25BC").
            addStyles(MenuHost.TRIGGER_POINT.is(MenuHost.relative(popUnder))).
            onClick(new Slot<Button>() {
                @Override public void onEmit (Button self) {
                    MenuHost.Pop pop = new MenuHost.Pop(self,
                        createMenu("Trees", "Elm", "Ash", "Maple", "Oak"));
                    pop.menu.itemTriggered().connect(updater(self));
                    menuHost.popup(pop);
                }
            });
        Button type = new Button("Select a type \u25BC").
            addStyles(
                MenuHost.TRIGGER_POINT.is(MenuHost.relative(popUnder)),
                MenuHost.POPUP_ORIGIN.is(BoxPoint.BR)).
            onClick(new Slot<Button>() {
                @Override public void onEmit (Button self) {
                    MenuHost.Pop pop = new MenuHost.Pop(self,
                        createMenu(null, "Road", "Street", "Boulevard", "Avenue"));
                    pop.menu.itemTriggered().connect(updater(self));
                    pop.menu.addStyles(Menu.OPENER.is(new Menu.AnimFn() {
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
        TrackingLabel cells = new TrackingLabel(menuHost, "Ship Locations \u25BC") {
            Menu menu;
            @Override public Menu createMenu () {
                if (menu != null) {
                    return menu;
                }
                String letters = "ABCDEFGHIJ";
                menu = new Menu(AxisLayout.horizontal().offStretch(), Style.VALIGN.top);
                Group g = new Group(new TableLayout(10));
                g.setStylesheet(Stylesheet.builder().add(MenuItem.class, Styles.none().
                    add(Style.BACKGROUND.is(Background.blank().inset(5, 1))).
                    addSelected(Style.BACKGROUND.is(Background.solid(Colors.BLACK).inset(5, 1)))).
                    create());
                for (int col = 0; col < 10; col++) {
                    for (int row = 0; row < 10; row++) {
                       g.add(new MenuItem(letters.substring(col, col+1) + (row + 1)));
                    }
                }
                return menu.add(g);
            }

            @Override public MenuHost.Pop makePop (Pointer.Event ev) {
                return super.makePop(ev).retainMenu();
            }

            @Override protected void wasRemoved () {
                super.wasRemoved();
                if (menu != null) menu.layer.close();
            }
        };

        TrackingLabel scrolled = new TrackingLabel(menuHost, "Bits \u25BC") {
            @Override public Menu createMenu () {
                final Menu menu = new Menu(AxisLayout.vertical().offStretch());
                menu.add(new Label("Select a byte").addStyles(Style.COLOR.is(0xFFFFFFFF),
                    Style.BACKGROUND.is(Background.beveled(0xFF8F8F8F, 0xFF4F4F4F, 0xFFCFCFCF).
                        inset(4))));
                Group items = new Group(AxisLayout.vertical());
                Scroller scroller = new Scroller(items);
                menu.add(new SizableGroup(AxisLayout.vertical().offStretch(), 0, 200).add(
                    scroller.setBehavior(Scroller.Behavior.VERTICAL).
                        setConstraint(AxisLayout.stretched())));

                StringBuilder bits = new StringBuilder();
                for (int ii = 0; ii < 256; ii++) {
                    bits.setLength(0);
                    for (int mask = 128; mask > 0; mask >>= 1) {
                        bits.append((ii & mask) != 0 ? 1 : 0);
                    }
                    items.add(new MenuItem(bits.toString()));
                }
                return menu;
            }
        };

        TrackingLabel bytes = new TrackingLabel(menuHost, "Bytes \u25BC") {
            final String HEX = "0123456789ABCDEF";
            @Override public Menu createMenu () {
                final PagedMenu menu = new PagedMenu(new TableLayout(2), 16);
                menu.add(new Label("Select a byte").addStyles(Style.COLOR.is(0xFFFFFFFF),
                    Style.BACKGROUND.is(Background.beveled(0xFF8F8F8F, 0xFF4F4F4F, 0xFFCFCFCF).
                        inset(4))).setConstraint(new TableLayout.Colspan(2)));
                final Button prev = new Button("<< Previous").onClick(menu.incrementPage(-1));
                final Button next = new Button("Next >>").onClick(menu.incrementPage(1));
                menu.add(prev, next);

                Slot<Object> updateEnabling = v -> {
                    prev.setEnabled(menu.page().get() > 0);
                    next.setEnabled(menu.page().get() < menu.numPages().get() - 1);
                };
                menu.page().connect(updateEnabling);
                menu.numPages().connect(updateEnabling);

                int sel = -1;
                for (int ii = 0; ii < 256; ii++) {
                    String hex = new StringBuilder("0x").
                        append(HEX.charAt((ii>>4)&0xf)).
                        append(HEX.charAt(ii&0xf)).toString();
                    if (text.get().startsWith(hex)) sel = ii;
                    menu.add(new MenuItem(hex));
                }
                if (sel != -1) menu.setPage(sel / menu.itemsPerPage);
                updateEnabling.onEmit(null);
                return menu;
            }
        };

        TrackingLabel colors = new TrackingLabel(menuHost, "Colors \u25BC") {
            @Override public Menu createMenu () {
                final PagedMenu menu = new PagedMenu(AxisLayout.vertical(), 32);
                final Slider slider = new Slider().setIncrement(1);
                slider.value.connect(new Slot<Float>() {
                    @Override public void onEmit (Float val) {
                        menu.setPage(FloatMath.round(val));
                    }
                });
                menu.page().connect(new Slot<Integer>() {
                    @Override public void onEmit (Integer page) {
                        slider.value.update(page.floatValue());
                    }
                });
                menu.numPages().connect(new Slot<Integer>() {
                    @Override public void onEmit (Integer numPages) {
                        slider.range.update(new Slider.Range(0, numPages.intValue() - 1));
                        slider.setEnabled(numPages > 0);
                    }
                });

                Styles itemStyles = Styles.none().add(Style.Mode.SELECTED,
                        Style.BACKGROUND.is(Background.solid(Colors.BLUE).inset(2))).
                    add(Style.BACKGROUND.is(Background.blank().inset(2)));
                Group colorTable = new Group(new TableLayout(4));
                for (int ii = 0; ii < 256; ii++) {
                    Canvas colorImg = graphics().createCanvas(16, 16);
                    colorImg.setFillColor(0xFF000000 | (ii << 16));
                    colorImg.fillRect(0, 0, 16, 16);
                    colorTable.add(new MenuItem("", Icons.image(colorImg.toTexture())).
                                   addStyles(itemStyles));
                }
                menu.add(colorTable, slider);
                return menu;
            }
        };

        return new Group(AxisLayout.vertical().offStretch()).add(
            new Label("Button popups"),
            new Group(AxisLayout.horizontal()).add(direction, tree, type),
            new Shim(1, 20),
            new Label("Continuous Tracking"),
            new Group(AxisLayout.horizontal()).add(subject, verb, object),
            new Shim(1, 20),
            new Label("Intermediate groups"),
            new Group(AxisLayout.horizontal()).add(depth, cells),
            new Shim(1, 20),
            new Label("Scrolling and Paging"),
            new Group(AxisLayout.horizontal()).add(scrolled, bytes, colors));
    }

    protected Slot<MenuItem> updater (final Button button) {
        return updater(button.text, button.icon);
    }

    protected Slot<MenuItem> updater (final Value<String> text, final Value<Icon> icon) {
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

    protected Icon tile (int index) {
        final float iwidth = 16, iheight = 16;
        return Icons.image(_squares.region(index*iwidth, 0, iwidth, iheight));
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
            super(text);
            this.menuHost = menuHost;
            addStyles(MenuHost.TRIGGER_POINT.is(MenuHost.pointer()));
        }

        public abstract Menu createMenu ();

        @Override protected Behavior<Label> createBehavior () {
            return new Behavior.Select<Label>(this) {
                @Override public void onStart (Pointer.Interaction iact) {
                    MenuHost.Pop pop = makePop(iact.event);
                    pop.menu.itemTriggered().connect(updater(text, icon));
                    menuHost.popup(pop);
                }
            };
        }

        protected MenuHost.Pop makePop (Pointer.Event ev) {
            return new MenuHost.Pop(this, createMenu(), ev).relayEvents(layer);
        }
    }

    protected Image _squares = assets().getImage("images/squares.png");
}
