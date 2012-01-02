//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.List;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

/**
 * Defines the interface to layouts, which implement a particular layout policy.
 */
public abstract class Layout
{
    /** An abstract base class for all layout constraints. */
    public static abstract class Constraint {
        /** Called by an element when it is configured with a constraint. */
        public void setElement (Element<?> elem) {
            // nothing needed by default
        }

        /** Allows a layout constraint to adjust an element's preferred size. */
        public void adjustPreferredSize (Dimension psize, float hintX, float hintY) {
            // no adjustments by default
        }
    }

    /**
     * Computes and returns the size needed to arrange children of the supplied elements according
     * to their preferred size, given the specified x and y size hints.
     */
    public abstract Dimension computeSize (Elements<?> elems, float hintX, float hintY);

    /**
     * Lays out the supplied elements into a region of the specified dimensions.
     */
    public abstract void layout (Elements<?> elems, float left, float top,
                                 float width, float height);

    // make Element.resolveStyle "visible" to custom layouts
    protected <V> V resolveStyle (Element<?> elem, Style<V> style) {
        return elem.resolveStyle(style);
    }

    // make Element.preferredSize "visible" to custom layouts
    protected IDimension preferredSize (Element<?> elem, float hintX, float hintY) {
        return elem.preferredSize(hintX, hintY);
    }

    protected void setBounds (Element<?> elem, float x, float y, float width, float height) {
        elem.setLocation(x, y);
        elem.setSize(width, height);
    }
}
