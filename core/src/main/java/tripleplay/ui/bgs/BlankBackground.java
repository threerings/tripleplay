//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;

import playn.core.GroupLayer;

import tripleplay.ui.Background;

/**
 * A background that displays nothing. This is the default for groups.
 */
public class BlankBackground extends Background
{
    @Override protected Instance instantiate (IDimension size) {
        return SINGLETON;
    }

    protected static final Instance SINGLETON = new Instance() {
        @Override public void addTo (GroupLayer parent, float x, float y) {
            // noop!
        }
        @Override public void destroy () {
            // noop!
        }
    };
}
