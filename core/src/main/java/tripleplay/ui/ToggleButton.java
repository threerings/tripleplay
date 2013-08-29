//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.SignalView;
import react.Value;

/**
 * A toggle button that displays text, or an icon, or both. Clicking the button toggles it from
 * selected to unselected, and vice versa.
 */
public class ToggleButton extends AbstractTextButton<ToggleButton> implements Togglable<ToggleButton>
{
    /** Creates a button with no text or icon. */
    public ToggleButton () {
        this(null, null);
    }

    /**  Creates a button with the supplied text. */
    public ToggleButton (String text) {
        this(text, null);
    }

    /** Creates a button with the supplied icon. */
    public ToggleButton (Icon icon) {
        this(null, icon);
    }

    /** Creates a button with the supplied text and icon. */
    public ToggleButton (String text, Icon icon) {
        super(text, icon);
    }

    @Override public Value<Boolean> selected () {
        return ((Behavior.Toggle<ToggleButton>)_behave).selected;
    }

    @Override public SignalView<ToggleButton> clicked () {
        return ((Behavior.Toggle<ToggleButton>)_behave).clicked;
    }

    @Override public void click () {
        ((Behavior.Toggle<ToggleButton>)_behave).click();
    }

    @Override public String toString () {
        return "ToggleButton(" + text() + ")";
    }

    @Override protected Class<?> getStyleClass () {
        return ToggleButton.class;
    }

    @Override protected Behavior<ToggleButton> createBehavior () {
        return new Behavior.Toggle<ToggleButton>(asT());
    }
}
