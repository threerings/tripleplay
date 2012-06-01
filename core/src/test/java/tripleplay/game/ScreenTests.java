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
    public static final int UPDATE_RATE = 17;

    public static void main (String[] args) {
        JavaPlatform.register();
        PlayN.run(new ScreenTests());
    }

    @Override // from interface Game
    public void init () {
        _stack.push(_top = createScreen(0));
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
        return UPDATE_RATE;
    }

    protected Screen createScreen (final int depth) {
        return new TestScreen() {
            @Override public String toString () {
                return "Screen" + depth;
            }
            @Override protected void createIface () {
                _root.add(new Label("Screen " + depth));

                addButton("Slide", new UnitSlot() { public void onEmit () {
                    _stack.push(createScreen(depth+1), _stack.slide());
                }});
                addButton("Flip", new UnitSlot() { public void onEmit () {
                    _stack.push(createScreen(depth+1), _stack.pageFlip());
                }});

                if (depth > 0) {
                    addButton("Replace", new UnitSlot() { public void onEmit () {
                        _stack.replace(createScreen(depth+1), _stack.slide().left());
                    }});

                    final TestScreen screen = this;
                    addButton("Back", new UnitSlot() { public void onEmit () {
                        _stack.remove(screen, _stack.pageFlip().unflip());
                    }});

                    addButton("Top", new UnitSlot() { public void onEmit () {
                        _stack.popTo(_top, _stack.slide().right());
                    }});
                }
            }

            protected void addButton (String label, UnitSlot onClick) {
                Button button = new Button(label);
                button.clicked().connect(onClick);
                _root.add(button);
            }
        };
    }

    protected Screen _top;
    protected final ScreenStack _stack = new ScreenStack() {
        protected void handleError (RuntimeException error) {
            PlayN.log().warn("Screen failure", error);
        }
    };
}
