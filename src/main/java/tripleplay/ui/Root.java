//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.IDimension;
import pythagoras.f.Point;

import tripleplay.util.Coords;

/**
 * The root of a display hierarchy. An application can have one or more roots, but they should not
 * overlap and will behave as if oblivious to one another's existence.
 */
public class Root extends Elements<Root>
{
    /**
     * Sizes this root element to its preferred size.
     */
    public Root pack () {
        IDimension psize = preferredSize(0, 0);
        setSize(psize.width(), psize.height());
        return this;
    }

    /**
     * Sizes this root element to the specified width and its preferred height.
     */
    public Root packToWidth (float width) {
        IDimension psize = preferredSize(width, 0);
        setSize(width, psize.height());
        return this;
    }

    /**
     * Sizes this root element to the specified height and its preferred width.
     */
    public Root packToHeight (float height) {
        IDimension psize = preferredSize(0, height);
        setSize(psize.width(), height);
        return this;
    }

    /**
     * Sets the size of this root element.
     */
    public Root setSize (float width, float height) {
        _size.setSize(width, height);
        invalidate();
        return this;
    }

    /**
     * Sets the size of this root element and its translation from its parent.
     */
    public Root setBounds (float x, float y, float width, float height) {
        setSize(width, height);
        layer.setTranslation(x, y);
        return this;
    }

    protected Root (Interface iface, Layout layout, Stylesheet sheet) {
        super(layout);
        setStylesheet(sheet);
        _iface = iface;
    }

    protected boolean dispatchPointerStart (float x, float y) {
        Point p = new Point(x, y);
        _active = hitTest(p);
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

    @Override protected Root root () {
        return this;
    }

    protected final Interface _iface;
    protected boolean _valid;
    protected Element<?> _active;
}
