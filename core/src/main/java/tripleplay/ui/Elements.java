//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import react.Signal;
import react.SignalView;

/**
 * Contains other elements and lays them out according to a layout policy.
 */
public abstract class Elements<T extends Elements<T>> extends Container.Mutable<T>
{
    /**
     * Creates a collection with the specified layout.
     */
    public Elements (Layout layout) {
        _layout = layout;
        set(Flag.HIT_DESCEND, true);
    }

    /** Emitted after a child has been added to this Elements. */
    public SignalView<Element<?>> childAdded () { return _childAdded; }

    /** Emitted after a child has been removed from this Elements. */
    public SignalView<Element<?>> childRemoved () { return _childRemoved; }

    /**
     * Returns the stylesheet configured for this group, or null.
     */
    @Override public Stylesheet stylesheet () {
        return _sheet;
    }

    /**
     * Configures the stylesheet to be used by this group.
     */
    public T setStylesheet (Stylesheet sheet) {
        _sheet = sheet;
        return asT();
    }

    public T add (Element<?>... children) {
        // remove the children from existing parents, if any
        for (Element<?> child : children) {
            removeFromParent(child, false);
        }

        _children.addAll(Arrays.asList(children));
        for (Element<?> child : children) {
            didAdd(child);
        }
        invalidate();
        return asT();
    }

    public T add (int index, Element<?> child) {
        // remove the child from an existing parent, if it has one
        Container.removeFromParent(child, false);

        _children.add(index, child);
        didAdd(child);
        invalidate();
        return asT();
    }

    @Override public int childCount () {
        return _children.size();
    }

    @Override public Element<?> childAt (int index) {
        return _children.get(index);
    }

    @Override public Iterator<Element<?>> iterator () {
        return Collections.unmodifiableList(_children).iterator();
    }

    @Override public void remove (Element<?> child) {
        if (_children.remove(child)) {
            didRemove(child, false);
            invalidate();
        }
    }

    @Override public void removeAt (int index) {
        didRemove(_children.remove(index), false);
        invalidate();
    }

    @Override public void removeAll () {
        while (!_children.isEmpty()) {
            removeAt(_children.size()-1);
        }
        invalidate();
    }

    @Override public void destroy (Element<?> child) {
        if (_children.remove(child)) {
            didRemove(child, true);
            invalidate();
        } else {
            child.layer.destroy();
        }
    }

    @Override public void destroyAt (int index) {
        didRemove(_children.remove(index), true);
        invalidate();
    }

    @Override public void destroyAll () {
        while (!_children.isEmpty()) {
            destroyAt(_children.size()-1);
        }
        invalidate();
    }

    @Override protected void didAdd (Element<?> child) {
        super.didAdd(child);
        _childAdded.emit(child);
    }

    @Override protected void didRemove (Element<?> child, boolean destroy) {
        super.didRemove(child, destroy);
        _childRemoved.emit(child);
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new ElementsLayoutData();
    }

    protected class ElementsLayoutData extends ContainerLayoutData {
        @Override public Layout getLayout() { return _layout; }
    }

    protected final Layout _layout;
    protected final List<Element<?>> _children = new ArrayList<Element<?>>();

    protected final Signal<Element<?>> _childAdded = Signal.create();
    protected final Signal<Element<?>> _childRemoved = Signal.create();

    protected Stylesheet _sheet;
}
