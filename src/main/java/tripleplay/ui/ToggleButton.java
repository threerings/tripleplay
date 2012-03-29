//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.Signal;
import react.SignalView;

/**
 * A toggle button that displays text, or an icon, or both. Clicking the button toggles it from
 * selected to unselected, and vice versa.
 */
public class ToggleButton extends TogglableTextWidget<ToggleButton>
    implements Clickable<ToggleButton>
{
    public ToggleButton (String text, Styles styles) {
        setStyles(styles).text.update(text);
    }

    public ToggleButton (Styles styles) {
        this("", styles);
    }

    public ToggleButton (String text) {
        this(text, Styles.none());
    }

    public ToggleButton () {
        this("");
    }

    @Override public SignalView<ToggleButton> clicked () {
        return _clicked;
    }

    @Override public String toString () {
        return "ToggleButton(" + text.get() + ")";
    }

    @Override protected void onClick () {
        _clicked.emit(this); // emit a click event
    }

    protected final Signal<ToggleButton> _clicked = Signal.create();
}
