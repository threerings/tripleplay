//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

/**
 * A shared base class for elements which contain other elements.
 */
public abstract class Container<T extends Container<T>> extends Element<T> {

    public abstract void remove (Element<?> child);

    /**
     * Returns the stylesheet associated with this parent, or null. Styles are resolved by
     * searching up the container hierarchy. Only {@link Elements} actually provides styles to its
     * children.
     */
    public abstract Stylesheet stylesheet ();

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

}
