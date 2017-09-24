//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import java.io.PrintWriter;

import playn.core.*;
import playn.scene.GroupLayer;
import playn.scene.Layer;
import playn.scene.Pointer;
import playn.scene.SceneGame;

import tripleplay.game.ScreenStack;

public class TripleDemo extends SceneGame {

    /** Args from the Java bootstrap class. */
    public static String[] mainArgs = {};

    /** A static reference to our game. If we were starting from scratch, I'd pass the game
      * instance to the screen constructor of all of our demo screens, but doing that now is too
      * much pain in my rear. */
    public static TripleDemo game;

    public final ScreenStack screens = new ScreenStack(this, rootLayer) {
        @Override protected Transition defaultPushTransition () { return slide(); }
        @Override protected Transition defaultPopTransition () { return slide().right(); }
    };

    public TripleDemo (Platform plat) {
        super(plat, 25); // update our "simulation" 40 times per second
        game = this;     // jam ourselves into a global variable, woo!
        new Pointer(plat, rootLayer, true);        // wire up event dispatch
        screens.push(new DemoMenuScreen(screens)); // start off with our menu screen

        // show debug rectangles when D key is pressed; dump scene graph on shift-D
        plat.input().keyboardEvents.collect(Keyboard.isKey(Key.D)).connect(event -> {
            Layer.DEBUG_RECTS = event.down;
            if (event.down && event.isShiftDown()) {
              rootLayer.debugPrint(plat.log());
            }
        });
    }
}
