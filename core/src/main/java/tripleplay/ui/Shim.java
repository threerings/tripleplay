//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

/**
 * An invisible widget that simply requests a fixed amount of space.
 */
public class Shim extends SizableWidget<Shim>
{
    public Shim (float width, float height) {
        this(new Dimension(width, height));
    }

    public Shim (IDimension size) {
        super(size);
    }

    @Override protected Class<?> getStyleClass () {
        return Shim.class;
    }
}
