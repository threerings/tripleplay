//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;

import tripleplay.ui.Element;

/**
 * Supplies elements. The means of achieving this depends on the situation. Common cases are
 * to provide a fixed instance or to construct a new element for the caller to cache. Particular
 * attention is paid to ownership and orderly resource destruction.
 */
public abstract class Supplier
{
    /**
     * Creates a supplier that will return a previously created element the first time and null
     * thereafter. If the element is still present when destroy is called, the element's layer will
     * be destroyed.
     */
    public static Supplier auto (final Element<?> elem) {
        return new Supplier() {
            Element<?> element = elem;
            @Override public Element<?> get () {
                Element<?> ret = element;
                element = null;
                return ret;
            }
            @Override public void destroy () {
                if (element != null) element.layer.destroy();
                element = null;
            }
        };
    }

    /**
     * Gets the element. Ownership of the element's resources (its layer) must also be transferred.
     * For example, if you don't add the element to any hierarchy, you need to call its {@code
     * layer.destroy} later.
     */
    public abstract Element<?> get ();

    /**
     * Destroys resources associated with the supplier instance. The base class implementation
     * does nothing.
     */
    public void destroy () {}
}
