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
    protected ClickableTextWidget () {
        enableInteraction();
    }

    @Override protected void onPointerStart (Pointer.Event event, float x, float y) {
        super.onPointerStart(event, x, y);
        if (!isEnabled()) return;
        onPress();
    }

    @Override protected void onPointerDrag (Pointer.Event event, float x, float y) {
        super.onPointerDrag(event, x, y);
        if (!isEnabled()) return;
        onHover(contains(x, y));
    }

    @Override protected void onPointerEnd (Pointer.Event event, float x, float y) {
        super.onPointerEnd(event, x, y);
        onRelease();
    }

    @Override protected void onPointerCancel (Pointer.Event event) {
        super.onPointerCancel(event);
        onCancel();
    }

    /** Called when the mouse is clicked on this widget. */
    protected void onPress () {
        set(Flag.SELECTED, true);
        invalidate();
    }

    /** Called as the user drags the pointer around with the widget depressed. */
    protected void onHover (boolean inBounds) {
        if (inBounds != isSelected()) {
            set(Flag.SELECTED, inBounds);
            invalidate();
        }
    }

    /** Called when the mouse is released after having been pressed on this widget. This should
     * {@link #onClick} if appropriate. */
    protected void onRelease () {
        if (isSelected()) {
            set(Flag.SELECTED, false);
            invalidate();
            onClick();
        }
    }

    /** Called when the interaction is canceled after having been pressed on this widget. This
     * should not result in a call to {@link #onClick}. */
    protected void onCancel () {
        if (isSelected()) {
            set(Flag.SELECTED, false);
            invalidate();
        }
    }

    /** Called when the mouse is pressed and released over this widget. */
    protected void onClick () {
        // nada by default
    }
}
