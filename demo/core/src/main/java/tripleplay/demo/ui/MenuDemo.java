package tripleplay.demo.ui;

import playn.core.Image;
import playn.core.PlayN;
import react.Slot;
import react.UnitSlot;
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
        Group main = new Group(AxisLayout.horizontal());
        final MenuHost menuHost = new MenuHost(iface, _root);
        final Button direction = new Button("Select a direction");
        final Button tree = new Button("Select a tree");
        final Button type = new Button("Select a type");
        main.add(direction, tree, type);
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
        return main;
    }

    protected Slot<MenuItem> updater (final Button target) {
        return new Slot<MenuItem>() {
            @Override public void onEmit (MenuItem item) {
                target.text.update(item.text.get());
            }
        };
    }

    protected void addIcons (Menu menu) {
        final Image squares = PlayN.assets().getImage("images/squares.png");
        int tile = 0;
        for (Element<?> item : menu) {
            if (item instanceof MenuItem) {
                ((MenuItem)item).icon.update(tile(squares, tile++));
            }
        }
    }

    protected Image tile (Image image, int index) {
        final float iwidth = 16, iheight = 16;
        return image.subImage(index*iwidth, 0, iwidth, iheight);
    }

    protected Menu createMenu (String title, String... items) {
        Menu menu = new Menu(AxisLayout.vertical().offStretch().gap(3));
        if (title != null) menu.add(new Label(title).addStyles(Style.COLOR.is(0xFFFFFFFF),
            Style.BACKGROUND.is(Background.beveled(0xFF8F8F8F, 0xFF4F4F4F, 0xFFCFCFCF).inset(4))));
        for (String item : items) menu.add(new MenuItem(item));
        return menu;
    }
}
