//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import java.util.Random;

import react.Slot;
import react.UnitSlot;

import playn.core.Color;
import playn.core.Keyboard;

import tripleplay.demo.DemoScreen;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Style;
import tripleplay.ui.Tabs;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.util.Colors;

public class TabsDemo extends DemoScreen
{
    @Override protected String name () {
        return "Tabs";
    }

    @Override protected String title () {
        return "UI: Tabs";
    }

    @Override protected Group createIface (Root root) {
        final int [] lastTab = {0};
        final Tabs tabs = new Tabs().addStyles(Style.BACKGROUND.is(
            Background.bordered(Colors.WHITE, Colors.BLACK, 1).inset(1)));
        final Button moveRight = new Button("Move Right").onClick(new UnitSlot() {
            @Override public void onEmit () {
                Tabs.Tab tab = tabs.selected.get();
                if (movable(tab)) {
                    tabs.repositionTab(tab, tab.index() + 1);
                }
            }
        }).setEnabled(false);
        final Button hide = new Button("Hide").onClick(new UnitSlot() {
            @Override public void onEmit () {
                Tabs.Tab tab = tabs.selected.get();
                if (tab != null) {
                    tab.setVisible(false);
                }
            }
        }).setEnabled(false);
        tabs.selected.connect(new Slot<Tabs.Tab>() {
            @Override public void onEmit (Tabs.Tab tab) {
                moveRight.setEnabled(movable(tab));
                hide.setEnabled(tab != null);
            }
        });
        return new Group(AxisLayout.vertical().offStretch()).add(
            new Group(AxisLayout.horizontal()).add(
                new Button("Add").onClick(new UnitSlot() {
                    @Override public void onEmit () {
                        String label = _prefix + ++lastTab[0];
                        tabs.add(label, tabContent(label));
                    }
                }),
                new Button("Remove...").onClick(new TabSelector(tabs) {
                    @Override public void handle (Tabs.Tab tab) {
                        tabs.destroyTab(tab);
                    }
                }),
                new Button("Highlight...").onClick(new TabSelector(tabs) {
                    @Override public void handle (Tabs.Tab tab) {
                        tabs.highlighter().highlight(tab, true);
                    }
                }), moveRight, hide, new Button("Show All").onClick(new UnitSlot() {
                    @Override public void onEmit () {
                        for (int ii = 0; ii < tabs.tabCount(); ii++) {
                            tabs.tabAt(ii).setVisible(true);
                        }
                    }
                })),
            tabs.setConstraint(AxisLayout.stretched()));
    }

    protected int number (Tabs.Tab tab) {
        return Integer.parseInt(tab.button.text.get().substring(_prefix.length()));
    }

    protected boolean movable (Tabs.Tab tab) {
        int index = tab != null ? tab.index() : -1;
        return index >= 0 && index + 1 < tab.parent().tabCount();
    }

    protected int randColor () {
        return Color.rgb(128 + _rnd.nextInt(127), 128 + _rnd.nextInt(127), 128 + _rnd.nextInt(127));
    }

    protected Group tabContent (String label) {
        return new Group(AxisLayout.vertical().offStretch().stretchByDefault()).add(
            new Label(label).addStyles(Style.BACKGROUND.is(Background.solid(randColor()))));
    }

    protected abstract class TabSelector extends UnitSlot {
        public Tabs tabs;
        public TabSelector (Tabs tabs) {
            this.tabs = tabs;
        }

        @Override public void onEmit () {
            String init = "";
            if (tabs.tabCount() > 0) {
                Tabs.Tab tab = tabs.tabAt(_rnd.nextInt(tabs.tabCount()));
                init = "" + number(tab);
            }
            input().getText(Keyboard.TextType.NUMBER, "Enter tab number", init).
                onSuccess(new Slot<String>() {
                    @Override public void onEmit (String result) {
                        for (int ii = 0; ii < tabs.tabCount(); ii++) {
                            if (result.equals("" + number(tabs.tabAt(ii)))) {
                                handle(tabs.tabAt(ii));
                                break;
                            }
                        }
                    }
                });
        }

        abstract public void handle (Tabs.Tab tab);
    }

    protected String _prefix = "Tab ";
    protected Random _rnd = new Random();
}
