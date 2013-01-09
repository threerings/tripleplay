//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;

import tripleplay.ui.Element;

/**
 * Defines a method that applies an operation to an element.
 * @param <T> the leaf type of Element.
 */
public abstract class ElementOp<T extends Element<?>>
{
    /**
     * Returns an element operation that enables or disables its elements. Usage:
     * <pre>{@code
     *     Hierarchy.of(elem).apply(ElementOp.setEnabled(false));
     * }</pre>
     */
    public static ElementOp<Element<?>> setEnabled (final boolean enabled) {
        return new ElementOp<Element<?>>() {
            public void apply (Element<?> elem) {
                elem.setEnabled(enabled);
            }
        };
    }

    /**
     * Applies an arbitrary operation to the given element.
     */
    public abstract void apply (T elem);

    /**
     * Iterates the given elements and applies this operation to each.
     */
    public final void applyToEach (Iterable<? extends T> elems)
    {
        for (T elem : elems) {
            apply(elem);
        }
    }
}
