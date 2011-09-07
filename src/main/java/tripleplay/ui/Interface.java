//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.List;

import playn.core.PlayN;
import playn.core.Game;
import playn.core.Pointer;

/**
 * The main class that integrates the Triple Play UI with a PlayN game. This class is mainly
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
    }

    /**
     * Activates this interface, which causes it to take control of user input.
     */
    public void activate () {
        PlayN.pointer().setListener(_plistener);
    }

    /**
     * Posts a runnable that will be executed after the next time the interface is validated.
     * Processing deferred actions is not tremendously efficient, so don't call this every frame.
     */
    public void deferAction (Runnable action) {
        _actions.add(action);
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

        // run any deferred actions
        if (!_actions.isEmpty()) {
            List<Runnable> actions = new ArrayList<Runnable>(_actions);
            _actions.clear();
            for (Runnable action : actions) {
                try {
                    action.run();
                } catch (Exception e) {
                    Log.log.warning("Interface action failed: " + action, e);
                }
            }
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

    protected final Pointer.Listener _delegate, _plistener = new Pointer.Listener() {
        @Override public void onPointerStart (Pointer.Event event) {
            float x = event.x(), y = event.y();
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
                _delegate.onPointerStart(event);
            } finally {
                _dispatch.clear();
            }
        }
        @Override public void onPointerDrag (Pointer.Event event) {
            if (_active != null) {
                _active.dispatchPointerDrag(event.x(), event.y());
            } else {
                _delegate.onPointerDrag(event);
            }
        }
        @Override public void onPointerEnd (Pointer.Event event) {
            if (_active != null) {
                _active.dispatchPointerEnd(event.x(), event.y());
                _active = null;
            } else {
                _delegate.onPointerEnd(event);
            }
        }
        protected Root _active;
    };

    protected final List<Root> _roots = new ArrayList<Root>();
    protected final List<Root> _dispatch = new ArrayList<Root>();
    protected final List<Runnable> _actions = new ArrayList<Runnable>();
}
