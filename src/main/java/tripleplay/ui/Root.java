//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Layer;
import playn.core.Pointer;

import pythagoras.f.IDimension;
import pythagoras.f.Point;

/**
 * The root of a display hierarchy. An application can have one or more roots, but they should not
 * overlap and will behave as if oblivious to one another's existence.
 */
public class Root extends Elements<Root>
{
    public interface PointerDelegate {
        /**
         * Called when the pointer event starts. Return true to receive drag and end events and
         * prevent propogation to other Roots.
         */
        boolean handlePointerStart (Pointer.Event event);

        /**
         * Called when the pointer event ends after a start for which the delegate returned true.
         */
        void onPointerEnd (Pointer.Event event);

        /**
         * Called when the pointer drags after a start for which the delegate returned true.
         */
        void onPointerDrag (Pointer.Event event);
    }

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

    /**
     * Sets the delegate to receive pointer events that hit no Elements in this Root. May be null to
     * clear the current delegate.
     */
    public void setPointerDelegate (PointerDelegate delegate) {
        _delegate = delegate;
    }

    protected Root (Interface iface, Layout layout, Stylesheet sheet) {
        super(layout);
        setStylesheet(sheet);
        _iface = iface;
    }

    protected boolean dispatchPointerStart (Pointer.Event event) {
        Point p = new Point(event.x(), event.y());
        _active = hitTest(p);
        if (_active == null) return _delegate == null ? false : _delegate.handlePointerStart(event);
        _active.onPointerStart(p.x, p.y);
        return true;
    }

    protected void dispatchPointerDrag (Pointer.Event event) {
        if (_active != null) {
            Point p = Layer.Util.screenToLayer(_active.layer, event.x(), event.y());
            _active.onPointerDrag(p.x, p.y);
        } else if (_delegate != null) _delegate.onPointerDrag(event);
    }

    protected void dispatchPointerEnd (Pointer.Event event) {
        if (_active != null) {
            Point p = Layer.Util.screenToLayer(_active.layer, event.x(), event.y());
            _active.onPointerEnd(p.x, p.y);
            _active = null;
        } else if (_delegate != null) _delegate.onPointerEnd(event);
    }

    @Override protected void hitToLayer (Point p) {
        Layer.Util.screenToLayer(layer, p, p);
    }

    @Override protected Root root () {
        return this;
    }

    protected final Interface _iface;
    protected boolean _valid;
    protected Element<?> _active;
    protected PointerDelegate _delegate;
}
