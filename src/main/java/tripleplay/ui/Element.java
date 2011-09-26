//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.IPoint;
import pythagoras.f.IRectangle;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;
import pythagoras.f.Transform;

import react.Slot;

import playn.core.PlayN;
import playn.core.GroupLayer;

/**
 * The root of the interface element hierarchy. See {@link Widget} for the root of all interactive
 * elements, and {@link Elements} for the root of all grouping elements.
 *
 * @param T used as a "self" type; when subclassing {@code Element}, T must be the type of the
 * subclass.
 */
public abstract class Element<T extends Element<T>>
{
    /** The layer associated with this element. */
    public final GroupLayer layer = PlayN.graphics().createGroupLayer();

    /**
     * Returns this element's x offset relative to its parent.
     */
    public float x () {
        return layer.transform().tx();
    }

    /**
     * Returns this element's y offset relative to its parent.
     */
    public float y () {
        return layer.transform().ty();
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
    public IPoint location (Point loc) {
        Transform transform = layer.transform();
        return loc.set(transform.tx(), transform.ty());
    }

    /**
     * Writes the current bounds of this element into the supplied bounds.
     * @return {@code bounds} for convenience.
     */
    public IRectangle bounds (Rectangle bounds) {
        Transform transform = layer.transform();
        bounds.setBounds(transform.tx(), transform.ty(), _size.width, _size.height);
        return bounds;
    }

    /**
     * Returns the parent of this element, or null.
     */
    public Elements<?> parent () {
        return _parent;
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
     * Returns <code>this</code> cast to <code>T</code>.
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
            public void onEmit (Boolean value) {
                setEnabled(value);
            }
        };
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
            public void onEmit (Boolean value) {
                setVisible(value);
            }
        };
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
        _constraint = constraint;
        return asT();
    }

    /**
     * Returns true if this element is part of an interface heirarchy.
     */
    public boolean isAdded () {
        return root() != null;
    }

    /**
     * Called when this element (or its parent element) was added to the interface hierarchy.
     */
    protected void wasAdded (Elements<?> parent) {
        _parent = parent;
    }

    /**
     * Called when this element (or its parent element) was removed from the interface hierarchy.
     */
    protected void wasRemoved () {
        _parent = null;
    }

    /**
     * Returns true if the supplied, element-relative, coordinates are inside our bounds.
     */
    protected boolean contains (float x, float y) {
        return !(x < 0 || x > _size.width || y < 0 || y > _size.height);
    }

    /**
     * Used to determine whether a point falls in this element's bounds.
     * @param point the point to be tested in this element's parent's coordinate system.
     * @return the leaf-most element that contains the supplied point or null if neither this
     * element, nor its children contain the point. Also {@code point} is updated to contain the
     * hit-element-relative coordinates in the event of a hit.
     */
    protected Element<?> hitTest (Point point) {
        // transform the point into our coordinate system
        point = layer.transform().inverseTransform(point, point);
        float x = point.x + layer.originX(), y = point.y + layer.originY();
        // check whether it falls within our bounds
        if (!contains(x, y)) return null;
        // if we're the hit component, update the supplied point
        point.set(x, y);
        return this;
    }

    /**
     * Called when the a touch/drag is started within the bounds of this component.
     */
    protected void onPointerStart (float x, float y) {
    }

    /**
     * Called when a touch that started within the bounds of this component is dragged. The drag
     * may progress outside the bounds of this component, but the events will still be dispatched
     * to this component until the touch is released.
     */
    protected void onPointerDrag (float x, float y) {
    }

    /**
     * Called when a touch that started within the bounds of this component is released. The
     * coordinates may be outside the bounds of this component, but the touch in question started
     * inside this component's bounds.
     */
    protected void onPointerEnd (float x, float y) {
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
        if (isSet(Flag.VALID)) {
            set(Flag.VALID, false);
            // note that our preferred size and background are no longer valid
            _preferredSize = null;
            // invalidate our parent if we've got one
            if (_parent != null) {
                _parent.invalidate();
            }
        }
    }

    /**
     * Does whatever this element needs to validate itself. This may involve recomputing
     * visualizations, or laying out children, or anything else.
     */
    protected void validate () {
        if (!isSet(Flag.VALID)) {
            layout();
            set(Flag.VALID, true);
        }
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
        layer.transform().setTranslation(x, y);
    }

    /**
     * Configures the size of this widget.
     */
    protected T setSize (float width, float height) {
        if (_size.width == width && _size.height == height) return asT(); // NOOP
        _size.setSize(width, height);
        // if we have a cached preferred size and this size differs from it, we need to clear our
        // layout data as it may contain computations specific to our preferred size
        if (_preferredSize != null && !_size.equals(_preferredSize)) clearLayoutData();
        invalidate();
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
    protected abstract Dimension computeSize (float hintX, float hintY);

    /**
     * Rebuilds this element's visualization. Called when this element's size has changed. In the
     * case of groups, this will relayout its children, in the case of widgets, this will rerender
     * the widget.
     */
    protected abstract void layout ();

    /**
     * Clears out cached layout data. This can be called by methods that change the configuration
     * of the widget when they know it will render pre-computed layout info invalid. Elements that
     * cache layout data should override this method and clear their cached layout.
     */
    protected void clearLayoutData () {
    }

    protected int _flags = Flag.VISIBLE.mask | Flag.ENABLED.mask;
    protected Elements<?> _parent;
    protected Dimension _preferredSize;
    protected Dimension _size = new Dimension();
    protected Styles _styles = Styles.none();
    protected Layout.Constraint _constraint;

    protected static enum Flag {
        VALID(1 << 0), ENABLED(1 << 1), VISIBLE(1 << 2), SELECTED(1 << 3);

        public final int mask;

        Flag (int mask) {
            this.mask = mask;
        }
    };
}
