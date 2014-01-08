//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.IPoint;
import pythagoras.f.IRectangle;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;

import playn.core.Connection;
import playn.core.Events;
import playn.core.GroupLayer;
import playn.core.Layer;
import playn.core.Pointer;

import react.Slot;

import tripleplay.platform.TPPlatform;
import tripleplay.ui.util.BoxPoint;
import tripleplay.util.Layers;

import static playn.core.PlayN.*;
import static tripleplay.ui.Log.log;

/**
 * Provides a context for popping up a menu.
 */
public class MenuHost
{
    /** Defines how to obtain the point on a trigger where a menu popup originates. */
    public interface TriggerPoint
    {
        /** For the given trigger and pointer position, gets the screen coordinates where the
         * menu popup should originate. */
        public Point getLocation (Element<?> trigger, IPoint pointer);
    }

    /** Gets a trigger point relative to an element using the given box point. */
    public static TriggerPoint relative (final BoxPoint location) {
        return new TriggerPoint() {
            @Override public Point getLocation (Element<?> trigger, IPoint pointer) {
                return location.resolve(trigger, new Point());
            }
        };
    }

    /** Gets a fixed trigger point for the given screen coordinates. */
    public static TriggerPoint absolute (final float x, final float y) {
        return new Absolute(x, y);
    }

    /** Gets a trigger point exactly under the pointer position. */
    public static TriggerPoint pointer () {
        return new TriggerPoint() {
            @Override public Point getLocation (Element<?> trigger, IPoint pointer) {
                return new Point(pointer);
            }
        };
    }

    /** The point on an element where menus should be placed, subject to boundary constraints.
     * This is only used if the element is set to a {@link Pop#trigger}. By default, uses the
     * top left corner of the trigger. */
    public static final Style<TriggerPoint> TRIGGER_POINT =
            Style.newStyle(true, relative(BoxPoint.TL));

    /** The point on the menu that should be placed directly on top of the trigger point, subject
     * to bounding constraints. This is only used if the element is set to a {@link Pop#trigger}.
     * By default, the top, left corner is the origin. */
    public static final Style<BoxPoint> POPUP_ORIGIN = Style.newStyle(true, BoxPoint.TL);

    /** The root layer that will contain all menus that pop up. It should normally be close to the
     * top of the hierarchy so that it draws on top of everything. */
    public final GroupLayer rootLayer;

    /** The interface we use to create the menu's root and do animation. */
    public final Interface iface;

    /**
     * An event type for triggering a menu popup.
     */
    public static class Pop
    {
        /** The element that triggered the popup. {@link #position} is relative to this. */
        public final Element<?> trigger;

        /** The menu to show. */
        public final Menu menu;

        /** The position of the pointer, if given during construction, otherwise null. */
        public final IPoint pointer;

        /** The bounds to confine the menu, in screen coordinates; usually the whole screen. */
        public IRectangle bounds;

        /** Creates a new event and initializes {@link #trigger} and {@link #menu}. */
        public Pop (Element<?> trigger, Menu menu) {
            this(trigger, menu, null);
        }

        /** Creates a new event and initializes {@link #trigger} and {@link #menu}. */
        public Pop (Element<?> trigger, Menu menu, Events.Position pointer) {
            if (menu == null) throw new IllegalArgumentException();
            this.menu = menu;
            this.trigger = trigger;
            this.pointer = pointer == null ? null : Events.Util.screenPos(pointer);
        }

        /**
         * Causes the menu to handle further events on the given layer. This is usually the layer
         * handling a pointer start that caused the popup. A listener will be added to the layer
         * and the menu notified of pointer drag and end events.
         */
        public Pop relayEvents (Layer layer) {
            _relayTarget = layer;
            return this;
        }

        /**
         * Flags this {@code Pop} event so that the menu will not be destroyed automatically when
         * it is deactivated. Returns this instance for chaining.
         */
        public Pop retainMenu () {
            _retain = true;
            return this;
        }

        /**
         * Optionally confines the menu area to the given screen area. By default the menu is
         * confined by the host's screen area (see {@link MenuHost#getScreenArea()}).
         */
        public Pop inScreenArea (IRectangle area) {
            bounds = new Rectangle(area);
            return this;
        }

