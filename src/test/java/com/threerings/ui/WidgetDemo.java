//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import forplay.core.ForPlay;
import forplay.core.Game;
import forplay.java.JavaPlatform;

/**
 * A test app for demoing the UI widgets.
 */
public class WidgetDemo implements Game
{
    public static void main (String[] args) {
        JavaPlatform platform = JavaPlatform.register();
        platform.assetManager().setPathPrefix("src/main/resources");
        ForPlay.run(new WidgetDemo());
    }

    @Override // from interface Game
    public void init () {

        // create our demo interface
        _root = new Root(AxisLayout.vertical().alignTop());
        ForPlay.graphics().rootLayer().add(_root.layer());

        _root.add(new Label("Hello"));
        _root.add(new Label("ForPlay"));
        _root.add(new Label("World"));

        _root.pack();
    }

    @Override // from interface Game
    public void update (float delta) {
        _root.update(delta);
    }

    @Override // from interface Game
    public void paint (float alpha) {
        _root.paint(alpha);
    }

    @Override // from interface Game
    public int updateRate () {
        return 30;
    }

    protected Root _root;
}
