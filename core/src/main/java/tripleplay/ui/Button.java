//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.SignalView;
import react.Slot;

/**
 * A button that displays text, or an icon, or both.
 */
public class Button extends AbstractTextButton<Button> implements Clickable<Button>
{
    /** Creates a button with no text or icon. */
    public Button () {
        this(null, (Icon)null);
    }

    /**  Creates a button with the supplied text. */
    public Button (String text) {
        this(text, (Icon)null);
    }

    /** Creates a button with the supplied icon. */
    public Button (Icon icon) {
        this(null, icon);
    }

    /** Creates a button with the supplied text and icon. */
    public Button (String text, Icon icon) {
        super(text, icon);
    }

    /** A convenience method for registering a click handler. Assumes you don't need the result of
     * {@link SignalView#connect}, because it throws it away. */
    public Button onClick (Slot<? super Button> onClick) {
        clicked().connect(onClick);
        return this;
    }

    @Override public SignalView<Button> clicked () {
        return ((Behavior.Click<Button>)_behave).clicked;
    }

    @Override public void click () {
        ((Behavior.Click<Button>)_behave).click();
    }

    @Override public String toString () {
        return "Button(" + text() + ")";
    }

    @Override protected Class<?> getStyleClass () {
        return Button.class;
    }

    @Override protected Behavior<Button> createBehavior () {
        return new Behavior.Click<Button>(this);
    }
}
