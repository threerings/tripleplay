//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

/**
 * Extends the {@link ClickableTextWidget} with toggling behavior.
 */
public abstract class TogglableTextWidget<T extends TogglableTextWidget<T>>
    extends ClickableTextWidget<T>
{
    /**
     * Returns true if this widget is currently selected.
     */
    public boolean isSelected () {
        return super.isSelected();
    }

    /**
     * Sets the selection state of this widget.
     */
    public T setSelected (boolean value) {
        set(Flag.SELECTED, value);
        invalidate();
        return asT();
    }

    /** Called when the mouse is clicked on this widget. */
    protected void onPress () {
        _anchorState = isSelected();
        set(Flag.SELECTED, !_anchorState);
        invalidate();
    }

    /** Called as the user drags the pointer around with the widget depressed. */
    protected void onHover (boolean inBounds) {
        boolean selected = inBounds ? !_anchorState : _anchorState;
        if (selected != isSelected()) {
            set(Flag.SELECTED, selected);
            invalidate();
        }
    }

    /** Called when the mouse is released after having been pressed on this widget. This should
     * {@link #onClick} if appropriate. */
    protected void onRelease () {
        if (_anchorState != isSelected()) {
            onClick();
        }
    }

    protected boolean _anchorState;
}
