//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.MathUtil;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;

import react.Closeable;
import react.Signal;
import react.SignalView;
import react.Slot;
import react.UnitSlot;
import react.ValueView;

import playn.scene.GroupLayer;
import playn.scene.Layer;

import tripleplay.ui.util.Insets;
import tripleplay.util.Ref;

/**
 * The root of the interface element hierarchy. See {@link Widget} for the root of all interactive
 * elements, and {@link Container} for the root of all container elements.
 *
 * @param T used as a "self" type; when subclassing {@code Element}, T must be the type of the
 * subclass.
 */
public abstract class Element<T extends Element<T>>
{
    /** The layer associated with this element. */
    public final GroupLayer layer = createLayer();

    protected Element () {
        // optimize hit testing by checking our bounds first
        layer.setHitTester(new Layer.HitTester() {
            public Layer hitTest (Layer layer, Point p) {
                Layer hit = null;
                if (isVisible() && contains(p.x, p.y)) {
                    if (isSet(Flag.HIT_DESCEND)) hit = layer.hitTestDefault(p);
                    if (hit == null && isSet(Flag.HIT_ABSORB)) hit = layer;
                }
                return hit;
            }
            @Override public String toString () {
                return "<" + size() + ">";
            }
        });

        // descend by default
        set(Flag.HIT_DESCEND, true);
    }

    /**
     * Returns this element's x offset relative to its parent.
     */
    public float x () {
        return layer.tx();
    }

    /**
     * Returns this element's y offset relative to its parent.
     */
    public float y () {
        return layer.ty();
    }

    /**
     * Returns the width and height of this element's bounds.
     */
    public IDimension size () {
        return _size;
    }

    /**
     * Writes the location of this element (relative to its parent) into the supplied point.
     * @return {@code loc} for convenience.
     */
    public Point location (Point loc) {
        return loc.set(x(), y());
    }

    /**
     * Writes the current bounds of this element into the supplied bounds.
     * @return {@code bounds} for convenience.
     */
    public Rectangle bounds (Rectangle bounds) {
        bounds.setBounds(x(), y(), _size.width, _size.height);
        return bounds;
    }

    /**
     * Returns the parent of this element, or null.
     */
    public Container<?> parent () {
        return _parent;
    }

    /**
     * Returns a signal that will dispatch when this element is added or removed from the
     * hierarchy. The emitted value is true if the element was just added to the hierarchy, false
     * if removed.
     */
    public SignalView<Boolean> hierarchyChanged () {
        if (_hierarchyChanged == null) _hierarchyChanged = Signal.create();
        return _hierarchyChanged;
    }

    /**
     * Returns the styles configured on this element.
     */
    public Styles styles () {
        return _styles;
    }

    /**
     * Configures the styles for this element. Any previously configured styles are overwritten.
     * @return this element for convenient call chaining.
     */
    public T setStyles (Styles styles) {
        _styles = styles;
        clearLayoutData();
        invalidate();
        return asT();
    }

    /**
     * Configures styles for this element (in the DEFAULT mode). Any previously configured styles
     * are overwritten.
     * @return this element for convenient call chaining.
     */
    public T setStyles (Style.Binding<?>... styles) {
        return setStyles(Styles.make(styles));
    }

    /**
     * Adds the supplied styles to this element. Where the new styles overlap with existing styles,
     * the new styles are preferred, but non-overlapping old styles are preserved.
     * @return this element for convenient call chaining.
     */
    public T addStyles (Styles styles) {
        _styles = _styles.merge(styles);
        clearLayoutData();
        invalidate();
        return asT();
    }

    /**
     * Adds the supplied styles to this element (in the DEFAULT mode). Where the new styles overlap
     * with existing styles, the new styles are preferred, but non-overlapping old styles are
     * preserved.
     * @return this element for convenient call chaining.
     */
    public T addStyles (Style.Binding<?>... styles) {
        return addStyles(Styles.make(styles));
    }

    /**
     * Returns {@code this} cast to {@code T}.
     */
    @SuppressWarnings({"unchecked", "cast"}) protected T asT () {
        return (T)this;
    }

    /**
     * Returns whether this element is enabled.
     */
    public boolean isEnabled () {
        return isSet(Flag.ENABLED);
    }

    /**
     * Enables or disables this element. Disabled elements are not interactive and are usually
     * rendered so as to communicate this state to the user.
     */
    public T setEnabled (boolean enabled) {
        if (enabled != isEnabled()) {
            set(Flag.ENABLED, enabled);
            clearLayoutData();
            invalidate();
        }
        return asT();
    }

