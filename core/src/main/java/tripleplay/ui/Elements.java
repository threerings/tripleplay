//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import playn.core.Layer;

import pythagoras.f.Dimension;
import pythagoras.f.Point;

import react.Signal;
import react.SignalView;

/**
 * Contains other elements and lays them out according to a layout policy.
 */
public abstract class Elements<T extends Elements<T>> extends Element<T>
    implements Iterable<Element<?>>
{
    /**
     * Creates a collection with the specified layout.
     */
    public Elements (Layout layout) {
        _layout = layout;

        // optimize hit testing by checking our bounds first
        layer.setHitTester(new Layer.HitTester() {
            public Layer hitTest (Layer layer, Point p) {
                return (isVisible() && contains(p.x, p.y)) ? layer.hitTestDefault(p) : null;
            }
        });
    }

    /** Emitted after a child has been added to this Elements. */
    public SignalView<Element<?>> childAdded() { return _childAdded; }

    /** Emitted after a child has been removed from this Elements. */
    public SignalView<Element<?>> childRemoved() { return _childRemoved; }

    /**
     * Returns the stylesheet configured for this group, or null.
     */
    public Stylesheet stylesheet () {
        return _sheet;
    }

    /**
     * Configures the stylesheet to be used by this group.
     */
    public T setStylesheet (Stylesheet sheet) {
        _sheet = sheet;
        return asT();
    }

    public int childCount () {
        return _children.size();
    }

    public Element<?> childAt (int index) {
        return _children.get(index);
    }

    public T add (Element<?>... children) {
        // remove the children from existing parents, if any
        for (Element<?> child : children) {
            Elements<?> parent = child.parent();
            if (parent != null) {
                parent.remove(child);
            }
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
        Elements<?> parent = child.parent();
        if (parent != null) {
            parent.remove(child);
        }

        _children.add(index, child);
        didAdd(child);
        invalidate();
        return asT();
    }

    public void remove (Element<?> child) {
        if (_children.remove(child)) {
            didRemove(child, false);
            invalidate();
        }
    }

    public void destroy (Element<?> child) {
        if (_children.remove(child)) {
            didRemove(child, true);
            invalidate();
        } else {
            child.layer.destroy();
        }
    }

    public void removeAt (int index) {
        didRemove(_children.remove(index), false);
        invalidate();
    }

    public void destroyAt (int index) {
        didRemove(_children.remove(index), true);
        invalidate();
    }

    public void removeAll () {
        while (!_children.isEmpty()) {
            removeAt(_children.size()-1);
        }
        invalidate();
    }

    public void destroyAll () {
        while (!_children.isEmpty()) {
            destroyAt(_children.size()-1);
        }
        invalidate();
    }

    /** Returns an unmodifiable iterator over the children of this Elements.  */
    public Iterator<Element<?>> iterator () {
        return Collections.unmodifiableList(_children).iterator();
    }

    protected void didAdd (Element<?> child) {
        layer.add(child.layer);
        child.wasParented(this);
        if (isAdded()) child.wasAdded();
        _childAdded.emit(child);
    }

    protected void didRemove (Element<?> child, boolean destroy) {
        if (destroy) child.set(Flag.WILL_DESTROY, true);
        layer.remove(child.layer);
        if (isAdded()) child.wasRemoved();
        child.wasUnparented();
        if (destroy) child.layer.destroy();
        _childRemoved.emit(child);
    }

    @Override protected void wasAdded () {
        super.wasAdded();
        for (Element<?> child : _children) {
            child.wasAdded();
        }
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        boolean willDestroy = isSet(Flag.WILL_DESTROY);
        for (Element<?> child : _children) {
            if (willDestroy) child.set(Flag.WILL_DESTROY, true);
            child.wasRemoved();
        }
        // if we're added again, we'll be re-laid-out
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new ElementsLayoutData();
    }

    protected class ElementsLayoutData extends LayoutData {
        @Override public Dimension computeSize (float hintX, float hintY) {
            return _layout.computeSize(Elements.this, hintX, hintY);
        }

        @Override public void layout (float left, float top, float width, float height) {
            // layout our children
            _layout.layout(Elements.this, left, top, width, height);
            // layout is only called as part of revalidation, so now we validate our children
            for (Element<?> child : _children) child.validate();
        }
    }

    protected final Layout _layout;
    protected final List<Element<?>> _children = new ArrayList<Element<?>>();

    protected final Signal<Element<?>> _childAdded = Signal.create();
    protected final Signal<Element<?>> _childRemoved = Signal.create();

    protected Stylesheet _sheet;
}
