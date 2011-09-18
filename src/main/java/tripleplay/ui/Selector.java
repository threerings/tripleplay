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

public class Selector
{
    public Signal<Element<?>> selected = Signal.create();
    public Signal<Element<?>> deselected = Signal.create();

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
                _conns.remove(removed).disconnect();
            }
        });
    }

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

    public Element<?> selected () {
        return _selected;
    }

    protected void onChildAdded (Element<?> child) {
        if (child instanceof ClickableTextWidget) {
            _conns.put(child, ((ClickableTextWidget<?>)child).clicked.connect(_clickSlot));
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
