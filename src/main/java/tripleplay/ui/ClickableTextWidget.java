//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;

import react.Signal;
import react.SignalView;

/**
 * A text widget that provides button-like behavior.
 */
public class ClickableTextWidget<T extends ClickableTextWidget<T>> extends TextWidget<T>
    implements Clickable<T>
{
    @Override public SignalView<T> clicked () {
        return _clicked;
    }

    @Override protected void onPointerStart (float x, float y) {
        super.onPointerStart(x, y);
        if (!isEnabled()) return;
        set(Flag.SELECTED, true);
        invalidate();
    }

    @Override protected void onPointerDrag (float x, float y) {
        super.onPointerDrag(x, y);
        boolean selected = isEnabled() && contains(x, y);
        if (selected != isSelected()) {
            set(Flag.SELECTED, selected);
            invalidate();
        }
    }

    @Override protected void onPointerEnd (float x, float y) {
        super.onPointerEnd(x, y);
        // we don't check whether the supplied coordinates are in our bounds or not because only
        // the drag changes result in changes to the button's visualization, and we want to behave
        // based on what the user sees
        if (isSelected()) {
            set(Flag.SELECTED, false);
            invalidate();
            _clicked.emit(asT()); // emit a click event
        }
    }

    protected final Signal<T> _clicked = Signal.create();
}
