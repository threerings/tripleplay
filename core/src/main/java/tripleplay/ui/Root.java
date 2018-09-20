//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.IDimension;
import react.Closeable;
import react.Signal;
import react.SignalView;

/**
 * The root of a display hierarchy. An application can have one or more roots, but they should not
 * overlap and will behave as if oblivious to one another's existence.
 */
public class Root extends Elements<Root> implements Closeable
{
    /** The interface of which this root is a part. */
    public final Interface iface;

    /** A signal emitted when this root is validated. */
    public final SignalView<Root> validated = Signal.create();

    /** Creates a new root with the provided layout and stylesheet. a*/
    public Root (Interface iface, Layout layout, Stylesheet sheet) {
        super(layout);
        this.iface = iface;
        setStylesheet(sheet);

        set(Flag.HIT_ABSORB, true);
    }

    /** Sizes this root element to its preferred size. */
    public Root pack () {
        return pack(0, 0);
    }

    /** Sizes this element to its preferred size, computed using the supplied hints. */
    public Root pack (float widthHint, float heightHint) {
        IDimension psize = preferredSize(widthHint, heightHint);
        setSize(psize.width(), psize.height());
        return this;
    }

    /** Sizes this root element to the specified width and its preferred height. */
    public Root packToWidth (float width) {
        IDimension psize = preferredSize(width, 0);
        setSize(width, psize.height());
        return this;
    }

    /** Sizes this root element to the specified height and its preferred width. */
    public Root packToHeight (float height) {
        IDimension psize = preferredSize(0, height);
        setSize(psize.width(), height);
        return this;
    }

    /** Sets the size of this root element. */
    @Override public Root setSize (float width, float height) {
        _size.setSize(width, height);
        invalidate();
        return this;
    }

    /** Sets the size of this root element. */
    public Root setSize (IDimension size) { return setSize(size.width(), size.height()); }

    /** Sets the size of this root element and its translation from its parent. */
    public Root setBounds (float x, float y, float width, float height) {
        setSize(width, height);
        setLocation(x, y);
        return this;
    }

    /** Configures the location of this root, relative to its parent layer. */
    @Override public void setLocation (float x, float y) {
        super.setLocation(x, y);
    }

    @Override public boolean isShowing () {
        return isVisible();
    }

    /** See {@link Interface#disposeRoot}. */
    @Override public void close () {
        iface.disposeRoot(this);
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
     * {@link Interface#paint} if the root is created via {@link Interface}.
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
            _menuHost = new MenuHost(iface, this);
        }
        return _menuHost;
    }

    @Override protected Class<?> getStyleClass () {
        return Root.class;
    }

    @Override protected Root root () {
        return this;
    }

    @Override protected void wasValidated () {
        super.wasValidated();
        ((Signal<Root>)validated).emit(this);
    }

    protected Element<?> _active;
    protected MenuHost _menuHost;
}
