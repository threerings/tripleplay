//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.Point;

/**
 * An invisible widget that simply requests a fixed amount of space.
 */
public class Shim extends Element<Shim>
{
    public Shim (float width, float height) {
        _shimWidth = width;
        _shimHeight = height;
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        return new Dimension(_shimWidth, _shimHeight);
    }

    @Override protected void layout () {
    }

    protected final float _shimWidth, _shimHeight;
}
