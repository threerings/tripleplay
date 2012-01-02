//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.layout;

import playn.core.Asserts;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.IPoint;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;

import tripleplay.ui.Element;
import tripleplay.ui.Elements;
import tripleplay.ui.Layout;

/**
 * A layout that positions elements at absolute coordinates (at either their preferred size or at a
 * manually specified size).
 */
public class AbsoluteLayout extends Layout
{
    /** Defines absolute layout constraints. */
    public static final class Constraint extends Layout.Constraint {
        public final IPoint position;
        public final IDimension size;

        public Constraint (IPoint position, IDimension size) {
            this.position = position;
            this.size = size;
        }
    }

    /**
     * Creates a layout constraint that positions the element at the specified coordinates, in its
     * preferred size.
     */
    public static Constraint at (float x, float y) {
        return at(new Point(x, y));
    }

    /**
     * Creates a layout constraint that positions the element at the specified coordinates, in its
     * preferred size.
     */
    public static Constraint at (IPoint position) {
        return at(position, new Dimension(0, 0));
    }

    /**
     * Creates a layout constraint that positions the element at the specified coordinates in the
     * specified size.
     */
    public static Constraint at (float x, float y, float width, float height) {
        return at(new Point(x, y), new Dimension(width, height));
    }

    /**
     * Creates a layout constraint that positions the element at the specified coordinates in the
     * specified size.
     */
    public static Constraint at (IPoint position, IDimension size) {
        return new Constraint(position, size);
    }

    @Override public Dimension computeSize (Elements<?> elems, float hintX, float hintY) {
        // report a size large enough to contain all of our elements
        Rectangle bounds = new Rectangle();
        for (Element<?> elem : elems) {
            if (!elem.isVisible()) continue;
            Constraint c = constraint(elem);
            IDimension psize = preferredSize(elem, c.size.width(), c.size.height());
            bounds.add(new Rectangle(c.position, c.size.width() == 0 ? psize : c.size));
        }
        return new Dimension(bounds.width, bounds.height);
    }

    @Override public void layout (Elements<?> elems,
                                  float left, float top, float width, float height) {
        for (Element<?> elem : elems) {
            if (!elem.isVisible()) continue;
            Constraint c = constraint(elem);
            if (c.size.width() == 0) {
                // this should return a cached size
                IDimension psize = preferredSize(elem, 0, 0);
                setBounds(elem, c.position.x(), c.position.y(), psize.width(), psize.height());
            } else {
                setBounds(elem, c.position.x(), c.position.y(), c.size.width(), c.size.height());
            }
        }
    }

    protected static Constraint constraint (Element<?> elem) {
        return (Constraint)Asserts.checkNotNull(
            elem.constraint(), "Elements in AbsoluteLayout must have a constraint.");
    }
}
