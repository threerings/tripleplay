//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import react.Value;

/**
 * A specialized <code>Value</code> for dimensions.
 */
public class DimensionValue extends Value<IDimension>
{
    /**
     * Creates a new value with the given dimension.
     */
    public DimensionValue (IDimension value) {
        super(value);
    }

    /**
     * Creates a new value with a new dimension of the given width and height.
     */
    public DimensionValue (float width, float height) {
        super(new Dimension(width, height));
    }

    /**
     * Updates the value to a new dimension of the given width and height.
     */
    public void update (float width, float height) {
        update(new Dimension(width, height));
    }

    /**
     * Updates the value to a new dimension with the current height and the given width.
     */
    public void updateWidth (float width) {
        update(new Dimension(width, get().height()));
    }

    /**
     * Updates the value to a new dimension with the current width and the given height.
     */
    public void updateHeight (float height) {
        update(new Dimension(get().width(), height));
    }
}
