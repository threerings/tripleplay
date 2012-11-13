//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;

import tripleplay.ui.Element;

/**
 * Supplies elements. The means of achieving this depends on the situation. Common cases are
 * to provide a fixed instance or to construct a new element for the caller to cache.
 */
public abstract class Supplier
{
    /**
     * Creates a supplier that will always return a previously created element.
     */
    public static Supplier ofInstance (final Element<?> elem) {
        return new Supplier() {
            @Override public Element<?> get () {
                return elem;
            }
        };
    }

    /**
     * Gets the element.
     */
    public abstract Element<?> get ();
}
