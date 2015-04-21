//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.game;

import playn.core.Clock;
import playn.core.Game;

import react.Slot;
import react.UnitSlot;

import tripleplay.demo.DemoScreen;
import tripleplay.demo.TripleDemo;
import tripleplay.game.ScreenSpace;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;

public class ScreenSpaceDemo extends DemoScreen {

    @Override protected String name () {
        return "ScreenSpace";
    }
    @Override protected String title () {
        return "ScreenSpace and Transitions";
    }

    @Override protected Group createIface (Root root) {
        // things are a bit hacky here because we're bridging between the ScreenStack world (which
        // is used for the demo) and the ScreenSpace world; normally a game would use *only*
        // ScreenStack or ScreenSpace, but for this demo we want to show both; so we put no UI in
        // our ScreenStack screen, and let the root ScreenSpace screen do the driving
        _space = new ScreenSpace(TripleDemo.game, TripleDemo.game.rootLayer);
        closeOnHide(paint.connect(new Slot<Clock>() { public void onEmit (Clock clock) {
            // bind the pos of our stack screen to the position of our top space screen
            if (_top != null) {
                layer.setTx(_top.layer.tx());
                layer.setTy(_top.layer.ty());
            }
        }}));
        return new Group(AxisLayout.vertical());
    }

    @Override public void showTransitionCompleted () {
        _space.add(_top = createScreen(0), ScreenSpace.Dir.FLIP);
    }

    @Override public void hideTransitionStarted () {
        _space.pop(_top);
        _top = null;
    }

    @Override public void wasRemoved () {
        super.wasRemoved();
        _space = null;
    }

    protected UnitSlot addScreen (final ScreenSpace.Dir dir) {
        return new UnitSlot() {
            public void onEmit () {
                ScreenSpace.Screen screen = createScreen(_space.screenCount());
                _space.add(screen, dir);
            }
        };
    }

    protected ScreenSpace.Screen createScreen (final int id) {
        return new ScreenSpace.UIScreen() {
            @Override public Game game () { return TripleDemo.game; }
            @Override public String toString () {
                return "Screen-" + id;
            }
            @Override protected void createUI () {
                Root root = iface.createRoot(AxisLayout.vertical(),
                                             SimpleStyles.newSheet(graphics()), layer);
                int blue = (id * 0x16);
                root.addStyles(Style.BACKGROUND.is(Background.solid(0xFF333300+blue)));
                root.add(new Label(toString()));
                for (ScreenSpace.Dir dir : ScreenSpace.Dir.values()) {
                    root.add(new Button(dir.name()).onClick(addScreen(dir)));
                }
                final ScreenSpace.Screen self = this;
                root.add(new Shim(30, 30), new Button("Pop").onClick(new UnitSlot() {
                    public void onEmit () {
                        if (_top == self) {
                            ScreenSpaceDemo.this.back.click();
                        } else {
                            _space.pop(self);
                        }
                    }
                }));
                root.setSize(size());
            }
        };
    }

    protected ScreenSpace _space;
    protected ScreenSpace.Screen _top;
}
