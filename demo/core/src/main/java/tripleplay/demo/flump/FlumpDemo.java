//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.flump;

import playn.core.GroupLayer;
import playn.core.ResourceCallback;
import static playn.core.PlayN.*;

import tripleplay.flump.*;
import tripleplay.ui.Group;
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

    @Override protected Group createIface () {
        final Group root = new Group(new AbsoluteLayout());

        Library.fromAssets("flump", new ResourceCallback<Library>() {
            public void done (Library lib) {
                _movie = lib.createMovie("walk");
                _movie.layer().setTranslation(graphics().width()/2, 300);
                root.layer.add(_movie.layer());
            }
            public void error (Throwable cause) { throw new IllegalStateException(cause); }
        });

        return root;
    }

    @Override public void update (float delta) {
        super.update(delta);
        if (_movie != null) {
            _movie.update(delta);
        }
    }

    protected Movie _movie;
}
