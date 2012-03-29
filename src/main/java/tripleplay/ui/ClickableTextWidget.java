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
    /**
     * Alters this widget to remain in a selected state after being pressed. In other words, make
     * it a check box. Note that this behavior currently hijacks the SELECTED flag so may need
     * refinement.
     * TODO: consider better check box support, though for touch devices, there isn't much to gain
     */
    public T withToggleBehavior () {
        _toggle = true;
        return asT();
    }

    /**
     * Returns true if this widget is currently selected. This is not normally needed unless the
     * widget is a toggle, set with {@link #withToggleBehavior()}.
     */
    public boolean isSelected () {
        return super.isSelected();
    }

    /**
     * Sets the selection state of this widget. This should only be used with toggle widgets, set
     * with {@link #withToggleBehavior()}.
     */
    public T setSelected (boolean value) {
        set(Flag.SELECTED, value);
        invalidate();
        return asT();
    }

    protected ClickableTextWidget () {
        enableInteraction();
    }

    @Override protected void onPointerStart (Pointer.Event event, float x, float y) {
        super.onPointerStart(event, x, y);
        if (!isEnabled()) return;
        _anchorState = isSelected();
        set(Flag.SELECTED, !_anchorState);
        invalidate();
        onPress();
    }

    @Override protected void onPointerDrag (Pointer.Event event, float x, float y) {
        super.onPointerDrag(event, x, y);
        boolean over = isEnabled() && contains(x, y);
        boolean selected = over ? !_anchorState : _anchorState;
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
        if (_toggle) {
            onClick();

        } else if (isSelected()) {
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

    protected boolean _toggle;
    protected boolean _anchorState;
}
