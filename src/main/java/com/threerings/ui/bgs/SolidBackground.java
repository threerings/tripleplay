//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui.bgs;

import pythagoras.f.IDimension;

import forplay.core.GroupLayer;

import com.threerings.ui.Background;

/**
 * A background that displays a solid color.
 */
public class SolidBackground extends Background
{
    /**
     * Creates a solid background with the specified color and insets.
     */
    public SolidBackground (int color, float top, float right, float bottom, float left) {
        super(top, right, bottom, left);
        _color = color;
    }

    @Override protected Instance instantiate (IDimension size) {
        return new LayerInstance(createSolidLayer(_color, size.getWidth(), size.getHeight()));
    }

    protected int _color;
}
