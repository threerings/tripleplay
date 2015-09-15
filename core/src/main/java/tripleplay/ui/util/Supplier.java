//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;

import react.Closeable;

import tripleplay.ui.Element;

/**
 * Supplies elements. The means of achieving this depends on the situation. Common cases are to
 * provide a fixed instance or to construct a new element for the caller to cache. Particular
 * attention is paid to ownership and orderly resource disposal.
 */
public abstract class Supplier implements Closeable
{
    /**
     * Creates a supplier that will return a previously created element the first time and null
     * thereafter. If the element is still present when dispose is called, the element's layer will
     * be disposeed.
     */
    public static Supplier auto (final Element<?> elem) {
        return new Supplier() {
            Element<?> element = elem;
            @Override public Element<?> get () {
                Element<?> ret = element;
                element = null;
                return ret;
            }
            @Override public void close () {
                if (element != null) element.layer.close();
                element = null;
            }
        };
    }

    /**
     * Creates a supplier that wraps another supplier and on dispose also disposes the created
     * element, if it implements {@link Closeable}.
     */
    public static Supplier withDispose (final Supplier other) {
        return new Supplier() {
            Element<?> created;
            @Override public Element<?> get () {
                return created = other.get();
            }
            @Override public void close () {
                other.close();
                if (created instanceof Closeable) {
                    ((Closeable)created).close();
                }
            }
        };
    }

    /**
     * Gets the element. Ownership of the element's resources (its layer) must also be transferred.
     * For example, if you don't add the element to any hierarchy, you need to call its {@code
     * layer.close} later.
     */
    public abstract Element<?> get ();

    /**
     * Disposes resources associated with the supplier instance. The base class implementation does
     * nothing.
     */
    @Override public void close () {}
}
