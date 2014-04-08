//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.List;

import playn.core.Events;
import playn.core.Layer;
import playn.core.Mouse;
import playn.core.Pointer;
import playn.core.Pointer.Event;
import pythagoras.f.Point;
import react.Signal;
import react.SignalView;
import react.Slot;
import tripleplay.anim.Animation;
import tripleplay.anim.Animator;
import tripleplay.ui.MenuItem.ShowText;
import tripleplay.util.Layers;

/**
 * Holds a collection of {@link MenuItem}s, dispatching a {@link Menu#itemTriggered} signal
 * when one is selected and triggered. Normally used in conjunction with {@link MenuHost} to popup
 * the menu (in its own {@code Root}), manage animations, track user input, and handle
 * cancellation.
 *
 * <p>Note that a menu can contain arbitrary {@code Element}s, but only those that are
 * {@code MenuItem}s are eligible for triggering. Changes to the children of previously added
 * {@link Elements} instances are tracked using {@link Elements#childAdded()} and {@link Elements#
 * childRemoved()}.</p>
 *
 * <p>Note about {@link Container} types other than {@code Elements}: it is assumed that the
 * children of such containers will NOT change after addition to the menu. Such changes will
 * result in undefined behavior, potentially including memory leaks. {@link Scroller}, for example,
 * is safe to use since it has exactly one child element that doesn't change.</p>
 *
 * TODO: support escape key to cancel; probably in MenuHost
 * TODO: support/implement full screen menus - this is probably what most phone apps will want
 */
public class Menu extends Elements<Menu>
{
    /**
     * Produces an animation for a menu.
     */
    public interface AnimFn
    {
        /**
         * For the given menu and animator, adds an animation to the given animator and returns it.
         * TODO: how can more than one animation be supported? seems Animation should have a join()
         * method in addition to then()
         */
        Animation go (Menu menu, Animator animator);
    }

    /** Generic animation to fade in a menu using the layer alpha. */
    public static AnimFn FADE_IN = new AnimFn() {
        public Animation go (Menu menu, Animator animator) {
            menu.layer.setAlpha(0);
            return animator.tweenAlpha(menu.layer).to(1).easeIn().in(200);
        }
    };

    /** Generic animation to fade out a menu using the layer alpha. */
    public static AnimFn FADE_OUT = new AnimFn() {
        public Animation go (Menu menu, Animator animator) {
            return animator.tweenAlpha(menu.layer).to(0).easeIn().in(40);
        }
    };

    /** The opening animation function for the menu. */
    public static Style<AnimFn> OPENER = Style.newStyle(true, FADE_IN);

    /** The closing animation function for the menu. */
    public static Style<AnimFn> CLOSER = Style.newStyle(true, FADE_OUT);

    /**
     * Creates a new menu using the given layout for its elements.
     */
    public Menu (Layout layout) {
        super(layout);

        // use a hit tester "eater" to pretend our layer covers all its siblings
        layer.setHitTester(new Layer.HitTester() {
            @Override public Layer hitTest (Layer layer, Point p) {
                Layer descendant = layer.hitTestDefault(p);
                return descendant == null ? absorbHits() ? layer : null : descendant;
            }
        });

        // deactivate the menu on any pointer events (children will still get theirs)
        layer.addListener(new Pointer.Adapter() {
            @Override public void onPointerStart (Pointer.Event event) {
                if (event.hit() == layer) deactivate();
            }
        });

        childAdded().connect(_descendantAdded);
        childRemoved().connect(_descendantRemoved);
    }

    /**
     * Creates a new menu using the given layout and styles.
     */
    public Menu (Layout layout, Styles styles) {
        this(layout);
        setStyles(styles);
    }

    /**
     * Creates a new menu using the given layout and style bindings.
     */
    public Menu (Layout layout, Style.Binding<?>... styles) {
        this(layout);
        setStyles(styles);
    }

    /**
     * Gets the signal that is dispatched when the menu is closed and no longer usable. This
     * occurs if an item is triggered or if the menu is manually cancelled (using
     * {@link #deactivate()}).
     */
    public SignalView<Menu> deactivated () {
        return _deactivated;
    }