    /**
     * Returns a slot which can be used to wire the enabled status of this element to a {@link
     * react.Signal} or {@link react.Value}.
     */
    public Slot<Boolean> enabledSlot () {
        return new Slot<Boolean>() {
            @Override public void onEmit (Boolean value) {
                setEnabled(value);
            }
        };
    }

    /**
     * Binds the enabledness of this element to the supplied value view. The current enabledness
     * will be adjusted to match the state of {@code isEnabled}.
     */
    public T bindEnabled (final ValueView<Boolean> isEnabledV) {
        return addBinding(new Binding(_bindings) {
            @Override public Closeable connect () {
                return isEnabledV.connectNotify(enabledSlot());
            }
            @Override public String toString () {
                return Element.this + ".bindEnabled";
            }
        });
    }

    /**
     * Returns whether this element is visible.
     */
    public boolean isVisible () {
        return isSet(Flag.VISIBLE);
    }

    /**
     * Configures whether this element is visible. An invisible element is not rendered and
     * consumes no space in a group.
     */
    public T setVisible (boolean visible) {
        if (visible != isVisible()) {
            set(Flag.VISIBLE, visible);
            layer.setVisible(visible);
            invalidate();
        }
        return asT();
    }

    /**
     * Returns a slot which can be used to wire the visible status of this element to a {@link
     * react.Signal} or {@link react.Value}.
     */
    public Slot<Boolean> visibleSlot () {
        return new Slot<Boolean>() {
            @Override public void onEmit (Boolean value) {
                setVisible(value);
            }
        };
    }

    /**
     * Binds the visibility of this element to the supplied value view. The current visibility will
     * be adjusted to match the state of {@code isVisible}.
     */
    public T bindVisible (final ValueView<Boolean> isVisibleV) {
        return addBinding(new Binding(_bindings) {
            public Closeable connect () {
                return isVisibleV.connectNotify(visibleSlot());
            }
            @Override public String toString () {
                return Element.this + ".bindVisible";
            }
        });
    }

    /**
     * Returns true only if this element and all its parents' {@link #isVisible()} return true.
     */
    public boolean isShowing () {
        Container<?> parent;
        return isVisible() && ((parent = parent()) != null) && parent.isShowing();
    }

    /**
     * Returns the layout constraint configured on this element, or null.
     */
    public Layout.Constraint constraint () {
        return _constraint;
    }

    /**
     * Configures the layout constraint on this element.
     * @return this element for call chaining.
     */
    public T setConstraint (Layout.Constraint constraint) {
        assert (_constraint == null || constraint == null) :
            "Cannot set constraint on element which already has a constraint. " +
            "Layout constraints cannot be automatically composed.";
        if (constraint != null) constraint.setElement(this);
        _constraint = constraint;
        invalidate();
        return asT();
    }

    /**
     * Returns true if this element is part of an interface heirarchy.
     */
    public boolean isAdded () {
        return root() != null;
    }

    /**
     * Returns the class of this element for use in computing its style. By default this is the
     * actual class, but you may wish to, for example, extend {@link Label} with some customization
     * and override this method to return {@code Label.class} so that your extension has the same
     * styles as Label applied to it.
     *
     * Concrete Element implementations should return the actual class instance instead of
     * getClass(). Returning getClass() means that further subclasses will lose all styles applied
     * to this implementation, probably unintentionally.
     */
    protected abstract Class<?> getStyleClass ();

    /**
     * Called when this element is added to a parent element. If the parent element is already
     * added to a hierarchy with a {@link Root}, this will immediately be followed by a call to
     * {@link #wasAdded}, otherwise the {@link #wasAdded} call will come later when the parent is
     * added to a root.
     */
    protected void wasParented (Container<?> parent) {
        _parent = parent;
    }

    /**
     * Called when this element is removed from its direct parent. If the element was removed from
     * a parent that was connected to a {@link Root}, a call to {@link #wasRemoved} will
     * immediately follow. Otherwise no call to {@link #wasRemoved} will be made.
     */
    protected void wasUnparented () {
        _parent = null;
    }

    /**
     * Called when this element (or its parent element) was added to an interface hierarchy
     * connected to a {@link Root}. The element will subsequently be validated and displayed
     * (assuming it's visible).
     */
    protected void wasAdded () {
        if (_hierarchyChanged != null) _hierarchyChanged.emit(Boolean.TRUE);
        invalidate();
        set(Flag.IS_ADDING, false);
        for (Binding b = _bindings; b != Binding.NONE; b = b.next) b.bind();
    }