        /**
         * Optionally confines the menu area to the given element. By default the menu is confined
         * by the host's screen area (see {@link MenuHost#getScreenArea()}).
         */
        public Pop inElement (Element<?> elem) {
            Point tl = Layer.Util.layerToScreen(elem.layer, 0, 0);
            Point br = Layer.Util.layerToScreen(
                elem.layer, elem.size().width(), elem.size().height());
            bounds = new Rectangle(tl.x(), tl.y(), br.x() - tl.x(), br.y() - tl.y());
            return this;
        }

        /**
         * Pops up this instance on the trigger's root's menu host. See {@link MenuHost#popup(Pop)}.
         * For convenience, returns the host that was used to perform the popup.
         */
        public MenuHost popup () {
            Root root = trigger.root();
            if (root == null) {
                Log.log.warning("Menu trigger not on a root?", "trigger", trigger);
                return null;
            }
            root.getMenuHost().popup(this);
            return root.getMenuHost();
        }

        /** Whether we should keep the menu around (i.e. not destroy it). */
        protected boolean _retain;

        /** The layer that will be sending pointer drag and end events to us. */
        protected Layer _relayTarget;
    }

    public static Connection relayEvents (Layer from, final Menu to) {
        return from.addListener(new Pointer.Adapter() {
            @Override public void onPointerDrag (Pointer.Event e) { to.onPointerDrag(e); }
            @Override public void onPointerEnd (Pointer.Event e) { to.onPointerEnd(e); }
        });
    }

    /**
     * Creates a menu host using the given values. The root layer is set to the layer of the given
     * root and the stylesheet to its stylesheet.
     */
    public MenuHost (Interface iface, Elements<?> root) {
        this(iface, root.layer);
        _stylesheet = root.stylesheet();
    }

    /**
     * Creates a new menu host using the given values. The stylesheet is set to an empty
     * one and can be changed via the {@link #setStylesheet(Stylesheet)} method.
     */
    public MenuHost (Interface iface, GroupLayer rootLayer) {
        this.iface = iface;
        this.rootLayer = rootLayer;
    }

    /**
     * Sets the stylesheet for menus popped by this host.
     */
    public MenuHost setStylesheet (Stylesheet sheet) {
        _stylesheet = sheet;
        return this;
    }

    /**
     * Deactivates the current menu, if any is showing.
     */
    public void deactivate () {
        if (_active != null) {
            _active.pop.menu.deactivate();
        }
    }

    /**
     * Sets the area to which menus should be confined when there isn't any other associated
     * bounds.
     */
    public void setScreenArea (IRectangle screenArea) {
        _screenArea.setBounds(screenArea);
    }

    /**
     * Gets the area to which menus should be confined when there isn't any other associated
     * bounds. By default, the entire available area is used, as given by
     * {@link playn.core.Graphics}.
     */
    public IRectangle getScreenArea () {
        return _screenArea;
    }

    /**
     * Displays the menu specified by the given pop, incorporating all the configured attributes
     * therein.
     */
    public void popup (final Pop pop) {
        // if there is no explicit constraint area requested, use the graphics
        if (pop.bounds == null) pop.inScreenArea(_screenArea);

        // set up the menu root, the RootLayout will do the complicated bounds jockeying
        final MenuRoot menuRoot = iface.addRoot(new MenuRoot(iface, _stylesheet, pop));
        rootLayer.add(menuRoot.layer);
        menuRoot.pack();

        // set up the activation
        final Activation activation = new Activation(pop);

        // cleanup
        final Runnable cleanup = new Runnable() {
            @Override public void run () {
                // check parentage, it's possible the menu has been repopped already
                if (pop.menu.parent() == menuRoot) {
                    // free the constraint to gc
                    pop.menu.setConstraint(null);

                    // remove or destroy it, depending on the caller's preference
                    if (pop._retain) menuRoot.remove(pop.menu);
                    else menuRoot.destroy(pop.menu);

                    // remove the hidden area we added
                    TPPlatform.instance().hideNativeOverlays(null);
                }

                // clear all connections
                activation.clear();

                // always kill off the transient root
                iface.destroyRoot(menuRoot);

                // if this was our active menu, clear it
                if (_active != null && _active.pop == pop) _active = null;
            }
        };

        // connect to deactivation signal and do our cleanup
        activation.deactivated = pop.menu.deactivated().connect(new Slot<Menu>() {
            @Override public void onEmit (Menu event) {
                // due to animations, deactivation can happen during layout, so do it next frame
                invokeLater(cleanup);
            }
        });

        // close the menu any time the trigger is removed from the hierarchy
        activation.triggerRemoved = pop.trigger.hierarchyChanged().connect(new Slot<Boolean>() {
            @Override public void onEmit (Boolean event) {
                if (!event) pop.menu.deactivate();
            }
        });

        // deactivate the old menu
        if (_active != null) _active.pop.menu.deactivate();

        // pass along the animator
        pop.menu.init(iface.animator());

        // activate
        _active = activation;
        pop.menu.activate();
    }

