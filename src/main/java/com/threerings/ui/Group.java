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

import com.threerings.ui.bgs.NullBackground;

/**
 * A grouping element that contains other elements and lays them out according to a layout policy.
 */
public class Group extends Element
{
    /** The background for a group. Not inherited. */
    public static final Style<Background> BACKGROUND = Style.<Background>newStyle(
        false, new NullBackground());

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
        // the child may not have created its layer yet
        Layer l = child.layer();
        if (l != null) _layer.add(l);
        if (isAdded()) child.wasAdded(this);
    }

    protected void didRemove (Element child) {
        // the child may not have created its layer yet
        Layer l = child.layer();
        if (l != null) _layer.remove(l);
        if (_constraints != null) _constraints.remove(child);
        if (isAdded()) child.wasRemoved();
    }

    protected void layerChanged (Element elem, Layer oldLayer, Layer newLayer) {
        if (oldLayer != null) _layer.remove(oldLayer);
        if (newLayer != null) _layer.add(newLayer);
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
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        Background bg = getStyle(BACKGROUND, State.DEFAULT);
        Dimension dims = _layout.computeSize(
            _children, _constraints, hintX - bg.width(), hintY - bg.height());
        dims.width += bg.width();
        dims.height += bg.height();
        System.out.println("Prefer " + dims);
        return dims;
    }

    @Override protected void layout () {
        if (_bginst != null) _bginst.destroy();
        Background bg = getStyle(BACKGROUND, State.DEFAULT);
        _bginst = bg.instantiate(_size);
        _bginst.addTo(_layer);
        System.out.println("Got " + _size);
        _layout.layout(_children, _constraints, bg.left, bg.top,
                       _size.width - bg.width(), _size.height - bg.height());
    }

    @Override protected Layer layer () {
        return _layer;
    }

    protected final GroupLayer _layer = ForPlay.graphics().createGroupLayer();
    protected final List<Element> _children = new ArrayList<Element>();

    protected final Layout _layout;
    protected Map<Element, Layout.Constraint> _constraints; // lazily created

    protected Background.Instance _bginst;
}