    /**
     * Called when this element (or its parent element) was removed from the interface hierarchy.
     * Also, if the element was removed directly from its parent, then the layer is orphaned prior
     * to this call. Furthermore, if the element is being disposed (see
     * {@link Container.Mutable#dispose} and other methods), the disposal of the layer will occur
     * <b>after</b> this method returns and the {@link #willDispose()} method returns true. This
     * allows subclasses to manage resources as needed. <p><b>NOTE</b>: the base class method must
     * <b>always</b> be called for correct operation.</p>
     */
    protected void wasRemoved () {
        _bginst.clear();
        if (_hierarchyChanged != null) _hierarchyChanged.emit(Boolean.FALSE);
        set(Flag.IS_REMOVING, false);
        for (Binding b = _bindings; b != Binding.NONE; b = b.next) b.close();
    }

    /**
     * Returns true if the supplied, element-relative, coordinates are inside our bounds.
     */
    protected boolean contains (float x, float y) {
        return !(x < 0 || x > _size.width || y < 0 || y > _size.height);
    }

    /**
     * Returns whether this element is selected. This is only applicable for elements that maintain
     * a selected state, but is used when computing styles for all elements (it is assumed that an
     * element that maintains no selected state will always return false from this method).
     * Elements that do maintain a selected state should override this method and expose it as
     * public.
     */
    protected boolean isSelected () {
        return isSet(Flag.SELECTED);
    }

    /**
     * An element should call this method when it knows that it has changed in such a way that
     * requires it to recreate its visualization.
     */
    protected void invalidate () {
        // note that our preferred size and background are no longer valid
        _preferredSize = null;

        if (isSet(Flag.VALID)) {
            set(Flag.VALID, false);
            // invalidate our parent if we've got one
            if (_parent != null) {
                _parent.invalidate();
            }
        }
    }

    /**
     * Gets a new slot which will invoke {@link #invalidate()} when emitted.
     */
    protected UnitSlot invalidateSlot () {
        return invalidateSlot(false);
    }

    /**
     * Gets a new slot which will invoke {@link #invalidate()}.
     * @param styles if set, the slot will also call {@link #clearLayoutData()} when emitted
     */
    protected UnitSlot invalidateSlot (final boolean styles) {
        return new UnitSlot() {
            @Override public void onEmit () {
                invalidate();
                if (styles) clearLayoutData();
            }
        };
    }

    /**
     * Does whatever this element needs to validate itself. This may involve recomputing
     * visualizations, or laying out children, or anything else.
     */
    protected void validate () {
        if (!isSet(Flag.VALID)) {
            layout();
            set(Flag.VALID, true);
            wasValidated();
        }
    }

    /**
     * A hook method called after this element is validated. This chiefly exists for {@link Root}.
     */
    protected void wasValidated () {
        // nada by default
    }

    /**
     * Returns the root of this element's hierarchy, or null if the element is not currently added
     * to a hierarchy.
     */
    protected Root root () {
        return (_parent == null) ? null : _parent.root();
    }

    /**
     * Returns whether the specified flag is set.
     */
    protected boolean isSet (Flag flag) {
        return (flag.mask & _flags) != 0;
    }

    /**
     * Sets or clears the specified flag.
     */
    protected void set (Flag flag, boolean on) {
        if (on) {
            _flags |= flag.mask;
        } else {
            _flags &= ~flag.mask;
        }
    }

    /**
     * Returns this element's preferred size, potentially recomputing it if needed.
     *
     * @param hintX if non-zero, an indication that the element will be constrained in the x
     * direction to the specified width.
     * @param hintY if non-zero, an indication that the element will be constrained in the y
     * direction to the specified height.
     */
    protected IDimension preferredSize (float hintX, float hintY) {
        if (_preferredSize == null) _preferredSize = computeSize(hintX, hintY);
        return _preferredSize;
    }

    /**
     * Configures the location of this element, relative to its parent.
     */
    protected void setLocation (float x, float y) {
        layer.setTranslation(MathUtil.round(x), MathUtil.round(y));
    }

    /**
     * Configures the size of this widget.
     */
    protected T setSize (float width, float height) {
        boolean changed = _size.width != width || _size.height != height;
        _size.setSize(width, height);
        // if we have a cached preferred size and this size differs from it, we need to clear our
        // layout data as it may contain computations specific to our preferred size
        if (_preferredSize != null && !_size.equals(_preferredSize)) clearLayoutData();
        if (changed) invalidate();
        return asT();
    }

