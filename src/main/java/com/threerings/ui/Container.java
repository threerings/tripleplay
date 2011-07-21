//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import java.util.ArrayList;
import java.util.List;

import forplay.core.ForPlay;
import forplay.core.GroupLayer;
import forplay.core.Layer;

import pythagoras.f.Dimension;

/**
 * A grouping element that contains other elements and lays them out according to a layout policy.
 */
public class Container extends Element
{
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
        // TODO: check if child is already added here? has parent?
        _children.add(index, child);
        invalidate();
    }

    public void remove (Element child) {
        if (_children.remove(child)) {
            invalidate();
        }
    }

    public void removeAt (int index) {
        _children.remove(index);
        invalidate();
    }

    public void removeAll () {
        _children.clear();
        invalidate();
    }

    @Override protected void computeSize (float hintX, float hintY, Dimension into) {
        // TODO
    }

    @Override protected void setSize (float width, float height) {
        // TODO
    }

    @Override protected Layer layer () {
        return _layer;
    }

    protected GroupLayer _layer = ForPlay.graphics().createGroupLayer();
    protected List<Element> _children = new ArrayList<Element>();
}
