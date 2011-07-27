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
        _root = new Root(AxisLayout.vertical());
        _root.setSize(ForPlay.graphics().width(), ForPlay.graphics().height());
        ForPlay.graphics().rootLayer().add(_root.layer());

        Group cols = new Group(AxisLayout.horizontal().alignTop());
        cols.setStyle(Group.BACKGROUND, Background.solid(0xFFFFCC99, 5));
        cols.add(new Label(TEXT1), AxisLayout.stretched());
        cols.add(new Label(TEXT2), AxisLayout.stretched());
        cols.add(new Label(TEXT3), AxisLayout.stretched());
        _root.add(cols);
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

    protected static final String TEXT1 = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";
    protected static final String TEXT2 = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.";
    protected static final String TEXT3 = "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness.";
}
