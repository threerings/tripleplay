package tripleplay.ui.layout;

import playn.core.Asserts;

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

/**
 * A layout that positions elements at above each other with different stretching and alignment
 * constraints. Constraints are specified like so:
 * <pre>{@code
 * Group group = new Group(new OverlayLayout()).add(
 *     OverlayLayout.at(new Label("+50+50"), 50, 50),
 *     OverlayLayout.at(new Button("100x50+25+25"), 25, 25, 100, 50)
 * );
 * }</pre>
 */
public class OverlayLayout extends Layout
{
    /**
     * Defines overlay layout constraints.
     */
    public static final class Constraint extends Layout.Constraint
    {
        public final IDimension size;
        public final boolean hstretch;
        public final boolean vstretch;
        public final HAlign halign;
        public final VAlign valign;

        public Constraint (IDimension size, boolean hstretch, boolean vstretch, HAlign halign,
            VAlign valign) {
            this.size = size;
            this.hstretch = hstretch;
            this.vstretch = vstretch;
            this.halign = halign;
            this.valign = valign;
        }

        public IDimension psize (OverlayLayout layout, Element<?> elem) {
            float fwidth = size.width(), fheight = size.height();
            if (fwidth > 0 && fheight > 0) { return size; }
            // if either forced width or height is zero, use preferred size in that dimension
            IDimension psize = layout.preferredSize(elem, fwidth, fheight);
            if (fwidth > 0) return new Dimension(fwidth, psize.height());
            else if (fheight > 0) return new Dimension(psize.width(), fheight);
            else return psize;
        }

        public IPoint pos (IDimension psize) {
            return new Point(
                halign.offset(psize.width(), 0),
                valign.offset(psize.height(), 0));
        }
    }

    /**
     * Positions {@code elem} to the specified alignment relative to the parent.
     */
    public static <T extends Element<?>> T at (T elem, HAlign halign, VAlign valign) {
        return at(elem, ZERO, false, false, halign, valign);
    }

    /**
     * Positions {@code elem} to specified alignment relative to the parent and with specified
     * horizontal and vertical stretching.
     */
    public static <T extends Element<?>> T at (T elem, boolean hstretch, boolean vstretch,
        HAlign halign, VAlign valign) {
        return at(elem, ZERO, hstretch, vstretch, halign, valign);
    }

    /**
     * Constrains {@code elem} to the specified alignment relative to the parent and with specified
     * horizontal and vertical stretching and element size.
     */
    public static <T extends Element<?>> T at (T elem, float width, float height, boolean hstretch,
        boolean vstretch, HAlign halign, VAlign valign) {
        return at(elem, new Dimension(width, height), hstretch, vstretch, halign, valign);
    }

    /**
     * Constrains {@code elem} to the specified alignment relative to the parent and with specified
     * horizontal and vertical stretching and element size.
     */
    public static <T extends Element<?>> T at (T elem, IDimension size, boolean hstretch,
        boolean vstretch, HAlign halign, VAlign valign) {
        elem.setConstraint(new Constraint(size, hstretch, vstretch, halign, valign));
        return elem;
    }

    /**
     * Centers {@code elem} in the parent.
     */
    public static <T extends Element<?>> T center (T elem) {
        return center(elem, false, false);
    }

    /**
     * Centers {@code elem} in the parent with specified horizontal and vertical stretching.
     */
    public static <T extends Element<?>> T center (T elem, boolean hstretch, boolean vstretch) {
        return center(elem, ZERO, hstretch, vstretch);
    }

    /**
     * Centers {@code elem} in the parent with specified horizontal and vertical stretching and
     * element size.
     */
    public static <T extends Element<?>> T center (T elem, float width, float height,
        boolean hstretch, boolean vstretch) {
        return center(elem, new Dimension(width, height), hstretch, vstretch);
    }

    /**
     * Centers {@code elem} in the parent with specified horizontal and vertical stretching and
     * element size.
     */
    public static <T extends Element<?>> T center (T elem, IDimension size, boolean hstretch,
        boolean vstretch) {
        elem.setConstraint(new Constraint(size, hstretch, vstretch, HAlign.CENTER, VAlign.CENTER));
        return elem;
    }

    /**
     * Stretches {@code elem} in the parent.
     */
    public static <T extends Element<?>> T stretched (T elem) {
        elem.setConstraint(new Constraint(ZERO, true, true, HAlign.CENTER, VAlign.CENTER));
        return elem;
    }

    /**
     * Configures the inter-element padding, in pixels.
     */
    public OverlayLayout padding (float padding) {
        _topPadding = padding;
        _rightPadding = padding;
        _bottomPadding = padding;
        _leftPadding = padding;
        return this;
    }

    /**
     * Configures the inter-element horizontal and vertical margins, in pixels.
     */
    public OverlayLayout padding (float hpadding, float vpadding) {
        _topPadding = vpadding;
        _rightPadding = hpadding;
        _bottomPadding = vpadding;
        _leftPadding = hpadding;
        return this;
    }

    /**
     * Configures the inter-element margins, in pixels.
     */
    public OverlayLayout padding (float top, float right, float bottom, float left) {
        _topPadding = top;
        _rightPadding = right;
        _bottomPadding = bottom;
        _leftPadding = left;
        return this;
    }

    @Override public Dimension computeSize (Container<?> elems, float hintX, float hintY) {
        // report a size large enough to contain all of our elements
        Rectangle bounds = new Rectangle();
        for (Element<?> elem : elems) {
            if (!elem.isVisible()) { continue; }
            Constraint c = constraint(elem);
            IDimension psize = c.psize(this, elem);
            bounds.add(new Rectangle(c.pos(psize), psize));
        }
        return new Dimension(bounds.width, bounds.height);
    }

    @Override public void layout (Container<?> elems, float left, float top, float width,
        float height) {
        for (Element<?> elem : elems) {
            if (!elem.isVisible()) { continue; }
            Constraint c = constraint(elem);
            IDimension psize = c.psize(this, elem); // this should return a cached size
            IPoint pos = c.pos(psize);
            setBounds(elem, left + pos.x(), top + pos.y(), psize.width(), psize.height());
        }
    }

    protected static Constraint constraint (Element<?> elem) {
        return (Constraint) Asserts.checkNotNull(
            elem.constraint(), "Elements in OverlayLayout must have a constraint.");
    }

    protected float _topPadding = 0.0f;
    protected float _rightPadding = 0.0f;
    protected float _bottomPadding = 0.0f;
    protected float _leftPadding = 0.0f;

    protected static final Dimension ZERO = new Dimension(0, 0);
}
