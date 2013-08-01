//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.Iterator;

/**
 * A shared base class for elements which contain other elements.
 */
public abstract class Container<T extends Container<T>> extends Element<T>
    implements Iterable<Element<?>>
{
    /**
     * Returns the stylesheet associated with this parent, or null. Styles are resolved by
     * searching up the container hierarchy. Only {@link Elements} actually provides styles to its
     * children.
     */
    public abstract Stylesheet stylesheet ();

    /** Returns the number of children contained by this container. */
    public abstract int childCount ();

    /*** Returns the child at the specified index. */
    public abstract Element<?> childAt (int index);

    /** Returns an unmodifiable iterator over the children of this Elements.  */
    public abstract Iterator<Element<?>> iterator ();

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

    protected void didAdd (Element<?> child) {
        layer.add(child.layer);
        child.wasParented(this);
        // bar n-child from being added twice
        if (isAdded() && !child.willAdd()) {
            child.set(Flag.IS_ADDING, true);
            child.wasAdded();
        }
    }

    protected void didRemove (Element<?> child, boolean destroy) {
        if (destroy) child.set(Flag.WILL_DESTROY, true);
        layer.remove(child.layer);
        boolean needsRemove = child.willRemove(); // early removal of a scheduled n-child
        child.wasUnparented();
        if (isAdded() || needsRemove) {
            child.set(Flag.IS_REMOVING, true);
            child.wasRemoved();
        }
        if (destroy) child.layer.destroy();
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
        boolean willDestroy = isSet(Flag.WILL_DESTROY);
        for (int ii = 0, count = childCount(); ii < count; ii++) {
            Element<?> child = childAt(ii);
            if (willDestroy) child.set(Flag.WILL_DESTROY, true);
            child.set(Flag.IS_REMOVING, true);
            child.wasRemoved();
        }
        // if we're added again, we'll be re-laid-out
    }
}
