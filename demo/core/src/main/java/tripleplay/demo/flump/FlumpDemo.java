//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.flump;

import playn.core.Clock;

import react.Slot;

import tripleplay.flump.*;
import tripleplay.ui.Group;
import tripleplay.ui.Root;
import tripleplay.ui.layout.AbsoluteLayout;

import tripleplay.demo.DemoScreen;

public class FlumpDemo extends DemoScreen
{
    @Override protected String name () {
        return "Flump";
    }

    @Override protected String title () {
        return "Flump animation";
    }

    @Override protected Group createIface (Root root) {
        final Group main = new Group(new AbsoluteLayout());
        JsonLoader.loadLibrary(game().plat, "flump").onSuccess(new Slot<Library>() {
            public void onEmit (Library lib) {
                final Movie movie = lib.createMovie("walk");
                movie.layer().setTranslation(size().width()/2, 300);
                main.layer.add(movie.layer());
                closeOnHide(paint.connect(new Slot<Clock>() {
                    public void onEmit (Clock clock) { movie.paint(clock); }
                }));
            }
        });
        return main;
    }
}
