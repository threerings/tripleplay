//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

/**
 * An invisible widget that simply requests a fixed amount of space.
 */
public class Shim extends Element<Shim>
{
    public Shim (float width, float height) {
        _size = new Dimension(width, height);
    }

    public Shim (IDimension size) {
        _size = new Dimension(size);
    }

    @Override protected Class<?> getStyleClass () {
        return Shim.class;
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new SizableLayoutData(null, _size);
    }

    protected final Dimension _size;
}
