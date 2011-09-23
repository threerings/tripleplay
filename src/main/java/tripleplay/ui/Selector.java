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
    public Selector () {
        _selected.listen(new ValueView.Listener<Element<?>> () {
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

    /** Returns a ValueView that emits changes when the selected item changes. */
    public ValueView<Element<?>> selected () { return _selected; }

    /** Sets the selected item. */
    public Selector setSelected (Element<?> selected) {
       _selected.update(selected);
       return this;
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
        }
    };

    protected final Slot<Element<?>> _clickSlot = new Slot<Element<?>>() {
        @Override public void onEmit (Element<?> clicked) {
            _selected.update(clicked);
        }
    };

    protected final Value<Element<?>> _selected = Value.create(null);
}
