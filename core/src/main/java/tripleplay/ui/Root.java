//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.IDimension;
import react.Signal;
import react.SignalView;
import tripleplay.util.Destroyable;

/**
 * The root of a display hierarchy. An application can have one or more roots, but they should not
 * overlap and will behave as if oblivious to one another's existence.
 */
public class Root extends Elements<Root>
    implements Destroyable
{
    /**
     * Creates a new root with the provided layout and stylesheet.
     */
    public Root (Interface iface, Layout layout, Stylesheet sheet) {
        super(layout);
        _iface = iface;
        setStylesheet(sheet);

        set(Flag.HIT_ABSORB, true);
    }

    /**
     * Returns the interface to which this root belongs.
     */
    public Interface iface () {
        return _iface;
    }

    /**
     * A signal emitted when this root is validated.
     */
    public SignalView<Root> validated () {
        return _validated;
    }

    @Override public boolean isShowing () {
        return isVisible();
    }

    /**
     * Sizes this root element to its preferred size.
     */
    public Root pack () {
        return pack(0, 0);
    }

    /**
     * Sizes this element to its preferred size, computed using the supplied hints.
     */
    public Root pack (float widthHint, float heightHint) {
        IDimension psize = preferredSize(widthHint, heightHint);
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
    @Override public Root setSize (float width, float height) {
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

    /** See {@link Interface#destroyRoot}. */
    @Override public void destroy () {
        _iface.destroyRoot(this);
    }

    /**
     * Computes the preferred size of this root. In general, one should use {@link #pack} or one of
     * the related pack methods, but if one has special sizing requirements, they may wish to call
     * {@code preferredSize} directly, followed by {@link #setSize}.
     *
     * @param hintX the width hint (a width in which the layout will attempt to fit itself), or 0
     * to allow the layout to use unlimited width.
     * @param hintY the height hint (a height in which the layout will attempt to fit itself), or 0
     * to allow the layout to use unlimited height.
     */
    @Override public IDimension preferredSize (float hintX, float hintY) {
        return super.preferredSize(hintX, hintY);
    }

    /**
     * Applies the root size to all descendants. The size normally comes from a call to
     * {@link #pack()} or a related method. Validation is performed automatically by
     * {@link Interface#paint(float)} if the root is created via {@link Interface}.
     */
    @Override public void validate () {
        super.validate();
    }

    /**
     * By default, all clicks that fall within a root's bounds are dispatched to the root's layer
     * if they do not land on an interactive child element. This prevents clicks from "falling
     * through" to lower roots, which are visually obscured by this root. Call this method with
     * false if you want this root not to absorb clicks (if it's "transparent").
     */
    public Root setAbsorbsClicks (boolean absorbsClicks) {
        set(Flag.HIT_ABSORB, absorbsClicks);
        return this;
    }

    /**
     * Sets this Root's menu host, allowing an application to more manage multiple roots with
     * a single menu host.
     */
    public void setMenuHost (MenuHost host) {
        if (_menuHost != null) {
            _menuHost.deactivate();
        }
        _menuHost = host;
    }

    /**
     * Gets this Root's menu host, creating it if necessary.
     */
    public MenuHost getMenuHost () {
        if (_menuHost == null) {
            _menuHost = new MenuHost(_iface, this);
        }
        return _menuHost;
    }

    @Override protected Class<?> getStyleClass () {
        return Root.class;
    }

    @Override protected Root root () {
        return this;
    }

    @Override protected void layout () {
        super.layout();
        _validated.emit(this);
    }

    protected final Interface _iface;
    protected Signal<Root> _validated = Signal.create();
    protected Element<?> _active;
    protected MenuHost _menuHost;
}
