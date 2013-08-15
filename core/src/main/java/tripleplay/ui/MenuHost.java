//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.PlayN;
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

import tripleplay.ui.Style.HAlign;
import tripleplay.ui.Style.VAlign;

import static playn.core.PlayN.graphics;

/**
 * Provides a context for popping up a menu.
 */
public class MenuHost
{
    /** The root layer that will contain all menus that pop up. It should normally be close to the
     * top of the hierarchy so that it draws on top of everything. */
    public final GroupLayer rootLayer;

    /** The interface we use to create the menu's root and do animation. */
    public final Interface iface;

    /**
     * An event type for triggering a menu popup. Also acts as a menu constraint so the integrated
     * host layout can make sure the whole menu is on screen and near to the triggering event
     * position or element.
     */
    public static class Pop extends Layout.Constraint
    {
        /** The element that triggered the popup. {@link #position} is relative to this. */
        public final Element<?> trigger;

        /** The menu to show. */
        public final Menu menu;

        /** The position where the menu should pop up, e.g. a touch event position. Relative to
         * {@link #trigger}. */
        public IPoint position;

        /** The bounds to confine the menu, in screen coordinates; usually the whole screen. */
        public IRectangle bounds;

        /** Creates a new event and initializes {@link #trigger} and {@link #menu}. */
        public Pop (Element<?> trigger, Menu menu) {
            this.menu = menu;
            this.trigger = trigger;
            position = new Point(0, 0);
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
         * Positions the menu popup at the given positional event.
         */
        public Pop atEventPos (Events.Position pos) {
            return atScreenPos(pos.x(), pos.y());
        }

        /**
         * Positions the menu popup at the given screen position.
         */
        public Pop atScreenPos (float x, float y) {
            position = new Point(x, y);
            return this;
        }

        /**
         * Positions the menu horizontally relative to the given layer, with an offset. The
         * vertical position remains unchanged.
         */
        public Pop atLayerX (Layer layer, float x) {
            return atScreenPos(Layer.Util.layerToScreen(layer, x, 0).x, position.y());
        }

        /**
         * Positions the menu vertically relative to the given layer, with an offset. The
         * horizontal position remains unchanged.
         */
        public Pop atLayerY (Layer layer, float y) {
            return atScreenPos(position.x(), Layer.Util.layerToScreen(layer, 0, y).y);
        }

        /**
         * Sets the horizontal alignment of the menu relative to the popup position.
         */
        public Pop halign (HAlign halign) {
            _halign = halign;
            return this;
        }

        /**
         * Sets the vertical alignment of the menu relative to the popup position.
         */
        public Pop valign (VAlign valign) {
            _valign = valign;
            return this;
        }

        /**
         * Positions the right edge of the menu relative to the left edge of the trigger, offset
         * by the given value.
         */
        public Pop toLeft (float x) {
            return atLayerX(trigger.layer, x).halign(HAlign.RIGHT);
        }

        /**
         * Positions the left edge of the menu relative to the right edge of the trigger, offset
         * by the given value.
         */
        public Pop toRight (float x) {
            return atLayerX(trigger.layer, trigger.size().width() + x).halign(HAlign.LEFT);
        }

        /**
         * Positions the top edge of the menu relative to the top edge of the trigger, offset
         * by the given value.
         */
        public Pop toTop (float y) {
            return atLayerY(trigger.layer, y).valign(VAlign.TOP);
        }

        /**
         * Positions the bottom edge of the menu relative to the bottom edge of the trigger, offset
         * by the given value.
         */
        public Pop toBottom (float y) {
            return atLayerY(trigger.layer, trigger.size().height() + y).valign(VAlign.BOTTOM);
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
         * confined by the hosts's screen area (see {@link MenuHost#getScreenArea()}).
         */
        public Pop inScreenArea (IRectangle area) {
            bounds = new Rectangle(area);
            return this;
        }

        /**
         * Optionally confines the menu area to the given element. By default the menu is confined
         * by the hosts's screen area (see {@link MenuHost#getScreenArea()}).
         */
        public Pop inElement (Element<?> elem) {
            Point tl = Layer.Util.layerToScreen(elem.layer, 0, 0);
            Point br = Layer.Util.layerToScreen(elem.layer, elem.size().width(), elem.size().height());
            bounds = new Rectangle(tl.x(), tl.y(), br.x() - tl.x(), br.y() - tl.y());
            return this;
        }

        /** Whether we should keep the menu around (i.e. not destroy it). */
        protected boolean _retain;

        /** The layer that will be sending pointer drag and end events to us. */
        protected Layer _relayTarget;

        protected HAlign _halign = HAlign.LEFT;
        protected VAlign _valign = VAlign.TOP;
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
     * Directs a menu pop signal to {@link #popup(Pop)}.
     */
    public Slot<Pop> onPopup () {
        return new Slot<Pop>() {
            @Override public void onEmit (Pop event) {
                popup(event);
            }
        };
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
        final Root menuRoot = iface.createRoot(new RootLayout(), _stylesheet, rootLayer);
        menuRoot.layer.setDepth(1);
        menuRoot.layer.setHitTester(null); // get hits from out of bounds
        menuRoot.add(pop.menu.setConstraint(pop));
        menuRoot.pack();
        menuRoot.layer.setTranslation(pop.position.x(), pop.position.y());

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
                    PlayN.uiOverlay().hideOverlay(null);
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
                PlayN.invokeLater(cleanup);
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

    public Menu active () {
        return _active != null ? _active.pop.menu : null;
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

            // get the constraint, it will always be a Pop
            Pop pop = (Pop)elems.childAt(0).constraint();

            // figure out the best place to put the menu, in screen coordinates; starting with
            // the requested popup position
            Rectangle bounds = new Rectangle(
                pop.position.x() + pop._halign.offset(width, 0),
                pop.position.y() + pop._valign.offset(height, 0), width, height);

            // make sure the menu lies inside the requested bounds if the menu doesn't do
            // that itself
            if (pop.menu.automaticallyConfine()) {
                confine(pop.bounds, bounds);

                // keep the bounds from overlapping the position
                float fudge = 2;
                if (bounds.width > fudge * 2 && bounds.height > fudge * 2) {
                    Rectangle ibounds = new Rectangle(bounds);
                    ibounds.grow(-fudge, -fudge);
                    if (ibounds.contains(pop.position)) {
                        avoidPoint(pop.bounds, ibounds, pop.position);
                        bounds.setLocation(ibounds.x() - fudge, ibounds.y() - fudge);
                    }
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
                PlayN.uiOverlay().hideOverlay(screenBounds);
            }
        }

    }

    /** Tries to place the inner bounds within the outer bounds, such that the inner bounds does
     * not contain the position. */
    protected static void avoidPoint (IRectangle outer, Rectangle inner, IPoint pos)
    {
        Rectangle checkBounds = new Rectangle();
        Rectangle best = new Rectangle(inner);
        float bestDist = Float.MAX_VALUE;

        float dx = pos.x() - outer.x(), dy = pos.y() - outer.y();

        // confine to the left
        checkBounds.setBounds(outer.x(), outer.y(), dx, outer.height());
        bestDist = compareAndConfine(checkBounds, inner, best, bestDist);

        // right
        checkBounds.setBounds(pos.x(), outer.y(), outer.width() - dx, outer.height());
        bestDist = compareAndConfine(checkBounds, inner, best, bestDist);

        // top
        checkBounds.setBounds(outer.x(), outer.y(), outer.width(), dy);
        bestDist = compareAndConfine(checkBounds, inner, best, bestDist);

        // bottom
        checkBounds.setBounds(outer.x(), pos.y(), outer.width(), outer.height() - dy);
        bestDist = compareAndConfine(checkBounds, inner, best, bestDist);

        inner.setBounds(best);
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
        public Connection pointerRelay;

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
            if (pointerRelay != null) pointerRelay.disconnect();
            if (triggerRemoved != null) triggerRemoved.disconnect();
            if (deactivated != null) deactivated.disconnect();
            pointerRelay = null;
            triggerRemoved = null;
            deactivated = null;
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
