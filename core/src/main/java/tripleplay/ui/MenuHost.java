package tripleplay.ui;

import playn.core.Connection;
import playn.core.Events;
import playn.core.GroupLayer;
import playn.core.Layer;
import playn.core.Pointer;
import pythagoras.f.Dimension;
import pythagoras.f.IPoint;
import pythagoras.f.IRectangle;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;
import react.Slot;

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
         * Positions the menu horizontally relative to the left edge of the trigger.
         */
        public Pop toLeft (float x) {
            return atLayerX(trigger.layer, x);
        }

        /**
         * Positions the menu horizontally relative to the right edge of the trigger.
         */
        public Pop toRight (float x) {
            return atLayerX(trigger.layer, trigger.size().width() + x);
        }

        /**
         * Positions the menu vertically relative to the top edge of the trigger.
         */
        public Pop toTop (float y) {
            return atLayerY(trigger.layer, y);
        }

        /**
         * Positions the menu vertically relative to the bottom edge of the trigger.
         */
        public Pop toBottom (float y) {
            return atLayerY(trigger.layer, trigger.size().height() + y);
        }

        /**
         * Flags this <code>Pop</code> event so that the menu will not be destroyed automatically
         * when it is deactivated. Returns this instance for chaining.
         */
        public Pop retainMenu () {
            _retain = true;
            return this;
        }

        /**
         * Optionally confines the menu area to the given screen area. By default the menu is
         * confined by the size of the application (see {@link playn.core.Graphics}).
         */
        public Pop inScreenArea (IRectangle area) {
            bounds = new Rectangle(area);
            return this;
        }

        /**
         * Optionally confines the menu area to the given element. By default the menu is confined
         * by the size of the application area (see {@link playn.core.Graphics}).
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
     * Displays the menu specified by the given pop, incorporating all the configured attributes
     * therein.
     */
    public void popup (final Pop pop) {
        // if there is no explicit constraint area requested, use the graphics
        if (pop.bounds == null) pop.inScreenArea(new Rectangle(
            0, 0, graphics().width(), graphics().height()));

        // set up the menu root
        final Root menuRoot = iface.createRoot(new RootLayout(), _stylesheet, rootLayer);
        menuRoot.layer.setDepth(1);
        menuRoot.layer.setHitTester(null); // get hits from out of bounds
        menuRoot.add(pop.menu.setConstraint(pop));
        menuRoot.pack();

        // position the menu
        Point loc = Layer.Util.screenToLayer(rootLayer, pop.position.x(), pop.position.y());
        menuRoot.layer.setTranslation(loc.x, loc.y);

        // set up the activation
        final Activation activation = new Activation(pop);

        // connect to deactivation signal and do our cleanup
        activation.deactivated = pop.menu.deactivated().connect(new Slot<Menu>() {
            @Override public void onEmit (Menu event) {
                // check parentage, it's possible the menu has been repopped already
                if (pop.menu.parent() == menuRoot) {
                    // free the constraint to gc
                    pop.menu.setConstraint(null);

                    // remove or destroy it, depending on the caller's preference
                    if (pop._retain) menuRoot.remove(pop.menu);
                    else menuRoot.destroy(pop.menu);
                }

                // clear all connections
                activation.clear();

                // TODO: do we need to stop menu animation here? it should be automatic since
                // by the time we reach this method, the deactivation animation is complete

                // always kill off the transient root
                iface.destroyRoot(menuRoot);

                // if this was our active menu, clear it
                if (_active != null && _active.pop.menu == event) _active = null;
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

    /** Simple layout for positioning the menu within the transient {@code Root}. */
    protected static class RootLayout extends Layout
    {
        @Override public Dimension computeSize (Elements<?> elems, float hintX, float hintY) {
            return new Dimension(preferredSize(elems.childAt(0), hintX, hintY));
        }

        @Override public void layout (Elements<?> elems, float left, float top, float width,
                                      float height) {
            if (elems.childCount() == 0) return;

            // get the constraint, it will always be a Pop
            Pop pop = (Pop)elems.childAt(0).constraint();

            // make sure the menu lies inside the requested bounds if the menu doesn't do
            // that itself
            if (pop.menu.automaticallyConfine()) {
                IRectangle bounds = pop.bounds;
                Point tl = Layer.Util.screenToLayer(elems.layer, bounds.x(), bounds.y());
                Point br = Layer.Util.screenToLayer(elems.layer,
                    bounds.x() + bounds.width(), bounds.y() + bounds.height());
                // nudge location
                left = Math.min(Math.max(left, tl.x), br.x - width);
                top = Math.min(Math.max(top, tl.y), br.y - height);
            }

            setBounds(elems.childAt(0), left, top, width, height);
        }
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
            if (target != null)
                pointerRelay = pop._relayTarget.addListener(new Pointer.Adapter() {
                    Menu menu = Activation.this.pop.menu;
                    @Override public void onPointerDrag (Pointer.Event e) { menu.onPointerDrag(e); }
                    @Override public void onPointerEnd (Pointer.Event e) { menu.onPointerEnd(e); }
                });
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
}
