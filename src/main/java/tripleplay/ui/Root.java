//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.AffineTransform;
import pythagoras.f.IDimension;
import pythagoras.f.Point;

import tripleplay.util.Coords;

/**
 * The root of a display hierarchy. An application can have one or more roots, but they should not
 * overlap and will behave as if oblivious to one another's existence.
 */
public class Root extends Group
{
    /**
     * Sizes this root element to its preferred size.
     */
    public void pack () {
        IDimension psize = getPreferredSize(0, 0);
        setSize(psize.getWidth(), psize.getHeight());
    }

    /**
     * Sizes this root element to the specified width and its preferred height.
     */
    public void packToWidth (float width) {
        IDimension psize = getPreferredSize(width, 0);
        setSize(width, psize.getHeight());
    }

    /**
     * Sizes this root element to the specified height and its preferred width.
     */
    public void packToHeight (float height) {
        IDimension psize = getPreferredSize(0, height);
        setSize(psize.getWidth(), height);
    }

    /**
     * Sets the size of this root element.
     */
    public void setSize (float width, float height) {
        _size.setSize(width, height);
        invalidate();
    }

    protected Root (Interface iface, Layout layout, Stylesheet sheet) {
        super(layout, sheet);
        _iface = iface;
    }

    protected boolean dispatchPointerStart (float x, float y) {
        Point p = new Point(x, y);
        _active = hitTest(new AffineTransform(), p);
        if (_active == null) return false;
        _active.onPointerStart(p.x, p.y);
        return true;
    }

    protected void dispatchPointerDrag (float x, float y) {
        if (_active != null) {
            Point p = Coords.screenToLayer(_active.layer, x, y, new Point());
            _active.onPointerDrag(p.x, p.y);
        }
    }

    protected void dispatchPointerEnd (float x, float y) {
        if (_active != null) {
            Point p = Coords.screenToLayer(_active.layer, x, y, new Point());
            _active.onPointerEnd(p.x, p.y);
            _active = null;
        }
    }

    @Override protected Root getRoot () {
        return this;
    }

    @Override protected void validate () {
        if (!isSet(Flag.VALID)) {
            super.validate();
        }
    }

    protected final Interface _iface;
    protected boolean _valid;
    protected Element _active;
}
