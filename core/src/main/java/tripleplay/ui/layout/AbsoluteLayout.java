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
 * manually specified size). Constraints are specified like so:
 * <pre>{@code
 * Group group = new Group(new AbsoluteLayout()).add(
 *     AbsoluteLayout.at(new Label("+50+50"), 50, 50),
 *     AbsoluteLayout.at(new Button("100x50+25+25"), 25, 25, 100, 50)
 * );
 * }</pre>
 */
public class AbsoluteLayout extends Layout
{
    /** Defines absolute layout constraints. */
    public static final class Constraint extends Layout.Constraint {
        public final IPoint position;
        public final IDimension size;
        public final boolean center;

        public Constraint (IPoint position, IDimension size, boolean center) {
            this.position = position;
            this.size = size;
            this.center = center;
        }

        public IDimension psize (AbsoluteLayout layout, Element<?> elem) {
            return size == ZERO ? layout.preferredSize(elem, size.width(), size.height()) : size;
        }

        public IPoint pos (IDimension psize) {
            return center ? position.subtract(psize.width()/2, psize.height()/2) : position;
        }
    }

    /**
     * Positions {@code elem} at the specified position, in its preferred size.
     */
    public static <T extends Element<?>> T at (T elem, float x, float y) {
        return at(elem, new Point(x, y));
    }

    /**
     * Positions {@code elem} at the specified position, in its preferred size.
     */
    public static <T extends Element<?>> T at (T elem, IPoint position) {
        return at(elem, position, ZERO);
    }

    /**
     * Constrains {@code elem} to the specified position and size.
     */
    public static <T extends Element<?>> T at (T elem, float x, float y, float width, float height) {
        return at(elem, new Point(x, y), new Dimension(width, height));
    }

    /**
     * Constrains {@code elem} to the specified position and size.
     */
    public static <T extends Element<?>> T at (T elem, IPoint position, IDimension size) {
        elem.setConstraint(new Constraint(position, size, false));
        return elem;
    }

    /**
     * Centers {@code elem} on the specified position, in its preferred size.
     */
    public static <T extends Element<?>> T centerAt (T elem, float x, float y) {
        return centerAt(elem, new Point(x, y));
    }

    /**
     * Centers {@code elem} on the specified position, in its preferred size.
     */
    public static <T extends Element<?>> T centerAt (T elem, IPoint position) {
        elem.setConstraint(new Constraint(position, ZERO, true));
        return elem;
    }

    @Override public Dimension computeSize (Elements<?> elems, float hintX, float hintY) {
        // report a size large enough to contain all of our elements
        Rectangle bounds = new Rectangle();
        for (Element<?> elem : elems) {
            if (!elem.isVisible()) continue;
            Constraint c = constraint(elem);
            IDimension psize = c.psize(this, elem);
            bounds.add(new Rectangle(c.pos(psize), psize));
        }
        return new Dimension(bounds.width, bounds.height);
    }

    @Override public void layout (Elements<?> elems,
                                  float left, float top, float width, float height) {
        for (Element<?> elem : elems) {
            if (!elem.isVisible()) continue;
            Constraint c = constraint(elem);
            IDimension psize = c.psize(this, elem); // this should return a cached size
            IPoint pos = c.pos(psize);
            setBounds(elem, left + pos.x(), top + pos.y(), psize.width(), psize.height());
        }
    }

    protected static Constraint constraint (Element<?> elem) {
        return (Constraint)Asserts.checkNotNull(
            elem.constraint(), "Elements in AbsoluteLayout must have a constraint.");
    }

    protected static final Dimension ZERO = new Dimension(0, 0);
}
