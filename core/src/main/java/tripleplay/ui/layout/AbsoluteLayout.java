//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
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
import tripleplay.ui.Style.HAlign;
import tripleplay.ui.Style.VAlign;

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
        public final HAlign halign;
        public final VAlign valign;

        public Constraint (IPoint position, IDimension size, HAlign halign, VAlign valign) {
            this.position = position;
            this.size = size;
            this.halign = halign;
            this.valign = valign;
        }

        public IDimension psize (AbsoluteLayout layout, Element<?> elem) {
            float fwidth = size.width(), fheight = size.height();
            if (fwidth > 0 && fheight > 0) return size;
            // if eiher forced width or height is zero, use preferred size in that dimension
            IDimension psize = layout.preferredSize(elem, fwidth, fheight);
            if (fwidth > 0) return new Dimension(fwidth, psize.height());
            else if (fheight > 0) return new Dimension(psize.width(), fheight);
            else return psize;
        }

        public IPoint pos (IDimension psize) {
            return new Point(
                position.x() + halign.offset(psize.width(), 0),
                position.y() + valign.offset(psize.height(), 0));
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
        elem.setConstraint(new Constraint(position, size, HAlign.LEFT, VAlign.TOP));
        return elem;
    }

    /**
     * Positions {@code elem} relative to the given position using the given alignments.
     */
    public static <T extends Element<?>> T at (T elem, float x, float y, HAlign halign, VAlign valign) {
        return at(elem, new Point(x, y), ZERO, halign, valign);
    }

    /**
     * Positions {@code elem} relative to the given position using the given alignments.
     */
    public static <T extends Element<?>> T at (T elem, IPoint position, HAlign halign, VAlign valign) {
        return at(elem, position, ZERO, halign, valign);
    }

    /**
     * Constrains {@code elem} to the specified size and aligns it relative to the given position
     * using the given alignments.
     */
    public static <T extends Element<?>> T at (T elem, float x, float y, float width, float height,
                                               HAlign halign, VAlign valign) {
        return at(elem, new Point(x, y), new Dimension(width, height), halign, valign);
    }

    /**
     * Constrains {@code elem} to the specified size and aligns it relative to the given position
     * using the given alignments.
     */
    public static <T extends Element<?>> T at (T elem, IPoint position, IDimension size,
                                               HAlign halign, VAlign valign) {
        elem.setConstraint(new Constraint(position, size, halign, valign));
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
        elem.setConstraint(new Constraint(position, ZERO, HAlign.CENTER, VAlign.CENTER));
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
