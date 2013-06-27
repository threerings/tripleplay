//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Pointer;

import react.Slot;
import react.Value;

/**
 * Extends the {@link TextWidget} with toggling behavior.
 */
public abstract class TogglableTextWidget<T extends TogglableTextWidget<T>> extends TextWidget<T>
    implements Togglable<T>
{
    /** Indicates whether this widget is selected. It may be listened to, and updated. */
    public final Value<Boolean> selected = Value.create(false);

    @Override // from Togglable
    public Value<Boolean> selected () {
        return selected;
    }

    protected TogglableTextWidget () {
        enableInteraction();
        selected.connect(new Slot<Boolean>() {
            public void onEmit (Boolean selected) {
                if (selected != isSelected()) {
                    set(Flag.SELECTED, selected);
                    invalidate();
                }
            }
        });
    }

    @Override protected void onPress (Pointer.Event event) {
        _anchorState = isSelected();
        selected.update(!_anchorState);
    }

    @Override protected void onHover (Pointer.Event event, boolean inBounds) {
        selected.update(inBounds ? !_anchorState : _anchorState);
    }

    @Override protected void onRelease (Pointer.Event event) {
        // we explicitly don't call super here
        if (_anchorState != isSelected()) {
            onClick(event);
        }
    }

    @Override protected void onCancel (Pointer.Event event) {
        // we explicitly don't call super here
        selected.update(_anchorState);
    }

    protected boolean _anchorState;
}
