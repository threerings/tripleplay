//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import playn.core.Game;
import pythagoras.f.IDimension;
import react.*;

import tripleplay.game.ScreenStack;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.TableLayout;

import tripleplay.demo.anim.*;
import tripleplay.demo.entity.*;
import tripleplay.demo.flump.*;
import tripleplay.demo.game.*;
import tripleplay.demo.particle.*;
import tripleplay.demo.ui.*;
import tripleplay.demo.util.*;

/**
 * Displays a top-level menu of our various demo screens.
 */
public class DemoMenuScreen extends ScreenStack.UIScreen
{
    public DemoMenuScreen (ScreenStack stack) {
        super(TripleDemo.game.plat);

        _stack = stack;
        _rlabels = new String[] {
            "tripleplay.ui", "", "", "", "",
            "tripleplay.anim",
            "tripleplay.game",
            "tripleplay.entity",
            "tripleplay.particle",
            "tripleplay.flump",
            "tripleplay.util"
        };
        _screens = new DemoScreen[] {
            // tripleplay.ui
            new MiscDemo(), new LabelDemo(), new MenuDemo(),
            new SliderDemo(), new SelectorDemo(), new BackgroundDemo(),
            new ScrollerDemo(), new TabsDemo(), new HistoryGroupDemo(),
            new LayoutDemo(), new FlowLayoutDemo(), new BorderLayoutDemo(),
            new TableLayoutDemo(), new AbsoluteLayoutDemo(), null,
            // tripleplay.anim
            new FramesDemo(), new AnimDemo(), new FlickerDemo(),
            // tripleplay.game
            new ScreensDemo(stack), new ScreenSpaceDemo(), null,
            // tripleplay.entity
            new AsteroidsDemo(), null, null,
            // tripleplay.particle
            new FountainDemo(), new FireworksDemo(), null,
            // // tripleplay.flump
            new FlumpDemo(), null, null,
            // tripleplay.util
            new ColorsDemo(), new InterpDemo(), null,
        };
    }

    @Override public Game game () { return TripleDemo.game; }

    @Override public void wasAdded () {
        super.wasAdded();
        final Root root = iface.createRoot(AxisLayout.vertical().gap(15),
                                           SimpleStyles.newSheet(game().plat.graphics()), layer);
        root.addStyles(Style.BACKGROUND.is(
            Background.bordered(0xFFCCCCCC, 0xFF99CCFF, 5).inset(5, 10)));
        sizeValue().connectNotify(new Slot<IDimension>() {
            public void onEmit (IDimension size) {
                root.setSize(size);
            }
        });
        root.add(new Label("Triple Play Demos").addStyles(Style.FONT.is(DemoScreen.TITLE_FONT)));

        Group grid = new Group(new TableLayout(
            TableLayout.COL.alignRight(),
            TableLayout.COL.stretch(),
            TableLayout.COL.stretch(),
            TableLayout.COL.stretch()).gaps(10, 10));
        root.add(grid);

        int shown = 0, toShow = (TripleDemo.mainArgs.length == 0) ? -1 :
            Integer.parseInt(TripleDemo.mainArgs[0]);

        for (int ii = 0; ii < _screens.length; ii++) {
            if (ii%3 == 0) grid.add(new Label(_rlabels[ii/3]));
            final DemoScreen screen = _screens[ii];
            if (screen == null) {
                grid.add(new Shim(1, 1));
            } else {
                grid.add(new Button(screen.name()).onClick(new UnitSlot() { public void onEmit () {
                    _stack.push(screen);
                    screen.back.clicked().connect(new UnitSlot() { public void onEmit () {
                        _stack.remove(screen);
                    }});
                }}));
                // push this screen immediately if it was specified on the command line
                if (shown++ == toShow) _stack.push(screen, ScreenStack.NOOP);
            }
        }
    }

    @Override public void wasRemoved () {
        super.wasRemoved();
        iface.disposeRoots();
    }

    protected Button screen (String title, final ScreenFactory factory) {
        return new Button(title).onClick(new UnitSlot() { public void onEmit () {
            final DemoScreen screen = factory.apply();
            _stack.push(screen);
            screen.back.clicked().connect(new UnitSlot() { public void onEmit () {
                _stack.remove(screen);
            }});
        }});
    }

    protected interface ScreenFactory {
        DemoScreen apply ();
    }

    protected final String[] _rlabels;
    protected final DemoScreen[] _screens;
    protected final ScreenStack _stack;
}
