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
        _children.addAll(Arrays.asList(children));
        for (Element<?> child : children) {
            didAdd(child);
        }
        invalidate();
        return asT();
    }

    public T add (int index, Element<?> child) {
        // TODO: check if child is already added here? has parent?
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
        if (isAdded()) child.wasAdded(this);
        _childAdded.emit(child);
    }

    protected void didRemove (Element<?> child, boolean destroy) {
        layer.remove(child.layer);
        if (destroy) child.layer.destroy();
        if (isAdded()) child.wasRemoved();
        _childRemoved.emit(child);
    }

    @Override protected void wasAdded (Elements<?> parent) {
        super.wasAdded(parent);
        for (Element<?> child : _children) {
            child.wasAdded(this);
        }
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        for (Element<?> child : _children) {
            child.wasRemoved();
        }

        // clear out our background instance
        if (_bginst != null) {
            _bginst.destroy();
            _bginst = null;
        }
        // if we're added again, we'll be re-laid-out
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        LayoutData ldata = computeLayout(hintX, hintY);
        Dimension size = _layout.computeSize(
            this, hintX - ldata.bg.width(), hintY - ldata.bg.height());
        return ldata.bg.addInsets(size);
    }

    @Override protected void layout () {
        LayoutData ldata = computeLayout(_size.width, _size.height);

        // prepare our background
        if (_bginst != null) _bginst.destroy();
        if (_size.width > 0 && _size.height > 0) {
            _bginst = ldata.bg.instantiate(_size);
            _bginst.addTo(layer);
        }

        // layout our children
        _layout.layout(this, ldata.bg.left, ldata.bg.top,
                       _size.width - ldata.bg.width(), _size.height - ldata.bg.height());

        // layout is only called as part of revalidation, so now we validate our children
        for (Element<?> child : _children) {
            child.validate();
        }

        clearLayoutData();
    }

    @Override protected void clearLayoutData () {
        _ldata = null;
    }

    protected LayoutData computeLayout (float hintX, float hintY) {
        if (_ldata == null) {
            _ldata = new LayoutData();
            // determine our background
            _ldata.bg = resolveStyle(Style.BACKGROUND);
        }
        return _ldata;
    }

    protected static class LayoutData {
        public Background bg;
    }

    protected final Layout _layout;
    protected final List<Element<?>> _children = new ArrayList<Element<?>>();

    protected final Signal<Element<?>> _childAdded = Signal.create();
    protected final Signal<Element<?>> _childRemoved = Signal.create();

    protected Stylesheet _sheet;

    protected LayoutData _ldata;
    protected Background.Instance _bginst;
}
