//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.layout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.Rectangle;
import tripleplay.ui.Element;
import tripleplay.ui.Elements;
import tripleplay.ui.Layout;
import tripleplay.ui.Style;

/**
 * Arranges up to 5 elements, one central and one on each edge. Added elements must have a
 * constraint from the class' listing (e.g. {@link BorderLayout#CENTER}), which determines the
 * position in the layout and stretching.
 * <p>This is how the layout looks. Note north/south and east/west behavior is not quite symmetric
 * because east and west fit between the bottom of the north and top of the south:</p>
 * <p><pre>
 *     |-----------------------------|
 *     |            north            |
 *     |-----------------------------|
 *     |      |               |      |
 *     |      |               |      |
 *     |      |               |      |
 *     | west |    center     | east |
 *     |      |               |      |
 *     |      |               |      |
 *     |-----------------------------|
 *     |            south            |
 *     |-----------------------------|
 * </pre></p>
 * When an element is not stretched, it obeys the {@link Style.HAlign} and {@link Style.VAlign}
 * bindings.
 */
public class BorderLayout extends Layout
{
    /** Constraint to position an element in the center of its parent. The element is stretched
     * in both directions to take up available space. If {@link Constraint#unstretched()} is
     * used, the element will be aligned in both directions using its preferred size and the
     * {@link Style.HAlign} and {@link Style.VAlign} bindings. */
    public static final Constraint CENTER = Position.CENTER.stretched;

    /** Constraint to position an element along the top edge of its parent. The element is
     * stretched horizontally and uses its preferred height. If {@link Constraint#unstretched()} is
     * used, the element will be aligned horizontally using its preferred size according to the
     * {@link Style.HAlign} binding. */
    public static final Constraint NORTH = Position.NORTH.stretched;

    /** Constraint to position an element along the bottom edge of its parent. The element is
     * stretched horizontally and uses its preferred height. If {@link Constraint#unstretched()} is
     * used, the element will be aligned horizontally using its preferred size according to the
     * {@link Style.HAlign} binding. */
    public static final Constraint SOUTH = Position.SOUTH.stretched;

    /** Constraint to position an element along the right edge of its parent. The element is
     * stretched vertically and uses its preferred width. If {@link Constraint#unstretched()} is
     * used, the element will be aligned vertically using its preferred size according to the
     * {@link Style.VAlign} binding. */
    public static final Constraint EAST = Position.EAST.stretched;

    /** Constraint to position an element along the right edge of its parent. The element is
     * stretched vertically and uses its preferred width. If {@link Constraint#unstretched()} is
     * used, the element will be aligned vertically using its preferred size according to the
     * {@link Style.VAlign} binding. */
    public static final Constraint WEST = Position.WEST.stretched;

    /**
     * Implements the constraints. Callers do not need to construct instances, but instead use the
     * declared constants and select or deselect the stretching option.
     */
    public static class Constraint extends Layout.Constraint {

        /**
         * Returns a new constraint specifying the same position as this, and with stretching.
         */
        public Constraint stretched () {
            return _pos.stretched;
        }

        /**
         * Returns a new constraint specifying the same position as this, and with no stretching.
         * The element's preferred size will be used and an appropriate alignment.
         */
        public Constraint unstretched () {
            return _pos.unstretched;
        }

        protected Constraint (Position pos, boolean stretch) {
            _pos = pos;
            _stretch = stretch;
        }

        protected Dimension adjust (IDimension pref, Rectangle boundary) {
            Dimension dim = new Dimension(pref);
            if (_stretch) {
                if ((_pos.orient & 1) != 0) {
                    dim.width = boundary.width;
                }
                if ((_pos.orient & 2) != 0) {
                    dim.height = boundary.height;
                }
            }
            dim.width = Math.min(dim.width, boundary.width);
            dim.height = Math.min(dim.height, boundary.height);
            return dim;
        }

        protected float align (float origin, float offset) {
            return _stretch ? origin : origin + offset;
        }

        protected final Position _pos;
        protected final boolean _stretch;
    }

    /** The horizontal gap between components. */
    public final float hgap;

    /** The vertical gap between components. */
    public final float vgap;

    /**
     * Constructs a new border layout with no gaps.
     */
    public BorderLayout () {
        this(0);
    }

    /**
     * Constructs a new border layout with the specified gap between components.
     */
    public BorderLayout (float gaps) {
        this(gaps, gaps);
    }

    /**
     * Constructs a new border layout with the specified horizontal and vertical gaps between
     * components.
     */
    public BorderLayout (float hgap, float vgap) {
        this.hgap = hgap;
        this.vgap = vgap;
    }

    @Override
    public Dimension computeSize (Elements<?> elems, float hintX, float hintY) {
        return new Slots(elems).computeSize(hintX, hintY);
    }

