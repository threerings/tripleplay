//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import react.SignalView;
import react.Slot;
import react.Value;
import react.ValueView;

/**
 * A button that displays text, or an icon, or both.
 */
public class Button extends TextWidget<Button>
    implements Clickable<Button>
{
    /** @deprecated Use {@link Behavior.Click#DEBOUNCE_DELAY}. */
    @Deprecated public static Style<Integer> DEBOUNCE_DELAY = Behavior.Click.DEBOUNCE_DELAY;

    /** The text displayed by this widget, or null. */
    public final Value<String> text = Value.create((String)null);

    /** The icon displayed by this widget, or null. */
    public final Value<Icon> icon = Value.<Icon>create(null);

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

    /** Creates a button with the supplied icon. */
    @Deprecated
    public Button (Image icon) {
        this(null, Icons.image(icon));
    }

    /** Creates a button with the supplied text and icon. */
    @Deprecated
    public Button (String text, Image icon) {
        this(text, Icons.image(icon));
    }

    /** Creates a button with the supplied text and icon. */
    public Button (String text, Icon icon) {
        this.text.update(text);
        this.text.connect(textDidChange());
        // update after connect so we trigger iconDidChange, in case our icon is a not-ready-image
        this.icon.connect(iconDidChange());
        this.icon.update(icon);
    }

    /**
     * Binds the text of this button to the supplied reactive value. The current text will be
     * adjusted to match the state of {@code text}.
     */
    public Button bindText (ValueView<String> text) {
        text.connectNotify(this.text.slot());
        return this;
    }

    /**
     * Binds the icon of this button to the supplied reactive value. The current icon will be
     * adjusted to match the state of {@code icon}.
     */
    public Button bindIcon (ValueView<Icon> icon) {
        icon.connectNotify(this.icon.slot());
        return this;
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
        return "Button(" + text.get() + ")";
    }

    @Override protected Class<?> getStyleClass () {
        return Button.class;
    }

    @Override protected Behavior<Button> createBehavior () {
        return new Behavior.Click<Button>(this);
    }

    @Override protected String text () {
        return text.get();
    }

    @Override protected Icon icon () {
        return icon.get();
    }
}
