//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import java.util.ArrayList;
import java.util.List;

import forplay.core.ForPlay;
import forplay.core.Game;
import forplay.core.Pointer;

/**
 * The main class that integrates the Triple Play UI with a ForPlay game. This class is mainly
 * necessary to handle the proper dispatching of input events to UI elements. Create an interface
 * instance, create {@link Root} groups via the interface and add the {@link Root#layer}s into your
 * scene graph wherever you desire. Call {@link #update} and {@link #paint} from {@link
 * Game#update} and {@link Game#paint} to render your interface.
 */
public class Interface
{
    /**
     * Creates an interface instance which will delegate unconsumed pointer events to the supplied
     * pointer delegate.
     */
    public Interface (Pointer.Listener delegate) {
        // if we have no delegate, use a NOOP delegate to simplify our logic
        _delegate = (delegate == null) ? new Pointer.Adapter() : delegate;
        ForPlay.pointer().setListener(_plistener);
    }

    /**
     * Updates the elements in this interface. Must be called from {@link Game#update}.
     */
    public void update (float delta) {
        // nada at the moment
    }

    /**
     * "Paints" the elements in this interface. Must be called from {@link Game#update}.
     */
    public void paint (float alpha) {
        for (int ii = 0, ll = _roots.size(); ii < ll; ii++) {
            _roots.get(ii).validate();
        }
    }

    /**
     * Creates a root element with the specified layout and stylesheet.
     */
    public Root createRoot (Layout layout, Stylesheet sheet) {
        Root root = new Root(this, layout, sheet);
        _roots.add(root);
        return root;
    }

    /**
     * Removes the supplied root element from this interface.
     */
    public void removeRoot (Root root) {
        _roots.remove(root);
    }

    protected final List<Root> _roots = new ArrayList<Root>();
    protected final List<Root> _dispatch = new ArrayList<Root>();
    protected final Pointer.Listener _delegate, _plistener = new Pointer.Listener() {
        @Override public void onPointerStart (float x, float y) {
            try {
                // copy our roots to a separate list to avoid conflicts if a root is added or
                // removed while dispatching an event
                _dispatch.addAll(_roots);
                for (Root root : _dispatch) {
                    if (root.dispatchPointerStart(x, y)) {
                        _active = root;
                        return;
                    }
                }
                _delegate.onPointerStart(x, y);
            } finally {
                _dispatch.clear();
            }
        }
        @Override public void onPointerDrag (float x, float y) {
            if (_active != null) {
                _active.dispatchPointerDrag(x, y);
            } else {
                _delegate.onPointerDrag(x, y);
            }
        }
        @Override public void onPointerEnd (float x, float y) {
            if (_active != null) {
                _active.dispatchPointerEnd(x, y);
                _active = null;
            } else {
                _delegate.onPointerEnd(x, y);
            }
        }
        protected Root _active;
    };
}
