//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.List;
import java.util.Map;

import pythagoras.f.Dimension;

/**
 * Defines the interface to layouts, which implement a particular layout policy.
 */
public abstract class Layout
{
    /** An abstract base class for all layout constraints. */
    public static interface Constraint {}

    /**
     * Creates a group with this layout. This is useful after a chain of method calls that
     * configures the layout, e.g.: {@code GroupLayout.vertical().alignTop().gap(10).toGroup()}.
     *
     * @param elems elements to add to the newly created group.
     */
    public Group toGroup (Element... elems) {
        Group g = new Group(this);
        for (Element elem : elems) {
            g.add(elem);
        }
        return g;
    }

    /**
     * Computes and returns the size needed to arrange the supplied children (with the specified
     * constraints) according to their preferred size, given the specified x and y size hints.
     * Neither {@code elems} nor {@code constraints} should be mutated.
     */
    public abstract Dimension computeSize (
        List<Element> elems, Map<Element, Layout.Constraint> constraints,
        float hintX, float hintY);

    /**
     * Lays out the supplied elements (according to the specified constraints) into a region of the
     * specified dimensions. Neither {@code elems} nor {@code constraints} should be mutated.
     */
    public abstract void layout (
        List<Element> elems, Map<Element, Layout.Constraint> constraints,
        float left, float top, float width, float height);
}