    @Override
    public void layout (Elements<?> elems, float left, float top, float width, float height) {
        Style.HAlign halign = resolveStyle(elems, Style.HALIGN);
        Style.VAlign valign = resolveStyle(elems, Style.VALIGN);
        Slots slots = new Slots(elems);
        Rectangle bounds = new Rectangle(left, top, width, height);
        slots.layoutNs(Position.NORTH, halign, bounds);
        slots.layoutNs(Position.SOUTH, halign, bounds);
        slots.layoutWe(Position.WEST, valign, bounds);
        slots.layoutWe(Position.EAST, valign, bounds);

        Position p = Position.CENTER;
        IDimension dim = slots.size(p, bounds.width, bounds.height);
        if (dim == null) {
            return;
        }

        Constraint c = slots.constraint(p);
        dim = c.adjust(dim, bounds);
        slots.setBounds(p,
            c.align(bounds.x, halign.offset(dim.width(), bounds.width)),
            c.align(bounds.y, valign.offset(dim.height(), bounds.height)), dim);
    }

    protected class Slots
    {
        final Map<Position, Element<?>> elements = new HashMap<Position, Element<?>>();

        Slots (Elements<?> elems) {
            for (Element<?> elem : elems) {
                Position p = Position.positionOf(elem.constraint());
                if (p == null) {
                    throw new IllegalStateException("Element with a non-BorderLayout constraint");
                }
                if (elements.put(p, elem) != null) {
                    throw new IllegalStateException(
                        "Multiple elements with the same constraint: " + p);
                }
            }

            // remove invisibles
            for (Iterator<Element<?>> it = elements.values().iterator(); it.hasNext(); ) {
                if (!it.next().isVisible()) {
                    it.remove();
                }
            }
        }

        Dimension computeSize (float hintX, float hintY) {
            int wce = count(WCE);
            Dimension nsSize = new Dimension();
            for (Position pos : NS) {
                IDimension dim = size(pos, hintX, 0);
                if (dim == null) {
                    continue;
                }
                nsSize.height += dim.height();
                nsSize.width = Math.max(nsSize.width, dim.width());
                if (wce > 0) {
                    nsSize.height += vgap;
                }
            }

            float ehintY = Math.max(0, hintY - nsSize.height);
            Dimension weSize = new Dimension();
            for (Position pos : WE) {
                IDimension dim = size(pos, 0, ehintY);
                if (dim == null) {
                    continue;
                }
                weSize.width += dim.width();
                weSize.height = Math.max(weSize.height, dim.height());
            }

            weSize.width += Math.max(wce - 1, 0) * hgap;
            float ehintX = Math.max(0, hintX - weSize.width);

            IDimension csize = size(Position.CENTER, ehintX, ehintY);
            if (csize != null) {
                weSize.width += csize.width();
                nsSize.height += csize.height();
            }
            return new Dimension(
                Math.max(weSize.width, nsSize.width),
                Math.max(weSize.height, nsSize.height));
        }

        void layoutNs (Position p, Style.HAlign halign, Rectangle bounds) {
            IDimension dim = size(p, bounds.width, 0);
            if (dim == null) {
                return;
            }
            Constraint c = constraint(p);
            dim = c.adjust(dim, bounds);
            float y = bounds.y;
            if (p == Position.NORTH) {
                bounds.y += dim.height() + vgap;
            } else {
                y += bounds.height - dim.height();
            }
            bounds.height -= dim.height() + vgap;
            setBounds(p, c.align(bounds.x, halign.offset(dim.width(), bounds.width)), y, dim);
        }

        void layoutWe (Position p, Style.VAlign valign, Rectangle bounds) {
            IDimension dim = size(p, 0, bounds.height);
            if (dim == null) {
                return;
            }
            Constraint c = constraint(p);
            dim = c.adjust(dim, bounds);
            float x = bounds.x;
            if (p == Position.WEST) {
                bounds.x += dim.width() + hgap;
            } else {
                x += bounds.width - dim.width();
            }
            bounds.width -= dim.width() + hgap;
            setBounds(p, x, c.align(bounds.y, valign.offset(dim.height(), bounds.height)), dim);
        }

        void setBounds (Position p, float x, float y, IDimension dim) {
            BorderLayout.this.setBounds(get(p), x, y, dim.width(), dim.height());
        }

        int count (Position ...ps) {
            int count = 0;
            for (Position p : ps) {
                if (elements.containsKey(p)) {
                    count++;
                }
            }
            return count;
        }

        boolean stretch (Position p) {
            return ((Constraint)get(p).constraint())._stretch;
        }

        Element<?> get (Position p) {
            return elements.get(p);
        }

        Constraint constraint (Position p) {
            return (Constraint)get(p).constraint();
        }

        IDimension size (Position p, float hintX, float hintY) {
            Element<?> e = elements.get(p);
            return e == null ? null : preferredSize(e, hintX, hintY);
        }
    }

    protected static enum Position
    {
        CENTER(3), NORTH(1), SOUTH(1), EAST(2), WEST(2);

        static Position positionOf (Layout.Constraint c) {
            for (Position p : values()) {
                if (p.unstretched == c || p.stretched == c) {
                    return p;
                }
            }
            return null;
        }

        final Constraint unstretched;
        final Constraint stretched;
        final int orient;

        Position (int orient) {
            this.orient = orient;
            unstretched = new Constraint(this, false);
            stretched = new Constraint(this, true);
        }
    }

    protected static final Position[] NS = {Position.NORTH, Position.SOUTH};
    protected static final Position[] WE = {Position.WEST, Position.EAST};
    protected static final Position[] WCE = {Position.WEST, Position.CENTER, Position.EAST};
}
