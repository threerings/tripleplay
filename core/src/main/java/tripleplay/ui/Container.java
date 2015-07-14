//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.Iterator;

import pythagoras.f.Dimension;

/**
 * A shared base class for elements which contain other elements.
 */
public abstract class Container<T extends Container<T>> extends Element<T>
    implements Iterable<Element<?>>
{
    /**
     * Removes and optionally destroys the given element from its parent, if the parent is a
     * mutable container. This is set apart as a utility method since it is not desirable to have
     * on all containers, but is frequently useful to have. The caller is willing to accept the
     * class cast exception if the parent container is not mutable. Does nothing if the element
     * has no parent.
     * @param element the element to remove
     * @param destroy whether to also destroy the element
     * @return true if the element had a parent and it was removed or destroyed
     */
    public static boolean removeFromParent (Element<?> element, boolean destroy) {
        if (element.parent() == null) return false;
        Mutable<?> parent = (Mutable<?>)element.parent();
        if (destroy) parent.destroy(element);
        else parent.remove(element);
        return true;
    }

    /** A container that allows mutation (adding and removal) of its children. */
    public static abstract class Mutable<T extends Mutable<T>> extends Container<T> {
        /** Removes the specified child from this container. */
        public abstract void remove (Element<?> child);

        /** Removes the child at the specified index from this container. */
        public abstract void removeAt (int index);

        /** Removes all children from this container. */
        public abstract void removeAll ();

        /** Removes and destroys the specified child. */
        public abstract void destroy (Element<?> child);

        /** Removes and destroys the child at the specified index. */
        public abstract void destroyAt (int index);

        /** Removes and destroys all children from this container. */
        public abstract void destroyAll ();
    }

    /**
     * Returns the stylesheet associated with this container, or null. Styles are resolved by
     * searching up the container hierarchy. Only {@link Container} actually provides styles to its
     * children.
     */
    public abstract Stylesheet stylesheet ();

    /** Returns the number of children contained by this container. */
    public abstract int childCount ();

    /*** Returns the child at the specified index. */
    public abstract Element<?> childAt (int index);

    /** Returns an unmodifiable iterator over the children of this Container.  */
    public abstract Iterator<Element<?>> iterator ();

    protected void didAdd (Element<?> child) {
        layer.add(child.layer);
        child.wasParented(this);
        // bar n-child from being added twice
        if (isAdded() && !child.willAdd()) {
            child.set(Flag.IS_ADDING, true);
            child.wasAdded();
        }
    }

    protected void didRemove (Element<?> child, boolean dispose) {
        if (dispose) child.set(Flag.WILL_DISPOSE, true);
        layer.remove(child.layer);
        boolean needsRemove = child.willRemove(); // early removal of a scheduled n-child
        child.wasUnparented();
        if (isAdded() || needsRemove) {
            child.set(Flag.IS_REMOVING, true);
            child.wasRemoved();
        }
        if (dispose) child.layer.close();
    }

    @Override protected void wasAdded () {
        super.wasAdded();
        for (int ii = 0, count = childCount(); ii < count; ii++) {
            Element<?> child = childAt(ii);
            child.set(Flag.IS_ADDING, true);
            child.wasAdded();
        }
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        boolean willDispose = isSet(Flag.WILL_DISPOSE);
        for (int ii = 0, count = childCount(); ii < count; ii++) {
            Element<?> child = childAt(ii);
            if (willDispose) child.set(Flag.WILL_DISPOSE, true);
            child.set(Flag.IS_REMOVING, true);
            child.wasRemoved();
        }
        // if we're added again, we'll be re-laid-out
    }

    @Override protected Dimension computeSize (LayoutData ldata, float hintX, float hintY) {
        return getLayout().computeSize(this, hintX, hintY);
    }

    @Override protected void layout (LayoutData ldata, float left, float top,
                                     float width, float height) {
        // layout our children
        getLayout().layout(this, left, top, width, height);
        // layout is only called as part of revalidation, so now we validate our children
        for (int ii = 0, nn = childCount(); ii < nn; ii++) childAt(ii).validate();
    }

    protected abstract Layout getLayout ();
}
