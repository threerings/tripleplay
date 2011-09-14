//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pythagoras.f.Dimension;
import pythagoras.f.Point;

/**
* Contains other elements and lays them out according to a layout policy.
*/
public abstract class Elements<T extends Elements<T>> extends Element<T>
{
    /**
     * Creates a collection with the specified layout.
     */
    public Elements (Layout layout) {
        _layout = layout;
    }

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

    public Element childAt (int index) {
        return _children.get(index);
    }

    public T add (Element... children) {
        _children.addAll(Arrays.asList(children));
        for (Element child : children) {
            didAdd(child);
        }
        invalidate();
        return asT();
    }

    public T add (int index, Element child) {
        // TODO: check if child is already added here? has parent?
        _children.add(index, child);
        didAdd(child);
        invalidate();
        return asT();
    }

    public void remove (Element child) {
        if (_children.remove(child)) {
            didRemove(child);
            invalidate();
        }
    }

    public void removeAt (int index) {
        didRemove(_children.remove(index));
        invalidate();
    }

    public void removeAll () {
        while (!_children.isEmpty()) {
            removeAt(_children.size()-1);
        }
        invalidate();
    }

    protected void didAdd (Element<?> child) {
        layer.add(child.layer);
        if (isAdded()) child.wasAdded(this);
    }

    protected void didRemove (Element<?> child) {
        layer.remove(child.layer);
        if (isAdded()) child.wasRemoved();
    }

    @Override protected void wasAdded (Elements<?> parent) {
        super.wasAdded(parent);
        for (Element<?> child : _children) {
            child.wasAdded(this);
        }
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        for (Element child : _children) {
            child.wasRemoved();
        }

        // clear out our background instance
        if (_bginst != null) {
            _bginst.destroy();
            _bginst = null;
        }
        // if we're added again, we'll be re-laid-out
    }

    @Override protected Element hitTest (Point point) {
        // transform the point into our coordinate system
        point = layer.transform().inverseTransform(point, point);
        // check whether it falls within our bounds
        float x = point.x + layer.originX(), y = point.y + layer.originY();
        if (!contains(x, y)) return null;
        // determine whether it falls within the bounds of any of our children
        for (Element child : _children) {
            Element hit = child.hitTest(point.set(x, y));
            if (hit != null) return hit;
        }
        return null;
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        LayoutData ldata = computeLayout(hintX, hintY);
        Dimension size = _layout.computeSize(
            _children, hintX - ldata.bg.width(), hintY - ldata.bg.height());
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
        _layout.layout(_children, ldata.bg.left, ldata.bg.top,
                       _size.width - ldata.bg.width(), _size.height - ldata.bg.height());

        // layout is only called as part of revalidation, so now we validate our children
        for (Element child : _children) {
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
            _ldata.bg = resolveStyle(state(), Style.BACKGROUND);
        }
        return _ldata;
    }

    protected static class LayoutData {
        public Background bg;
    }

    protected final Layout _layout;
    protected final List<Element> _children = new ArrayList<Element>();
    protected Stylesheet _sheet;

    protected LayoutData _ldata;
    protected Background.Instance _bginst;
}