    /**
     * Opens this menu, using an animation created by the resolved {@link #OPENER} style. Once the
     * animation is finished, the user can view the {@code MenuItem} choices. When one is selected
     * and dispatched via the {@link #itemTriggered} signal, the menu is deactivated automatically.
     */
    public void activate () {
        // already active, nothing to do
        if (_active) return;

        // Undefunct
        _defunct = false;

        Runnable doActivation = new Runnable() {
            @Override public void run () {
                // skip to the end!
                fastForward();

                // animate the menu opening
                _complete = new Runnable() {
                    @Override public void run () {
                        onOpened();
                    }
                };
                _anim = open().then().action(_complete).handle();
            }
        };

        // postpone the activation if we need validation
        if (isSet(Flag.VALID)) doActivation.run();
        else _postLayout = doActivation;
    }

    /**
     * Closes this menu, using an animation created by the resolved {@link #CLOSER} style. This is
     * normally called automatically when the user clicks off the menu or triggers one of its
     * {@code MenuItem}s. After the animation is complete, the {@link #deactivated()} signal will
     * be dispatched.
     */
    public void deactivate () {
        // skip to the end!
        fastForward();

        // disable input and animate closure
        _active = false;
        _defunct = true;
        _complete = new Runnable() {
            @Override public void run () {
                onClosed();
            }
        };
        _anim = close().then().action(_complete).handle();
    }

    /**
     * Gets the signal that is dispatched when a menu item is selected.
     */
    public SignalView<MenuItem> itemTriggered () {
        return _itemTriggered;
    }

    /** Tests if this menu's position should be adjusted by the host such that the menu's bounds
     * lies within the requested area. */
    protected boolean automaticallyConfine () {
        return true;
    }

    /** Tests if this menu should trap all positional events. */
    protected boolean absorbHits () {
        return true;
    }

    @Override protected Class<?> getStyleClass () {
        return Menu.class;
    }

    @Override protected void layout () {
        super.layout();

        // and now activate if it was previously requested and we weren't yet valid
        if (_postLayout != null) {
            _postLayout.run();
            _postLayout = null;
        }
    }

    /** Creates an animation to move the menu's layer (and its children) into the open state.
     * By default, simply resolves the {@link #OPENER} style and calls {@link AnimFn#go}.
     * Subclasses can hook in here if needed. */
    protected Animation open () {
        return resolveStyle(OPENER).go(this, _animator);
    }

    /** Creates an animation to move the menu's layer (and its children) into the open state.
     * By default, simply resolves the {@link #CLOSER} style and calls {@link AnimFn#go}.
     * Subclasses can hook in here if needed. */
    protected Animation close () {
        return resolveStyle(CLOSER).go(this, _animator);
    }

    /** Called when the animation to open the menu is complete or fast forwarded. */
    protected void onOpened () {
        clearAnim();
        _active = true;
        Pointer.Event pd = _pendingDrag, pe = _pendingEnd;
        _pendingDrag = _pendingEnd = null;
        if (pe != null) onPointerEnd(pe);
        else if (pd != null) onPointerDrag(pd);
    }

    /** Called when the animation to close the menu is complete or fast forwarded. */
    protected void onClosed () {
        clearAnim();
        _deactivated.emit(this);
        _selector.selected.update(null);
    }

    /** Runs the animation completion action and cancels the animation. */
    protected void fastForward () {
        if (_anim != null) {
            // cancel the animation
            _anim.cancel();
            // run our complete logic manually (this will always invoke clearAnim too)
            _complete.run();
            assert _anim == null && _complete == null;
        }
    }

    /** Clears out members used during animation. */
    protected void clearAnim () {
        _complete = null;
        _anim = null;
    }

    /** Connects up the menu item. This gets called when any descendant is added that is an
     * instance of MenuItem. */
    protected void connectItem (MenuItem item) {
        _items.add(item);
        item.setRelay(Layers.join(
            item.layer.addListener((Pointer.Listener)_itemListener),
            item.layer.addListener((Mouse.LayerListener)_itemListener)));
    }

    /** Disconnects the menu item. This gets called when any descendant is removed that is an
     * instance of MenuItem. */
    protected void disconnectItem (MenuItem item) {
        int itemIdx = _items.indexOf(item);
        _items.remove(itemIdx);
        item.setRelay(Layers.NOT_LISTENING);
        didDisconnectItem(item, itemIdx);
    }

    /** Notifes subclasses of item removal, in case they want to know the index. */
    protected void didDisconnectItem (MenuItem item, int itemIdx) {
    }