    /**
     * Resolves the value for the supplied style. See {@link Styles#resolveStyle} for the gritty
     * details.
     */
    protected <V> V resolveStyle (Style<V> style) {
        return Styles.resolveStyle(this, style);
    }

    /**
     * Recomputes this element's preferred size.
     *
     * @param hintX if non-zero, an indication that the element will be constrained in the x
     * direction to the specified width.
     * @param hintY if non-zero, an indication that the element will be constrained in the y
     * direction to the specified height.
     */
    protected Dimension computeSize (float hintX, float hintY) {
        // allow any layout constraint to adjust the layout hints
        if (_constraint != null) {
            hintX = _constraint.adjustHintX(hintX);
            hintY = _constraint.adjustHintY(hintY);
        }

        // create our layout data and ask it for our preferred size (accounting for our background
        // insets in the process)
        LayoutData ldata = _ldata = createLayoutData(hintX, hintY);
        Insets insets = ldata.bg.insets;
        Dimension size = computeSize(ldata, hintX - insets.width(), hintY - insets.height());
        insets.addTo(size);

        // allow any layout constraint to adjust the computed preferred size
        if (_constraint != null) _constraint.adjustPreferredSize(size, hintX, hintY);

        // round our preferred size up to the nearest whole number; if we allow it to remain
        // fractional, we can run into annoying layout problems where floating point rounding error
        // causes a tiny fraction of a pixel to be shaved off of the preferred size of a text
        // widget, causing it to wrap its text differently and hosing the layout
        size.width = MathUtil.iceil(size.width);
        size.height = MathUtil.iceil(size.height);

        return size;
    }

    /**
     * Computes this element's preferred size, delegating to {@link LayoutData#computeSize} by
     * default. This is called by {@link #computeSize(float,float)} after adjusting the hints based
     * on our layout constraints and insets. The returned dimension will be post facto adjusted to
     * include room for the element's insets (if any) and rounded up to the nearest whole pixel
     * value.
     */
    protected Dimension computeSize (LayoutData ldata, float hintX, float hintY) {
        return ldata.computeSize(hintX, hintY);
    }

    /**
     * Handles common element layout (background), then calls
     * {@link #layout(Element.LayoutData,float,float,float,float)} to do the actual layout.
     */
    protected void layout () {
        if (!isVisible()) return;

        float width = _size.width, height = _size.height;
        LayoutData ldata = (_ldata != null) ? _ldata : createLayoutData(width, height);

        // if we have a non-matching background, dispose it (note that if we don't want a bg, any
        // existing bg will necessarily be invalid)
        Background.Instance bginst = _bginst.get();
        boolean bgok = (bginst != null && bginst.owner() == ldata.bg &&
                        bginst.size.equals(_size));
        if (!bgok) _bginst.clear();
        // if we want a background and don't already have one, create it
        if (width > 0 && height > 0 && !bgok) {
            bginst = _bginst.set(ldata.bg.instantiate(_size));
            bginst.addTo(layer, 0, 0, 0);
        }

        // do our actual layout
        Insets insets = ldata.bg.insets;
        layout(ldata, insets.left(), insets.top(),
               width - insets.width(), height - insets.height());

        // finally clear our cached layout data
        clearLayoutData();
    }

    /**
     * Delegates layout to {@link LayoutData#layout} by default.
     */
    protected void layout (LayoutData ldata, float left, float top, float width, float height) {
        ldata.layout(left, top, width, height);
    }

    /**
     * Creates the layout data record used by this element. This record temporarily holds resolved
     * style information between the time that an element has its preferred size computed, and the
     * time that the element is subsequently laid out. Note: {@code hintX} and {@code hintY} <em>do
     * not</em> yet have the background insets subtracted from them, because the creation of the
     * LayoutData is what resolves the background in the first place.
     */
    protected LayoutData createLayoutData (float hintX, float hintY) {
        return new LayoutData();
    }

    /**
     * Clears out cached layout data. This can be called by methods that change the configuration
     * of the element when they know it will render pre-computed layout info invalid.
     */
    protected void clearLayoutData () {
        _ldata = null;
    }

    /**
     * Creates the layer to be used by this element. Subclasses may override to use a clipped one.
     */
    protected GroupLayer createLayer () {
        return new GroupLayer() {
            @Override public String name () { return Element.this + " layer"; }
        };
    }

