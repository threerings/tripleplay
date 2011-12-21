//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Pointer;

/**
 * A text widget that provides button-like behavior.
 */
public abstract class ClickableTextWidget<T extends ClickableTextWidget<T>> extends TextWidget<T>
{
    @Override protected void onPointerStart (Pointer.Event event, float x, float y) {
        super.onPointerStart(event, x, y);
        if (!isEnabled()) return;
        set(Flag.SELECTED, true);
        invalidate();
        onPress();
    }

    @Override protected void onPointerDrag (Pointer.Event event, float x, float y) {
        super.onPointerDrag(event, x, y);
        boolean selected = isEnabled() && contains(x, y);
        if (selected != isSelected()) {
            set(Flag.SELECTED, selected);
            invalidate();
        }
    }

    @Override protected void onPointerEnd (Pointer.Event event, float x, float y) {
        super.onPointerEnd(event, x, y);
        onRelease();

        // we don't check whether the supplied coordinates are in our bounds or not because only
        // the drag changes cause changes to the button's visualization, and we want to behave
        // based on what the user sees
        if (isSelected()) {
            set(Flag.SELECTED, false);
            invalidate();
            onClick();
        }
    }

    /** Called when the mouse is clicked on this widget. */
    protected void onPress () {
        // nada by default
    }

    /** Called when the mouse is released after having been pressed on this widget. This will be
     * called before {@link #onClick}, if the latter is called at all. */
    protected void onRelease () {
        // nada by default
    }

    /** Called when the mouse is pressed and released over this widget. */
    protected void onClick () {
        // nada by default
    }
}
