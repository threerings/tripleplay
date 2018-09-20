//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.layout;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.IPoint;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;
import tripleplay.ui.Container;
import tripleplay.ui.Element;
import tripleplay.ui.Layout;
import tripleplay.ui.Style.HAlign;
import tripleplay.ui.Style.VAlign;
import tripleplay.ui.util.BoxPoint;

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
        public final BoxPoint position;
        public final BoxPoint origin;
        public final IDimension size;

        public Constraint (IPoint position, IDimension size, HAlign halign, VAlign valign) {
            this(BoxPoint.TL.offset(position.x(), position.y()),
                BoxPoint.TL.align(halign, valign), size);
        }

        public Constraint (BoxPoint position, BoxPoint origin, IDimension size) {
            this.position = position;
            this.origin = origin;
            this.size = size;
        }

        public IDimension psize (AbsoluteLayout layout, Element<?> elem) {
            float fwidth = size.width(), fheight = size.height();
            if (fwidth > 0 && fheight > 0) return size;
            // if either forced width or height is zero, use preferred size in that dimension
            IDimension psize = layout.preferredSize(elem, fwidth, fheight);
            if (fwidth > 0) return new Dimension(fwidth, psize.height());
            else if (fheight > 0) return new Dimension(psize.width(), fheight);
            else return psize;
        }

        public IPoint pos (float width, float height, IDimension prefSize) {
            Point position = this.position.resolve(0, 0, width, height, new Point());
            Point origin = this.origin.resolve(prefSize, new Point());
            position.x -= origin.x;
            position.y -= origin.y;
            return position;
        }
    }

    /**
     * Creates a constraint to position an element uniformly. The given box point is used for both
     * the position and the origin. For example, if {@code BoxPoint.BR} is used, then the element
     * will be positioned such that its bottom right corner is over the bottom right corner of
     * the group.
     */
    public static Constraint uniform (BoxPoint where) {
        return new Constraint(where, where, ZERO);
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
    public static <T extends Element<?>> T at (T elem, float x, float y,
                                               HAlign halign, VAlign valign) {
        return at(elem, new Point(x, y), ZERO, halign, valign);
    }

    /**
     * Positions {@code elem} relative to the given position using the given alignments.
     */
    public static <T extends Element<?>> T at (T elem, IPoint position,
                                               HAlign halign, VAlign valign) {
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

    @Override public Dimension computeSize (Container<?> elems, float hintX, float hintY) {
        // report a size large enough to contain all of our elements
        Rectangle bounds = new Rectangle();
        for (Element<?> elem : elems) {
            if (!elem.isVisible()) continue;
            Constraint c = constraint(elem);
            IDimension psize = c.psize(this, elem);
            bounds.add(new Rectangle(c.pos(0, 0, psize), psize));
        }
        return new Dimension(bounds.width, bounds.height);
    }

    @Override public void layout (Container<?> elems,
                                  float left, float top, float width, float height) {
        for (Element<?> elem : elems) {
            if (!elem.isVisible()) continue;
            Constraint c = constraint(elem);
            IDimension psize = c.psize(this, elem); // this should return a cached size
            IPoint pos = c.pos(width, height, psize);
            setBounds(elem, left + pos.x(), top + pos.y(), psize.width(), psize.height());
        }
    }

    protected static Constraint constraint (Element<?> elem) {
        assert elem.constraint() != null : "Elements in AbsoluteLayout must have a constraint.";
        return (Constraint)elem.constraint();
    }

    protected static final Dimension ZERO = new Dimension(0, 0);
}
