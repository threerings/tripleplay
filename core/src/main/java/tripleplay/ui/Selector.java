//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.Slot;
import react.Value;
import react.ValueView;

/**
 * Maintains a single selected item among a specified set of <code>Element</code> instances. The
 * elements may be added individually, or the children of an <code>Elements</code> may be tracked
 * automatically.
 *
 * <p>A click on a tracked element that implements <code>Clickable</code> makes it the selected
 * item, or <code>selected</code> can be used to manually control the selected item.</p>
 */
public class Selector
{
    /** The selected item. May be updated to set the selection manually. */
    public final Value<Element<?>> selected = Value.create(null);

    /** Create a selector with a null initial selection. */
    public Selector () {
        selected.connect(new ValueView.Listener<Element<?>> () {
            @Override public void onChange (Element<?> selected, Element<?> deselected) {
                if (deselected != null) get(deselected).update(false);
                if (selected != null) get(selected).update(true);
            }
        });
    }

    /** Creates a selector containing the children of elements with initialSelection selected. */
    public Selector (Elements<?> elements, Element<?> initialSelection) {
        this();
        add(elements);
        if (initialSelection instanceof Togglable<?>) {
            selected.update(initialSelection);
        }
    }

    /**
     * Tracks the children of <code>elements</code> for setting the selection. Children
     * subsequently added or removed from <code>elements</code> are automatically handled
     * appropriately.
     */
    public Selector add (Elements<?> elements) {
        for (Element<?> child : elements) {
            _addSlot.onEmit(child);
        }
        elements.childAdded().connect(_addSlot);
        elements.childRemoved().connect(_removeSlot);
        return this;
    }

    /**
     * Stops tracking the children of <code>elements</code> for setting the selection.
     */
    public Selector remove (Elements<?> elements) {
        for (Element<?> child : elements) {
            _removeSlot.onEmit(child);
        }
        elements.childAdded().disconnect(_addSlot);
        elements.childRemoved().disconnect(_removeSlot);
        return this;
    }

    /**
     * Tracks one or more elements.
     */
    public Selector add (Element<?> elem, Element<?>... more) {
        _addSlot.onEmit(elem);
        for (Element<?> e : more) {
            _addSlot.onEmit(e);
        }
        return this;
    }

    /**
     * Stops tracking one or more elements.
     */
    public Selector remove (Element<?> elem, Element<?>... more) {
        _removeSlot.onEmit(elem);
        for (Element<?> e : more) {
            _removeSlot.onEmit(e);
        }
        return this;
    }

    /**
     * Internal method to get the selection value of an element (non-null).
     */
    protected Value<Boolean> get (Element<?> elem) {
        return ((Togglable<?>)elem).selected();
    }

    protected final Slot<Element<?>> _addSlot = new Slot<Element<?>>() {
        @Override public void onEmit (Element<?> child) {
            if (child instanceof Togglable<?>) {
                ((Togglable<?>)child).clicked().connect(_clickSlot);
            }
        }
    };

    protected final Slot<Element<?>> _removeSlot = new Slot<Element<?>>() {
        @Override public void onEmit (Element<?> removed) {
            if (removed instanceof Togglable<?>) {
                ((Togglable<?>)removed).clicked().disconnect(_clickSlot);
            }
            if (selected.get() == removed) selected.update(null);
        }
    };

    protected final Slot<Element<?>> _clickSlot = new Slot<Element<?>>() {
        @Override public void onEmit (Element<?> clicked) {
            selected.update(get(clicked).get() ? clicked : null);
        }
    };
}
