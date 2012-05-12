//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game;

import react.UnitSlot;

import playn.core.Game;
import playn.core.PlayN;
import playn.java.JavaPlatform;

import tripleplay.ui.Button;
import tripleplay.ui.Label;

/**
 * Tests/demonstrates screen-related things.
 */
public class ScreenTests implements Game
{
    public static void main (String[] args) {
        JavaPlatform.register();
        PlayN.run(new ScreenTests());
    }

    @Override // from interface Game
    public void init () {
        _stack.push(createScreen(0));
    }

    @Override // from interface Game
    public void update (float delta) {
        _stack.update(delta);
    }

    @Override // from interface Game
    public void paint (float alpha) {
        _stack.paint(alpha);
    }

    @Override // from interface Game
    public int updateRate () {
        return 17;
    }

    protected Screen createScreen (final int depth) {
        return new IfaceScreen() {
            protected void createIface () {
                _root.add(new Label("Screen " + depth));
                if (depth > 0) {
                    Button back = new Button("Back");
                    final IfaceScreen screen = this;
                    back.clicked().connect(new UnitSlot() { public void onEmit () {
                        _stack.remove(screen, _stack.slide().right());
                    }});
                    _root.add(back);
                }
                Button slide = new Button("Slide");
                slide.clicked().connect(new UnitSlot() { public void onEmit () {
                    _stack.push(createScreen(depth+1), _stack.slide());
                }});
                _root.add(slide);
            }
        };
    }

    protected final ScreenStack _stack = new ScreenStack() {
        protected void handleError (RuntimeException error) {
            PlayN.log().warn("Screen failure", error);
        }
    };
}
