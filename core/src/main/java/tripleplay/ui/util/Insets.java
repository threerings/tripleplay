//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;

import pythagoras.f.Dimension;

/** Corresponds to the distances that some rectangular object's edges will be offset when,
 * for example, it is contained in another rectangle. */
public class Insets
{
    /** Read-only instance with zero for all edges. */
    public static Insets ZERO = new Insets(0, 0, 0, 0);

    /** Returns a read-only instance with all edges set to the same value. */
    public static Insets uniform (float val) {
        return new Insets(val, val, val, val);
    }

    /** Returns a read-only instance with left and right set to one value and top and bottom set
     * to another. */
    public static Insets symmetric (float horiz, float vert) {
        return new Insets(vert, horiz, vert, horiz);
    }

    /** Insets with changeable values. */
    public static class Mutable extends Insets {
        private Mutable (Insets i) {
            super(i.top(), i.right(), i.bottom(), i.left());
        }

        @Override public Mutable mutable () {
            return this;
        }

        /** Adds the given insets to these. Returns {@code this} for chaining. */
        public Mutable add (Insets insets) {
            _top += insets._top;
            _right += insets._right;
            _bottom += insets._bottom;
            _left += insets._left;
            return this;
        }

        /** Sets the top edge and returns {@code this} for chaining. */
        public Mutable top (float newTop) {
            _top = newTop;
            return this;
        }

        /** Sets the right edge and returns {@code this} for chaining. */
        public Mutable right (float newRight) {
            _right = newRight;
            return this;
        }

        /** Sets the bottom edge and returns {@code this} for chaining. */
        public Mutable bottom (float newBottom) {
            _bottom = newBottom;
            return this;
        }

        /** Sets the left edge and returns {@code this} for chaining. */
        public Mutable left (float newLeft) {
            _left = newLeft;
            return this;
        }
    }

    /** Gets the top inset. */
    public float top () {
        return _top;
    }

    /** Gets the right inset. */
    public float right () {
        return _right;
    }

    /** Gets the bottom inset. */
    public float bottom () {
        return _bottom;
    }

    /** Gets the left inset. */
    public float left () {
        return _left;
    }

    /** Creates new insets. */
    public Insets (float top, float right, float bottom, float left) {
        _top = top;
        _right = right;
        _bottom = bottom;
        _left = left;
    }

    /** Returns the total adjustment to width. */
    public float width () {
        return _left + _right;
    }

    /** Returns this total adjustment to height. */
    public float height () {
        return _top + _bottom;
    }

    /** Adds these insets to the supplied dimensions. Returns {@code size} for chaining. */
    public Dimension addTo (Dimension size) {
        size.width += width();
        size.height += height();
        return size;
    }

    /** Adds these insets from the supplied dimensions. Returns {@code size} for chaining. */
    public Dimension subtractFrom (Dimension size) {
        size.width -= width();
        size.height -= height();
        return size;
    }

    /** Gets or creates a copy of these insets that can be mutated. Note, if storing an instance,
     * the caller is expected to assign to the return value here in case a new object is
     * allocated. */
    public Mutable mutable () {
        return new Mutable(this);
    }

    /** Returns a new instance which is the supplied adjustments added to these insets. */
    public Insets adjust (float dtop, float dright, float dbottom, float dleft) {
        return new Insets(_top + dtop, _right + dright, _bottom + dbottom, _left + dleft);
    }

    /** The amount to inset an edge. */
    protected float _top, _right, _bottom, _left;
}

