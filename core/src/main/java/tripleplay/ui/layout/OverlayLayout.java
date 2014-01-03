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

        public IDimension psize (OverlayLayout layout, Element<?> elem, float availX,
            float availY) {
            float fwidth = size.width(), fheight = size.height();
            if (hstretch) fwidth = availX;
            if (vstretch) fheight = availY;
            if (fwidth > 0 && fheight > 0) return new Dimension(fwidth, fheight);
            // if either forced width or height is zero, use preferred size in that dimension
            IDimension psize = layout.preferredSize(elem, fwidth, fheight);
            if (fwidth > 0) return new Dimension(fwidth, psize.height());
            else if (fheight > 0) return new Dimension(psize.width(), fheight);
            else return psize;
        }

        public IPoint pos (IDimension psize, float availX, float availY) {
            return new Point(
                halign.offset(psize.width(), availX),
                valign.offset(psize.height(), availY));
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
     * size.
     */
    public static <T extends Element<?>> T at (T elem, float width, float height, HAlign halign,
        VAlign valign) {
        return at(elem, new Dimension(width, height), false, false, halign, valign);
    }

    /**
     * Constrains {@code elem} to the specified alignment relative to the parent and with specified
     * size.
     */
    public static <T extends Element<?>> T at (T elem, IDimension size, HAlign halign,
        VAlign valign) {
        return at(elem, size, false, false, halign, valign);
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
        return at(elem, size, hstretch, vstretch, HAlign.CENTER, VAlign.CENTER);
    }

    /**
     * Centers vertically and aligns to the left {@code elem} in the parent.
     */
    public static <T extends Element<?>> T centerLeft (T elem) {
        return at(elem, ZERO, false, false, HAlign.LEFT, VAlign.CENTER);
    }

    /**
     * Centers vertically and aligns to the left {@code elem} in the parent with specified
     * element size.
     */
    public static <T extends Element<?>> T centerLeft (T elem, float width, float height) {
        return at(elem, new Dimension(width, height), false, false, HAlign.LEFT, VAlign.CENTER);
    }

    /**
     * Centers vertically and aligns to the left {@code elem} in the parent with specified
     * element size.
     */
    public static <T extends Element<?>> T centerLeft (T elem, IDimension size) {
        return at(elem, size, false, false, HAlign.LEFT, VAlign.CENTER);
    }

    /**
     * Centers vertically and aligns to the right {@code elem} in the parent.
     */
    public static <T extends Element<?>> T centerRight (T elem) {
        return at(elem, ZERO, false, false, HAlign.RIGHT, VAlign.CENTER);
    }

    /**
     * Centers vertically and aligns to the right {@code elem} in the parent with specified
     * element size.
     */
    public static <T extends Element<?>> T centerRight (T elem, float width, float height) {
        return at(elem, new Dimension(width, height), false, false, HAlign.RIGHT, VAlign.CENTER);
    }

    /**
     * Centers vertically and aligns to the right {@code elem} in the parent with specified
     * element size.
     */
    public static <T extends Element<?>> T centerRight (T elem, IDimension size) {
        return at(elem, size, false, false, HAlign.RIGHT, VAlign.CENTER);
    }

    /**
     * Centers horizontally and aligns to the top {@code elem} in the parent.
     */
    public static <T extends Element<?>> T centerTop (T elem) {
        return at(elem, ZERO, false, false, HAlign.CENTER, VAlign.TOP);
    }

    /**
     * Centers horizontally and aligns to the top {@code elem} in the parent with specified
     * element size.
     */
    public static <T extends Element<?>> T centerTop (T elem, float width, float height) {
        return at(elem, new Dimension(width, height), false, false, HAlign.CENTER, VAlign.TOP);
    }

    /**
     * Centers horizontally and aligns to the top {@code elem} in the parent with specified
     * element size.
     */
    public static <T extends Element<?>> T centerTop (T elem, IDimension size) {
        return at(elem, size, false, false, HAlign.CENTER, VAlign.TOP);
    }

    /**
     * Centers horizontally and aligns to the bottom {@code elem} in the parent.
     */
    public static <T extends Element<?>> T centerBottom (T elem) {
        return at(elem, ZERO, false, false, HAlign.CENTER, VAlign.BOTTOM);
    }

    /**
     * Centers horizontally and aligns to the bottom {@code elem} in the parent with specified
     * element size.
     */
    public static <T extends Element<?>> T centerBottom (T elem, float width, float height) {
        return at(elem, new Dimension(width, height), false, false, HAlign.CENTER, VAlign.BOTTOM);
    }

    /**
     * Centers horizontally and aligns to the bottom {@code elem} in the parent with specified
     * element size.
     */
    public static <T extends Element<?>> T centerBottom (T elem, IDimension size) {
        return at(elem, size, false, false, HAlign.CENTER, VAlign.BOTTOM);
    }

    /**
     * Stretches {@code elem} in the parent.
     */
    public static <T extends Element<?>> T stretch (T elem) {
        return at(elem, ZERO, true, true, HAlign.CENTER, VAlign.CENTER);
    }

    /**
     * Stretches and aligns to the right the {@code elem} in the parent.
     */
    public static <T extends Element<?>> T stretchRight (T elem) {
        return at(elem, ZERO, false, true, HAlign.RIGHT, VAlign.CENTER);
    }

    /**
     * Stretches and aligns to the left the {@code elem} in the parent.
     */
    public static <T extends Element<?>> T stretchLeft (T elem) {
        return at(elem, ZERO, false, true, HAlign.LEFT, VAlign.CENTER);
    }

    /**
     * Stretches and aligns to the top the {@code elem} in the parent.
     */
    public static <T extends Element<?>> T stretchTop (T elem) {
        return at(elem, ZERO, true, false, HAlign.CENTER, VAlign.TOP);
    }

    /**
     * Stretches and aligns to the bottom the {@code elem} in the parent.
     */
    public static <T extends Element<?>> T stretchBottom (T elem) {
        return at(elem, ZERO, true, false, HAlign.CENTER, VAlign.BOTTOM);
    }

    /**
     * Aligns {@code elem} to the top-left corner in the parent.
     */
    public static <T extends Element<?>> T topLeft (T elem) {
        return at(elem, ZERO, false, false, HAlign.LEFT, VAlign.TOP);
    }

    /**
     * Aligns {@code elem} to the top-left corner in the parent with specified element size.
     */
    public static <T extends Element<?>> T topLeft (T elem, float width, float height) {
        return at(elem, new Dimension(width, height), false, false, HAlign.LEFT, VAlign.TOP);
    }

    /**
     * Aligns {@code elem} to the top-left corner in the parent with specified element size.
     */
    public static <T extends Element<?>> T topLeft (T elem, IDimension size) {
        return at(elem, size, false, false, HAlign.LEFT, VAlign.TOP);
    }

    /**
     * Aligns {@code elem} to the top-right corner in the parent.
     */
    public static <T extends Element<?>> T topRight (T elem) {
        return at(elem, ZERO, false, false, HAlign.RIGHT, VAlign.TOP);
    }

    /**
     * Aligns {@code elem} to the top-right corner in the parent with specified element size.
     */
    public static <T extends Element<?>> T topRight (T elem, float width, float height) {
        return at(elem, new Dimension(width, height), false, false, HAlign.RIGHT, VAlign.TOP);
    }

    /**
     * Aligns {@code elem} to the top-right corner in the parent with specified element size.
     */
    public static <T extends Element<?>> T topRight (T elem, IDimension size) {
        return at(elem, size, false, false, HAlign.RIGHT, VAlign.TOP);
    }

    /**
     * Aligns {@code elem} to the bottom-left corner in the parent.
     */
    public static <T extends Element<?>> T bottomLeft (T elem) {
        return at(elem, ZERO, false, false, HAlign.LEFT, VAlign.BOTTOM);
    }

    /**
     * Aligns {@code elem} to the bottom-left corner in the parent with specified element size.
     */
    public static <T extends Element<?>> T bottomLeft (T elem, float width, float height) {
        return at(elem, new Dimension(width, height), false, false, HAlign.LEFT, VAlign.BOTTOM);
    }

    /**
     * Aligns {@code elem} to the bottom-left corner in the parent with specified element size.
     */
    public static <T extends Element<?>> T bottomLeft (T elem, IDimension size) {
        return at(elem, size, false, false, HAlign.LEFT, VAlign.BOTTOM);
    }

    /**
     * Aligns {@code elem} to the bottom-right corner in the parent.
     */
    public static <T extends Element<?>> T bottomRight (T elem) {
        return at(elem, ZERO, false, false, HAlign.RIGHT, VAlign.BOTTOM);
    }

    /**
     * Aligns {@code elem} to the bottom-right corner in the parent with specified element size.
     */
    public static <T extends Element<?>> T bottomRight (T elem, float width, float height) {
        return at(elem, new Dimension(width, height), false, false, HAlign.RIGHT, VAlign.BOTTOM);
    }

    /**
     * Aligns {@code elem} to the bottom-right corner in the parent with specified element size.
     */
    public static <T extends Element<?>> T bottomRight (T elem, IDimension size) {
        return at(elem, size, false, false, HAlign.RIGHT, VAlign.BOTTOM);
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
        // available size without paddings
        float availX = hintX - _leftPadding - _rightPadding;
        float availY = hintY - _topPadding - _bottomPadding;

        // report a size large enough to contain all of our elements
        Rectangle bounds = new Rectangle();
        for (Element<?> elem : elems) {
            if (!elem.isVisible()) continue;
            Constraint c = constraint(elem);
            IDimension psize = c.psize(this, elem, availX, availY);
            bounds.add(new Rectangle(c.pos(psize, availX, availY), psize));
        }
        return new Dimension(bounds.width, bounds.height);
    }

    @Override public void layout (Container<?> elems, float left, float top, float width,
        float height) {
        // available size without paddings
        float availX = width - _leftPadding - _rightPadding;
        float availY = height - _topPadding - _bottomPadding;

        for (Element<?> elem : elems) {
            if (!elem.isVisible()) continue;
            Constraint c = constraint(elem);
            IDimension psize = c.psize(this, elem, availX, availY); // this should return a cached size
            IPoint pos = c.pos(psize, availX, availY);
            setBounds(elem, _leftPadding + left + pos.x(), _topPadding + top + pos.y(),
                psize.width(), psize.height());
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
