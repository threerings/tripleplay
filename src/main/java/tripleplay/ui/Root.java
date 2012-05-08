//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.IDimension;
import pythagoras.f.Point;

import playn.core.Layer;
import playn.core.PlayN;

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
        setLocation(x, y);
        return this;
    }

    protected Root (Interface iface, Layout layout, Stylesheet sheet) {
        super(layout);
        setStylesheet(sheet);
        _iface = iface;

        if (absorbsClicks()) {
            layer.setHitTester(new Layer.HitTester() {
                public Layer hitTest (Layer layer, Point p) {
                    if (!isVisible() || !contains(p.x, p.y)) return null;
                    // if this click doesn't hit anything else, it hits us
                    Layer hit = layer.hitTestDefault(p);
                    return (hit != null) ? hit : Root.this.layer;
                }
            });
        }
    }

    @Override protected Root root () {
        return this;
    }

    /**
     * By default, all clicks that fall within a root's bounds are dispatched to the root's layer
     * if they do not land on an interactive child element. This prevents clicks from "falling
     * through" to lower roots, which are visually obscured by this root. Override this method and
     * return false if you want this root not to absorb clicks (if it's "transparent").
     */
    protected boolean absorbsClicks () {
        return true;
    }

    protected final Interface _iface;
    protected boolean _valid;
    protected Element<?> _active;
}
