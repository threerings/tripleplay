//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A container with a fixed list of children, which client code must assume is immutable.
 * Subclasses may or may not expose the children directly. Subclasses may layout via the
 * {@link Layout} system or roll their own.
 * TODO: why not remove the publicly exposed mutating methods in Container instead of throwing
 * exceptions here?
 */
public abstract class Composite<T extends Composite<T>> extends Container<T>
{
    /**
     * Sets the stylesheet of this composite.
     */
    public T setStylesheet (Stylesheet stylesheet) {
        _stylesheet = stylesheet;
        invalidate();
        return asT();
    }

    @Override public Stylesheet stylesheet () {
        return _stylesheet;
    }

    @Override public int childCount () {
        return _children.size();
    }

    @Override public Element<?> childAt (int index) {
        return _children.get(index);
    }

    @Override public Iterator<Element<?>> iterator () {
        return _children.iterator();
    }

    /**
     * Not implemented; subclasses have exclusive control via {@link #initChildren}.
     * @throws UnsupportedOperationException
     */
    @Override public void remove (Element<?> child) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented; subclasses have exclusive control via {@link #initChildren}.
     * @throws UnsupportedOperationException
     */
    @Override public void removeAt (int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented; subclasses have exclusive control via {@link #initChildren}.
     * @throws UnsupportedOperationException
     */
    @Override public void removeAll () {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented; subclasses have exclusive control via {@link #initChildren}.
     * @throws UnsupportedOperationException
     */
    @Override public void destroy (Element<?> child) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented; subclasses have exclusive control via {@link #initChildren}.
     * @throws UnsupportedOperationException
     */
    @Override public void destroyAt (int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Not implemented; subclasses have exclusive control via {@link #initChildren}.
     * @throws UnsupportedOperationException
     */
    @Override public void destroyAll () {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new composite instance with no children. Subclasses are expected to call
     * {@link #initChildren(List)} afterwards to supply the enumeration of children.
     */
    protected Composite () {
        set(Flag.HIT_DESCEND, true);
    }

    /**
     * Creates a new composite instance with the given children. The list is used directly.
     */
    protected Composite (List<Element<?>> children) {
        this();
        initChildren(children);
    }

    /**
     * Creates a new composite instance with the given children. The array is used directly.
     */
    protected Composite (Element<?>... children) {
        this();
        initChildren(children);
    }

    /**
     * Sets the composite children; subclasses are expected to call this during construction or
     * supply children in {@link #Composite(List)}. The list is used directly.
     */
    protected void initChildren (List<Element<?>> children) {
        if (!_children.isEmpty()) throw new IllegalStateException();
        setChildren(children, false);
    }

    /**
     * Sets the composite children; subclasses are expected to call this during construction or
     * supply children in {@link #Composite(List)}. The array is used directly.
     */
    protected void initChildren (Element<?>... children) {
        initChildren(Arrays.asList(children));
    }

    /**
     * Sets the composite children; this is probably not needed for most composite types.
     */
    protected void setChildren (List<Element<?>> children, boolean destroy) {
        for (Element<?> child : _children) didRemove(child, destroy);
        for (Element<?> child : _children = children) didAdd(child);
        invalidate();
    }

    /**
     * Sets the optional layout. If not null, this composite will henceforth lay itself out by
     * delegation to it. If null or not called, subclasses must override
     * {@link #createLayoutData(float, float)}.
     */
    protected void setLayout (Layout layout) {
        _layout = layout;
        invalidate();
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        if (_layout == null) throw new IllegalStateException();
        return new ContainerLayoutData() {
            @Override public Layout getLayout() { return _layout; }
        };
    }

    /** Children set by subclass. */
    protected List<Element<?>> _children = Collections.emptyList();

    /** Optional layout set by subclass. */
    protected Layout _layout;

    /** Optional stylesheet. */
    protected Stylesheet _stylesheet;
}
