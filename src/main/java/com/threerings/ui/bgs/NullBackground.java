//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui.bgs;

import pythagoras.f.IDimension;

import forplay.core.GroupLayer;

import com.threerings.ui.Background;

/**
 * A background that displays nothing. This is the default for groups.
 */
public class NullBackground extends Background
{
    public NullBackground () {
        super(0, 0, 0, 0);
    }

    @Override protected Instance instantiate (IDimension size) {
        return SINGLETON;
    }

    protected static final Instance SINGLETON = new Instance() {
        @Override public void addTo (GroupLayer parent) {
            // noop!
        }
        @Override public void destroy () {
            // noop!
        }
    };
}
