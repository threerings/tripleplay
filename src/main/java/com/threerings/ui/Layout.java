//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import java.util.List;
import java.util.Map;

import pythagoras.f.Dimension;

/**
 * Defines the interface to layouts, which implement a particular layout policy.
 */
public abstract class Layout
{
    /** An abstract base class for all layout constraints. */
    public static abstract class Constraint {}

    /**
     * Creates a group with this layout. This is useful after a chain of method calls that
     * configures the layout, e.g.: {@code GroupLayout.vertical().alignTop().gap(10).toGroup()}.
     */
    public Group toGroup () {
        return new Group(this);
    }

    /**
     * Computes the size needed to arrange the supplied children (with the specified constraints)
     * according to their preferred size, given the specified x and y size hints. Stores the
     * results in {@code into}. Neither {@code elems} nor {@code constraints} should be mutated.
     */
    public abstract void computeSize (
        List<Element> elems, Map<Element, Layout.Constraint> constraints,
        float hintX, float hintY, Dimension into);

    /**
     * Lays out the supplied elements (according to the specified constraints) into a region of the
     * specified dimensions. Neither {@code elems} nor {@code constraints} should be mutated.
     */
    public abstract void layout (
        List<Element> elems, Map<Element, Layout.Constraint> constraints,
        float width, float height);
}