    /**
     * Tests if this element is about to be disposed. Elements are disposed via a call to one of
     * the "dispose" methods such as {@link Container.Mutable#dispose(Element)}. This allows
     * subclasses to manage resources appropriately during their implementation of {@link
     * #wasRemoved}, for example clearing a child cache. <p>NOTE: at the expense of slight semantic
     * dissonance, the flag is not cleared after disposal.</p>
     */
    protected boolean willDispose () {
        return isSet(Flag.WILL_DISPOSE);
    }

    /**
     * Tests if this element is scheduled to be removed from a root hierarchy.
     */
    protected final boolean willRemove () {
        return isSet(Flag.IS_REMOVING) || (_parent != null && _parent.willRemove());
    }

    /**
     * Tests if this element is scheduled to be added to a root hierarchy.
     */
    protected final boolean willAdd () {
        return isSet(Flag.IS_ADDING) || (_parent != null && _parent.willAdd());
    }

    protected T addBinding (Binding binding) {
        _bindings = binding;
        if (isAdded()) binding.bind();
        return asT();
    }

    /** Resolves style and other information needed to layout this element. */
    protected class LayoutData {
        /** This element's background style. */
        public final Background bg = resolveStyle(Style.BACKGROUND);

        /**
         * Computes this element's preferred size, given the supplied hints. The background insets
         * will be automatically added to the returned size.
         */
        public Dimension computeSize (float hintX, float hintY) {
            return new Dimension(0, 0);
        }

        /**
         * Rebuilds this element's visualization. Called when this element's size has changed. In
         * the case of groups, this will relayout its children, in the case of widgets, this will
         * rerender the widget.
         */
        public void layout (float left, float top, float width, float height) {
            // noop!
        }
    }

    /** Ways in which a preferred and an original dimension can be "taken" to produce a result.
     * The name is supposed to be readable in context and compact, for example
     * {@code new SizableLayoutData(...).forWidth(Take.MAX).forHeight(Take.MIN, 200)}. */
    protected enum Take
    {
        /** Uses the maximum of the preferred size and original. */
        MAX {
            @Override public float apply (float preferred, float original) {
                return Math.max(preferred, original);
            }
        },
        /** Uses the minimum of the preferred size and original. */
        MIN {
            @Override public float apply (float preferred, float original) {
                return Math.min(preferred, original);
            }
        },
        /** Uses the preferred size if non-zero, otherwise the original. This is the default. */
        PREFERRED_IF_SET {
            @Override public float apply (float preferred, float original) {
                return preferred == 0 ? original : preferred;
            }
        };

        public abstract float apply (float preferred, float original);
    }

    /**
     * A layout data that will delegate to another layout data instance, but alter the size
     * computation to optionally use fixed values.
     */
    protected class SizableLayoutData extends LayoutData {
        /**
         * Creates a new layout with the given delegates and size.
         * @param layoutDelegate the delegate to use during layout. May be null if the element
         * has no layout
         * @param sizeDelegate the delegate to use during size computation. May be null if the
         * size will be completely specified by {@code prefSize}
         * @param prefSize overrides the size computation. The width and/or height may be zero,
         * which indicates the {@code sizeDelegate}'s result should be used for that axis. Passing
         * {@code null} is equivalent to passing a 0x0 dimension
         */
        public SizableLayoutData (LayoutData layoutDelegate, LayoutData sizeDelegate,
                                  IDimension prefSize) {
            this.layoutDelegate = layoutDelegate;
            this.sizeDelegate = sizeDelegate;
            if (prefSize != null) {
                prefWidth = prefSize.width();
                prefHeight = prefSize.height();
            } else {
                prefWidth = prefHeight = 0;
            }
        }

        /**
         * Creates a new layout that will defer to the given delegate for layout and size. This is
         * equivalent to {@code SizableLayoutData(delegate, delegate, prefSize)}.
         * @see #SizableLayoutData(LayoutData, LayoutData, IDimension)
         */
        public SizableLayoutData (LayoutData delegate, IDimension prefSize) {
            this.layoutDelegate = delegate;
            this.sizeDelegate = delegate;
            if (prefSize != null) {
                prefWidth = prefSize.width();
                prefHeight = prefSize.height();
            } else {
                prefWidth = prefHeight = 0;
            }
        }

        /**
         * Sets the way in which widths are combined to calculate the resulting preferred size.
         * For example, {@code new SizeableLayoutData(...).forWidth(Take.MAX)}.
         */
        public SizableLayoutData forWidth (Take fn) {
            widthFn = fn;
            return this;
        }