    public Pop activePop () {
        return _active != null ? _active.pop : null;
    }

    public Menu active () {
        return _active != null ? _active.pop.menu : null;
    }

    protected static class MenuRoot extends Root
    {
        public final Pop pop;

        public MenuRoot (Interface iface, Stylesheet sheet, Pop pop) {
            super(iface, new RootLayout(), sheet);
            this.pop = pop;
            layer.setDepth(1);
            layer.setHitTester(null); // get hits from out of bounds
            add(pop.menu);
        }
    }

    /** Simple layout for positioning the menu within the transient {@code Root}. */
    protected static class RootLayout extends Layout
    {
        @Override public Dimension computeSize (Container<?> elems, float hintX, float hintY) {
            return new Dimension(preferredSize(elems.childAt(0), hintX, hintY));
        }

        @Override public void layout (Container<?> elems, float left, float top, float width,
                                      float height) {
            if (elems.childCount() == 0) return;

            MenuRoot menuRoot = (MenuRoot)elems;
            Pop pop = menuRoot.pop;

            // get the trigger point from the trigger
            TriggerPoint position = resolveStyle(pop.trigger, TRIGGER_POINT);

            // get the origin point from the menu
            BoxPoint origin = resolveStyle(pop.trigger, POPUP_ORIGIN);

            // get the desired position, may be relative to trigger or pointer
            Point tpos = position.getLocation(pop.trigger, pop.pointer);
            Point mpos = origin.resolve(0, 0, width, height, new Point());

            // figure out the best place to put the menu, in screen coordinates; starting with
            // the requested popup position
            Rectangle bounds = new Rectangle(tpos.x - mpos.x, tpos.y - mpos.y, width, height);

            // make sure the menu lies inside the requested bounds if the menu doesn't do
            // that itself
            if (pop.menu.automaticallyConfine()) {
                confine(pop.bounds, bounds);

                // fudge is the number of pixels around the menu that we don't need to avoid
                // TODO: can we get the menu's Background's insets?
                float fudge = 2;

                // TODO: do we need any of this finger avoidance stuff if the popup is not
                // relative to the pointer? E.g. a combo box with its menu off to the right

                // keep the bounds from overlapping the position
                if (bounds.width > fudge * 2 && bounds.height > fudge * 2) {
                    Rectangle ibounds = new Rectangle(bounds);
                    ibounds.grow(-fudge, -fudge);

                    // set up the fingerprint
                    float fingerRadius = touch().hasTouch() ? 10 : 3;
                    IPoint fingerPos = pop.pointer == null ? tpos : pop.pointer;
                    Rectangle fingerBox = new Rectangle(
                        fingerPos.x() - fingerRadius, fingerPos.y() - fingerRadius,
                        fingerRadius * 2, fingerRadius * 2);

                    // try and place the menu so it isn't under the finger
                    if (!avoidPoint(pop.bounds, ibounds, fingerBox)) {
                        log.warning("Oh god, menu doesn't fit", "menu", pop.menu);
                    }
                    bounds.setLocation(ibounds.x() - fudge, ibounds.y() - fudge);
                }
            }

            // save a copy of bounds in screen coordinates
            Rectangle screenBounds = new Rectangle(bounds);

            // relocate to layer coordinates
            bounds.setLocation(Layer.Util.screenToLayer(elems.layer, bounds.x, bounds.y));

            // set the menu bounds
            setBounds(elems.childAt(0), bounds.x, bounds.y, bounds.width, bounds.height);

            // check if menu is closed (layout can still occur in this state)
            if (!pop.menu._defunct) {
                // tell the UI overlay to let the real dimensions of the menu through
                // TODO: this looks wrong if the menu has any transparent border - fix
                // by using an image overlay instead, with the root captured onto it
                TPPlatform.instance().hideNativeOverlays(screenBounds);
            }
        }
    }

