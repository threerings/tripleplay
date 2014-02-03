//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;

import java.util.Iterator;

import tripleplay.ui.Container;
import tripleplay.ui.Element;

/**
 * A view for the hierarchical structure of an {@link Element}.
 */
public class Hierarchy
{
    /**
     * Iterates over the ancestors of an element. See {@link #ancestors()}.
     */
    public static class Ancestors
        implements Iterator<Element<?>>
    {
        public Element<?> current;

        public Ancestors (Element<?> elem) {
            if (elem == null) {
                throw new IllegalArgumentException();
            }
            current = elem;
        }

        @Override public boolean hasNext () {
            return current != null;
        }

        @Override public Element<?> next () {
            Element<?> next = current;
            if (!hasNext()) {
                throw new IllegalStateException();
            }
            current = current.parent();
            return next;
        }

        @Override public void remove () {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Create a new view of the given element.
     */
    public static Hierarchy of (Element<?> elem) {
        return new Hierarchy(elem);
    }

    /** The element that is the focus of this view. */
    public final Element<?> elem;

    /**
     * Creates a new view focused on the given element.
     */
    public Hierarchy (Element<?> elem) {
        this.elem = elem;
    }

    /**
     * Tests if the given element is a proper descendant contained in this hierarchy, or is the
     * root.
     */
    public boolean hasDescendant (Element<?> descendant) {
        if (descendant == elem) return true;
        if (descendant == null) return false;
        return hasDescendant(descendant.parent());
    }

    /**
     * Returns an object to iterate over the ancestors of this hierarchy, including the root.
     */
    public Iterable<Element<?>> ancestors () {
        return new Iterable<Element<?>> () {
            @Override public Iterator<Element<?>> iterator () {
                return new Ancestors(elem);
            }
        };
    }

    /**
     * Applies the given operation to the root of the hierarchy and to every proper descendant.
     */
    public Hierarchy apply (ElementOp<Element<?>> op) {
        forEachDescendant(elem, op);
        return this;
    }

    protected static void forEachDescendant (Element<?> root, ElementOp<Element<?>> op) {
        op.apply(root);
        if (root instanceof Container<?>) {
            Container<?> es = (Container<?>)root;
            for (int ii = 0, ll = es.childCount(); ii < ll; ++ii) {
                forEachDescendant(es.childAt(ii), op);
            }
        }
    }
}