        /**
         * Sets the preferred width and how it should be combined with the delegate's preferred
         * width. For example, {@code new SizeableLayoutData(...).forWidth(Take.MAX, 250)}.
         */
        public SizableLayoutData forWidth (Take fn, float pref) {
            widthFn = fn;
            prefWidth = pref;
            return this;
        }

        /**
         * Sets the way in which heights are combined to calculate the resulting preferred size.
         * For example, {@code new SizeableLayoutData(...).forHeight(Take.MAX)}.
         */
        public SizableLayoutData forHeight (Take fn) {
            heightFn = fn;
            return this;
        }

        /**
         * Sets the preferred height and how it should be combined with the delegate's preferred
         * height. For example, {@code new SizeableLayoutData(...).forHeight(Take.MAX, 250)}.
         */
        public SizableLayoutData forHeight (Take fn, float pref) {
            heightFn = fn;
            prefHeight = pref;
            return this;
        }

        @Override public Dimension computeSize (float hintX, float hintY) {
            // hint the delegate with our preferred width or height or both,
            // then swap in our preferred function on that (min, max, or subclass)
            return adjustSize(sizeDelegate == null ? new Dimension(prefWidth, prefHeight) :
                sizeDelegate.computeSize(resolveHintX(hintX), resolveHintY(hintY)));
        }

        @Override public void layout (float left, float top, float width, float height) {
            if (layoutDelegate != null) layoutDelegate.layout(left, top, width, height);
        }

        /**
         * Refines the given x hint for the delegate to consume. By default uses our configured
         * preferred width if not zero, otherwise the passed-in x hint.
         */
        protected float resolveHintX (float hintX) {
            return select(prefWidth, hintX);
        }

        /**
         * Refines the given y hint for the delegate to consume. By default uses our configured
         * preferred height if not zero, otherwise the passed-in y hint.
         */
        protected float resolveHintY (float hintY) {
            return select(prefHeight, hintY);
        }

        /**
         * Adjusts the dimension computed by the delegate to get the final preferred size. By
         * default, uses the previously configured {@link Take} values.
         */
        protected Dimension adjustSize (Dimension dim) {
            dim.width = widthFn.apply(prefWidth, dim.width);
            dim.height = heightFn.apply(prefHeight, dim.height);
            return dim;
        }

        protected float select (float pref, float base) {
            return pref == 0 ? base : pref;
        }

        protected final LayoutData layoutDelegate;
        protected final LayoutData sizeDelegate;
        protected float prefWidth, prefHeight;
        protected Take widthFn = Take.PREFERRED_IF_SET, heightFn = Take.PREFERRED_IF_SET;
    }

    /** Used to track bindings to reactive values, which are established when this element is added
      * to the UI hierarchy and closed when the element is removed. This allows us to provide
      * bindFoo() methods which neither leak connections to reactive values whose lifetimes may
      * exceed that of the element that is displaying them, nor burdens the caller with thinking
      * about and managing this. */
    protected static abstract class Binding {
        public static final Binding NONE = new Binding(null) {
            public Closeable connect () { return Closeable.Util.NOOP; }
        };

        public final Binding next;
        public Binding (Binding next) {
            this.next = next;
        }

        public abstract Closeable connect ();

        public void bind () {
            assert _conn == Closeable.Util.NOOP : "Already bound: " + this;
            _conn = connect();
        }
        public void close () {
            _conn = Closeable.Util.close(_conn);
        }

        protected Closeable _conn = Closeable.Util.NOOP;
    }

    protected int _flags = Flag.VISIBLE.mask | Flag.ENABLED.mask;
    protected Container<?> _parent;
    protected Dimension _preferredSize;
    protected Dimension _size = new Dimension();
    protected Styles _styles = Styles.none();
    protected Layout.Constraint _constraint;
    protected Signal<Boolean> _hierarchyChanged;
    protected Binding _bindings = Binding.NONE;

    protected LayoutData _ldata;
    protected final Ref<Background.Instance> _bginst = Ref.<Background.Instance>create(null);

    protected static enum Flag {
        VALID(1 << 0), ENABLED(1 << 1), VISIBLE(1 << 2), SELECTED(1 << 3), WILL_DISPOSE(1 << 4),
        HIT_DESCEND(1 << 5), HIT_ABSORB(1 << 6), IS_REMOVING(1 << 7), IS_ADDING(1 << 8);

        public final int mask;

        Flag (int mask) {
            this.mask = mask;
        }
    }
}
