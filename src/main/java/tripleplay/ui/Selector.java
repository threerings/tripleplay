//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.HashMap;
import java.util.Map;

import react.Connection;
import react.Signal;
import react.Slot;

/**
 * Maintains a single selected item among the children of an <code>Elements</code>.<p>
 *
 * A click on a child that implements <code>Clickable</code> makes it the selected item, or
 * <code>setSelected</code> can be used to manually control the selected item.
 */
public class Selector
{
    /** Emitted when the selection changes with the newly selected item. */
    public Signal<Element<?>> selected = Signal.create();

    /** Emitted when the selection changes with the newly deselected item. */
    public Signal<Element<?>> deselected = Signal.create();

    /**
     * Creates a selector on the children of <code>elements</code>.
     */
    public Selector (Elements<?> elements) {
        for (Element<?> child : elements) {
            onChildAdded(child);
        }
        elements.childAdded.connect(new Slot<Element<?>> () {
            @Override public void onEmit (Element<?> added) {
                onChildAdded(added);
            }
        });
        elements.childRemoved.connect(new Slot<Element<?>> () {
            @Override public void onEmit (Element<?> removed) {
                Connection conn = _conns.remove(removed);
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    /**
     * Sets the selected item and deselects the currently selected item if there is one.
     * <code>selected</code> may be null to select nothing.
     */
    public Selector setSelected (Element<?> selected) {
        if (_selected == selected) { return this; }
        if (_selected != null) {
            _selected.set(Element.Flag.SELECTED, false);
            _selected.invalidate();
            deselected.emit(_selected);
        }
        _selected = selected;
        if (_selected != null) {
            _selected.set(Element.Flag.SELECTED, true);
            _selected.invalidate();
        }
        this.selected.emit(_selected);
        return this;
    }

    /** Returns the selected item. Can be null if nothing is selected. */
    public Element<?> selected () {
        return _selected;
    }

    protected void onChildAdded (Element<?> child) {
        if (child instanceof Clickable<?>) {
            _conns.put(child, ((Clickable<?>)child).clicked().connect(_clickSlot));
        }
    }

    protected Element<?> _selected;

    protected final Slot<Element<?>> _clickSlot = new Slot<Element<?>>() {
        @Override public void onEmit (Element<?> clicked) {
            setSelected(clicked);
        }
    };

    protected final Map<Element<?>, Connection> _conns = new HashMap<Element<?>, Connection>();
}
