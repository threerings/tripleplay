//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forplay.core.ForPlay;
import forplay.core.GroupLayer;
import forplay.core.Layer;

import pythagoras.f.Dimension;

/**
 * A grouping element that contains other elements and lays them out according to a layout policy.
 */
public class Group extends Element
{
    /**
     * Creates a group with the specified layout.
     */
    public Group (Layout layout) {
        _layout = layout;
    }

    // TODO: was added, was removed, blah blah

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
        if (isAdded()) child.wasAdded();
    }

    protected void didRemove (Element child) {
        if (_constraints != null) _constraints.remove(child);
        if (isAdded()) child.wasRemoved();
    }

    @Override protected void wasAdded () {
        for (Element child : _children) {
            child.wasAdded();
        }
    }

    @Override protected void wasRemoved () {
        for (Element child : _children) {
            child.wasRemoved();
        }
    }

    @Override protected void computeSize (float hintX, float hintY, Dimension into) {
        _layout.computeSize(_children, _constraints, hintX, hintY, into);
    }

    @Override protected void setSize (float width, float height) {
        _layout.layout(_children, _constraints, width, height);
    }

    @Override protected Layer layer () {
        return _layer;
    }

    protected final Layout _layout;
    protected final GroupLayer _layer = ForPlay.graphics().createGroupLayer();
    protected final List<Element> _children = new ArrayList<Element>();
    protected Map<Element, Layout.Constraint> _constraints; // lazily created
}
