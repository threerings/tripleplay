//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pythagoras.f.Dimension;

/**
 * A grouping element that contains other elements and lays them out according to a layout policy.
 */
public class Group extends Element
{
    /**
     * Creates a group with the specified layout and no stylesheet.
     */
    public Group (Layout layout) {
        this(layout, null);
    }

    /**
     * Creates a group with the specified layout and stylesheet.
     */
    public Group (Layout layout, Stylesheet sheet) {
        _layout = layout;
        _sheet = sheet;
    }

    /**
     * Returns the stylesheet configured for this group, or null.
     */
    public Stylesheet stylesheet () {
        return _sheet;
    }

    public int childCount () {
        return _children.size();
    }

    public Element childAt (int index) {
        return _children.get(index);
    }

    public void add (Element child) {
        add(_children.size(), child);
    }

    public void add (int index, Element child) {
        add(index, child, null);
    }

    public void add (Element child, Layout.Constraint constraint) {
        add(_children.size(), child, constraint);
    }

    public void add (int index, Element child, Layout.Constraint constraint) {
        // TODO: check if child is already added here? has parent?
        _children.add(index, child);
        if (constraint != null) {
            if (_constraints == null) {
                _constraints = new HashMap<Element, Layout.Constraint>();
            }
            _constraints.put(child, constraint);
        }
        didAdd(child);
        invalidate();
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
        _constraints = null;
        while (!_children.isEmpty()) {
            removeAt(_children.size()-1);
        }
        invalidate();
    }

    protected void didAdd (Element child) {
        layer.add(child.layer);
        if (isAdded()) child.wasAdded(this);
    }

    protected void didRemove (Element child) {
        layer.remove(child.layer);
        if (_constraints != null) _constraints.remove(child);
        if (isAdded()) child.wasRemoved();
    }

    @Override protected void wasAdded (Group parent) {
        super.wasAdded(parent);
        for (Element child : _children) {
            child.wasAdded(this);
        }
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        for (Element child : _children) {
            child.wasRemoved();
        }

        // clear out our background instance
        if (_bginst != null) _bginst.destroy();
        // if we're added again, we'll be re-laid-out
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        LayoutData ldata = computeLayout(hintX, hintY);
        Dimension size = _layout.computeSize(
            _children, _constraints, hintX - ldata.bg.width(), hintY - ldata.bg.height());
        return ldata.bg.addInsets(size);
    }

    @Override protected void layout () {
        LayoutData ldata = computeLayout(_size.width, _size.height);

        // prepare our background
        if (_bginst != null) _bginst.destroy();
        _bginst = ldata.bg.instantiate(_size);
        _bginst.addTo(layer);

        // layout our children
        _layout.layout(_children, _constraints, ldata.bg.left, ldata.bg.top,
                       _size.width - ldata.bg.width(), _size.height - ldata.bg.height());
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
    protected final Stylesheet _sheet;
    protected final List<Element> _children = new ArrayList<Element>();
    protected Map<Element, Layout.Constraint> _constraints; // lazily created

    protected LayoutData _ldata;
    protected Background.Instance _bginst;
}
