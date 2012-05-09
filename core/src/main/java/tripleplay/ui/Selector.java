//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.Slot;
import react.Value;
import react.ValueView;

/**
 * Maintains a single selected item among the children of <code>Elements</code>.<p>
 *
 * A click on a child that implements <code>Clickable</code> makes it the selected item, or
 * <code>setSelected</code> can be used to manually control the selected item.
 */
public class Selector
{
    /** The selected item. May be updated to set the selection manually. */
    public final Value<Element<?>> selected = Value.create(null);

    /** Create a selector with a null initial selection. */
    public Selector () {
        selected.connect(new ValueView.Listener<Element<?>> () {
            @Override public void onChange (Element<?> selected, Element<?> deselected) {
                if (deselected != null) {
                    deselected.set(Element.Flag.SELECTED, false);
                    deselected.invalidate();
                }
                if (selected != null) {
                    selected.set(Element.Flag.SELECTED, true);
                    selected.invalidate();
                }
            }
        });
    }

    /** Creates a selector containing the children of elements with initialSelection selected. */
    public Selector (Elements<?> elements, Element<?> initialSelection) {
        this();
        add(elements);
        selected.update(initialSelection);
    }

    /**
     * Tracks the children of <code>elements</code> for setting the selection.
     */
    public Selector add (Elements<?> elements) {
        for (Element<?> child : elements) {
            _childAddSlot.onEmit(child);
        }
        elements.childAdded().connect(_childAddSlot);
        elements.childRemoved().connect(_childRemoveSlot);
        return this;
    }

    /**
     * Stops tracking the children of <code>elements</code> for setting the selection.
    */
    public Selector remove (Elements<?> elements) {
        for (Element<?> child : elements) {
            _childRemoveSlot.onEmit(child);
        }
        elements.childAdded().disconnect(_childAddSlot);
        elements.childRemoved().disconnect(_childRemoveSlot);
        return this;
    }

    protected final Slot<Element<?>> _childAddSlot = new Slot<Element<?>>() {
        @Override public void onEmit (Element<?> child) {
            if (child instanceof Clickable<?>) {
                ((Clickable<?>)child).clicked().connect(_clickSlot);
            }
        }
    };

    protected final Slot<Element<?>> _childRemoveSlot = new Slot<Element<?>>() {
        @Override public void onEmit (Element<?> removed) {
            if (removed instanceof Clickable<?>) {
                ((Clickable<?>)removed).clicked().disconnect(_clickSlot);
            }
            if (selected.get() == removed) selected.update(null);
        }
    };

    protected final Slot<Element<?>> _clickSlot = new Slot<Element<?>>() {
        @Override public void onEmit (Element<?> clicked) {
            selected.update(clicked);
        }
    };

}
