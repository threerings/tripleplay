//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import java.util.HashMap;
import java.util.Map;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.IPoint;
import pythagoras.f.IRectangle;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;

import forplay.core.Asserts;
import forplay.core.Layer;
import forplay.core.Transform;

/**
 * The root of the interface element hierarchy. See {@link Widget} for the root of all interactive
 * elements, and {@link Container} for the root of all grouping elements.
 */
public abstract class Element
{
    /** Defines the states that may be assumed by an element. */
    public static enum State {
        DEFAULT, DISABLED, DOWN;
    }

    /**
     * Returns this element's x offset relative to its parent.
     */
    public float x () {
        return layer().transform().tx();
    }

    /**
     * Returns this element's y offset relative to its parent.
     */
    public float y () {
        return layer().transform().ty();
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
        Transform transform = layer().transform();
        return loc.set(transform.tx(), transform.ty());
    }

    /**
     * Writes the current bounds of this element into the supplied bounds.
     * @return {@code bounds} for convenience.
     */
    public IRectangle bounds (Rectangle bounds) {
        Transform transform = layer().transform();
        bounds.setBounds(transform.tx(), transform.ty(), _size.width, _size.height);
        return bounds;
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
    public void setEnabled (boolean enabled) {
        if (enabled != isEnabled()) {
            set(Flag.ENABLED, enabled);
            invalidate();
        }
    }

    /**
     * Returns whether this element is visible.
     */
    public boolean isVisible () {
        return isSet(Flag.VISIBLE);
    }

    /**
     * Configures whether this element is visible. An invisible element is not rendered and
     * consumes no space in a container.
     */
    public void setVisible (boolean visible) {
        if (visible != isVisible()) {
            set(Flag.VISIBLE, visible);
            invalidate();
        }
    }

    /**
     * Returns true if this element is part of an interface heirarchy.
     */
    public boolean isAdded () {
        return getRoot() != null;
    }

    /**
     * Returns the appropriate value for the specified style when the widget is in the specified
     * state. If no value is configured for this element, the default will be returned (for
     * non-inherited styles), or this element's parent will be consulted (for inherited styles).
     * This method may not be called before this element has been added to the interface hierarchy.
     */
    public <V> V getStyle (Style<V> style, State state) {
        Asserts.checkState(_parent != null, "Styles may not be queried before an " +
                           "element has been added to the interface hierarchy.");
        StyleKey<V> key = new StyleKey<V>(style, state);
        @SuppressWarnings("unchecked") V value = (_styles == null) ? null : (V)_styles.get(key);
        return (value != null) ? value :
            (style.inherited ? _parent.getStyle(style, state) : style.getDefault(state));
    }

    /**
     * Configures the specified style for this element. This may be called before the element has
     * been added to the interface hierarchy.
     */
    public <V> void setStyle (Style<V> style, V value, State state) {
        if (_styles == null) {
            _styles = new HashMap<StyleKey<?>, Object>();
        }
        _styles.put(new StyleKey<V>(style, state), value);
    }

    /**
     * Clears out the specified style for this element. The element will revert to using the
     * default or inherited value for that style. This may be called before the element has been
     * added to the interface hierarchy.
     */
    public <V> void clearStyle (Style<V> style, State state) {
        if (_styles != null) {
            _styles.remove(new StyleKey<V>(style, state));
        }
    }

    /**
     * Computes the style state of this element based on its flags.
     */
    protected State state () {
        return isSet(Flag.ENABLED) ? State.DEFAULT : State.DISABLED;
    }

    /**
     * An element should call this method when it knows that it has changed in such a way that
     * requires it to recreate its visualization.
     */
    protected void invalidate () {
        // note that our preferred size is no longer valid
        _preferredSize = null;
        // invalidate our parent if we've got one
        if (_parent != null) {
            _parent.invalidate();
        }
    }

    /**
     * Returns the root of this element's hierarchy, or null if the element is not currently added
     * to a hierarchy.
     */
    protected Root getRoot () {
        return (_parent == null) ? null : _parent.getRoot();
    }

    /**
     * Returns whether the specified state flag is set.
     */
    protected boolean isSet (Flag flag) {
        return (flag.mask & _flags) != 0;
    }

    /**
     * Sets or clears the specified state flag.
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
    protected IDimension getPreferredSize (float hintX, float hintY) {
        if (_preferredSize == null) {
            computeSize(hintX, hintY, _preferredSize = new Dimension());
        }
        return _preferredSize;
    }

    /**
     * Configures the size of this widget, potentially triggering a regeneration of its
     * visualization.
     */
    protected abstract void setSize (float width, float height);

    /**
     * Recomputes this element's preferred size.
     *
     * @param hintX if non-zero, an indication that the element will be constrained in the x
     * direction to the specified width.
     * @param hintY if non-zero, an indication that the element will be constrained in the y
     * direction to the specified height.
     * @param into the preferred size is written into this parameter.
     */
    protected abstract void computeSize (float hintX, float hintY, Dimension into);

    /**
     * Returns the layer associated with this interface element.
     */
    protected abstract Layer layer ();

    protected static class StyleKey<V> {
        public Style<V> style;
        public State state;

        public StyleKey (Style<V> style, State state) {
            this.style = style;
            this.state = state;
        }

        @Override public boolean equals (Object other) {
            StyleKey<?> okey = (StyleKey<?>)other;
            return (okey.style == style) && (okey.state == state);
        }

        @Override public int hashCode () {
            return style.hashCode() ^ state.hashCode();
        }
    }

    protected int _flags;
    protected Container _parent;
    protected Dimension _preferredSize;
    protected Dimension _size = new Dimension();
    protected Map<StyleKey<?>, Object> _styles; // lazily initialized to save memory

    protected static enum Flag {
        ENABLED(1 << 0), VISIBLE(1 << 1), HOVERED(1 << 2);

        public final int mask;

        Flag (int mask) {
            this.mask = mask;
        }
    };
}