    /** Called by the host when the pointer is dragged. */
    protected void onPointerDrag (Pointer.Event e) {
        if (!_active) {
            _pendingDrag = e;
            return;
        }

        _selector.selected.update(getHover(e));
    }

    /** Called by the host when the pointer is lifted. */
    protected void onPointerEnd (Pointer.Event e) {
        if (!_active) {
            _pendingEnd = e;
            return;
        }

        MenuItem hover = getHover(e);
        Element<?> selected = _selector.selected.get();
        _selector.selected.update(hover);
        if (hover == null) return;

        // trigger if this is the 2nd click -or- we always show text
        if (hover == selected || hover._showText == ShowText.ALWAYS) {
            if (isVisible() && hover.isEnabled()) {
                hover.trigger();
                _itemTriggered.emit(hover);
                deactivate();
            }
        }
    }

    /** Gets the item underneath the given event. */
    protected MenuItem getHover (Events.Position e) {
        // manual hit detection
        Layer hit = layer.hitTest(Layer.Util.screenToLayer(layer, e.x(), e.y()));

        for (MenuItem item : _items) {
            if (item.isVisible() && item.layer == hit) {
                return item;
            }
        }

        return null;
    }

    /** Called by the host when the menu is popped. */
    protected void init (Animator animator) {
        _animator = animator;
    }

    protected abstract class DescendingSlot extends Slot<Element<?>>
    {
        @Override public void onEmit (Element<?> elem) {
            if (elem instanceof Container) {
                for (Element<?> child : (Container<?>)elem) onEmit(child);
                if (elem instanceof Elements) visitElems((Elements<?>)elem);
            } else if (elem instanceof MenuItem) {
                visitItem((MenuItem)elem);
            }
        }
        protected abstract void visitElems (Elements<?> elems);
        protected abstract void visitItem (MenuItem item);
    }

    protected class ItemListener extends Mouse.LayerAdapter
        implements Pointer.Listener
    {
        @Override public void onPointerStart (Event event) {
            Menu.this.onPointerDrag(event);
        }

        @Override public void onPointerDrag (Event event) {
            Menu.this.onPointerDrag(event);
        }

        @Override public void onPointerEnd (Event event) {
            Menu.this.onPointerEnd(event);
        }

        @Override public void onPointerCancel (Event event) {
        }

        @Override public void onMouseOver (Mouse.MotionEvent event) {
            if (_active) _selector.selected.update(getHover(event));
        }

        @Override public void onMouseOut (Mouse.MotionEvent event) {
            if (_active) _selector.selected.update(null);
        }
    }

    protected final Slot<Element<?>> _descendantAdded = new DescendingSlot() {
        @Override protected void visitElems (Elements<?> elems) {
            elems.childAdded().connect(_descendantAdded);
            elems.childRemoved().connect(_descendantRemoved);
        }
        @Override protected void visitItem (MenuItem item) {
            connectItem(item);
        }
    };

    protected final Slot<Element<?>> _descendantRemoved = new DescendingSlot() {
        @Override protected void visitElems (Elements<?> elems) {
            elems.childAdded().disconnect(_descendantAdded);
            elems.childRemoved().disconnect(_descendantRemoved);
        }
        @Override protected void visitItem (MenuItem item) {
            disconnectItem(item);
        }
    };

    protected ItemListener _itemListener = new ItemListener();

    /** Dispatched when the menu is deactivated. */
    protected final Signal<Menu> _deactivated = Signal.create();

    /** Dispatched when an item in the menu is triggered. */
    protected final Signal<MenuItem> _itemTriggered = Signal.create();

    /** Tracks the currently selected menu item (prior to triggering, an item is selected). */
    protected final Selector _selector = new Selector();

    protected final List<MenuItem> _items = new ArrayList<MenuItem>();

    /** Animator that runs the menu opening and closing states, usually from Interface. */
    protected Animator _animator;

    /** Handle to the current open or close animation, or null if no animation is active. */
    protected Animation.Handle _anim;

    /** Stash of the last Animation.Action in case we need to cancel it. For example, if the
     * menu is deactivated before it finished opening. */
    protected Runnable _complete;

    /** Method to execute after layout, used to activate the menu. */
    protected Runnable _postLayout;

    /** Whether the menu is ready for user input. */
    protected boolean _active;

    /** Whether the menu is closed. */
    protected boolean _defunct;

    /** Input events that may have occurred prior to the menu being ready. */
    protected Pointer.Event _pendingDrag, _pendingEnd;
}