    /** Tries to place the inner bounds within the outer bounds, such that the inner bounds does
     * not contain the position. */
    protected static boolean avoidPoint (IRectangle outer, Rectangle inner, IRectangle fingerprint)
    {
        Rectangle checkBounds = new Rectangle();
        Rectangle best = new Rectangle(inner);
        float bestDist = Float.MAX_VALUE, edge;

        // confine to the left
        edge = fingerprint.x();
        checkBounds.setBounds(outer.x(), outer.y(), edge - outer.x(), outer.height());
        bestDist = compareAndConfine(checkBounds, inner, best, bestDist);

        // right
        edge = fingerprint.maxX();
        checkBounds.setBounds(edge, outer.y(), outer.width() - edge, outer.height());
        bestDist = compareAndConfine(checkBounds, inner, best, bestDist);

        // top
        edge = fingerprint.y();
        checkBounds.setBounds(outer.x(), outer.y(), outer.width(), edge - outer.y());
        bestDist = compareAndConfine(checkBounds, inner, best, bestDist);

        // bottom
        edge = fingerprint.maxY();
        checkBounds.setBounds(outer.x(), edge, outer.width(), outer.height() - edge);
        bestDist = compareAndConfine(checkBounds, inner, best, bestDist);

        inner.setBounds(best);
        return bestDist < Float.MAX_VALUE;
    }

    /** Confines a rectangle and updates the current best fit based on the moved distance. */
    protected static float compareAndConfine (
        IRectangle outer, IRectangle inner, Rectangle best, float bestDist) {

        // don't bother if there isn't even enough space
        if (outer.width() <= inner.width() || outer.height() < inner.height()) return bestDist;

        // confine
        Rectangle confined = confine(outer, new Rectangle(inner));

        // check distance and overwrite the best fit if we have a new winner
        float dx = confined.x - inner.x(), dy = confined.y - inner.y();
        float dist = dx * dx + dy * dy;
        if (dist < bestDist) {
            best.setBounds(confined);
            bestDist = dist;
        }

        return bestDist;
    }

    /** Moves ths given inner rectangle such that it lies within the given outer rectangle.
     * The results are undefined if either the inner width or height is greater that the outer's
     * width or height, respectively. */
    protected static Rectangle confine (IRectangle outer, Rectangle inner) {
        float dx = outer.x() - inner.x(), dy = outer.y() - inner.y();
        if (dx <= 0) dx = Math.min(0, outer.maxX() - inner.maxX());
        if (dy <= 0) dy = Math.min(0, outer.maxY() - inner.maxY());
        inner.translate(dx, dy);
        return inner;
    }

    /** Holds a few variables related to the menu's activation. */
    protected static class Activation
    {
        /** The configuration of the menu. */
        public final Pop pop;

        /** Connects to the pointer events from the relay. */
        public Connection pointerRelay = Layers.NOT_LISTENING;

        /** Connection to the trigger's hierarchy change. */
        public react.Connection triggerRemoved;

        /** Connection to the menu's deactivation. */
        public react.Connection deactivated;

        /** Creates a new activation. */
        public Activation (Pop pop) {
            this.pop = pop;

            // handle pointer events from the relay
            Layer target = pop._relayTarget;
            if (target != null) pointerRelay = relayEvents(target, pop.menu);
        }

        /** Clears out the connections. */
        public void clear () {
            if (triggerRemoved != null) triggerRemoved.disconnect();
            if (deactivated != null) deactivated.disconnect();
            pointerRelay.disconnect();
            pointerRelay = null;
            triggerRemoved = null;
            deactivated = null;
        }
    }

    protected static class Absolute implements TriggerPoint
    {
        public final Point pos;

        protected Absolute (float x, float y) {
            pos = new Point(x, y);
        }

        @Override public Point getLocation (Element<?> trigger, IPoint pointer) {
            return new Point(pos);
        }
    }

    /** The stylesheet used for popped menus. */
    protected Stylesheet _stylesheet = Stylesheet.builder().create();

    /** Currently active. */
    protected Activation _active;

    /** When confining the menu to the graphics' bounds, use this. */
    protected final Rectangle _screenArea = new Rectangle(
        0, 0, graphics().width(), graphics().height());
}
