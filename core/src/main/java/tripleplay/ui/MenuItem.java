//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Connection;
import playn.core.Image;
import playn.core.Pointer;
import pythagoras.f.Dimension;

import react.SignalView;
import react.Value;

/**
 * An item in a menu. This overrides clicking with a two phase click behavior: clicking an
 * unselected menu item selects it; clicking a selected menu item triggers it.
 */
public class MenuItem extends TextWidget<MenuItem> implements Togglable<MenuItem>
{
    /** Modes of text display. */
    public enum ShowText {
        ALWAYS, NEVER, WHEN_ACTIVE
    }

    /** The text shown. */
    public final Value<String> text = Value.create(null);

    /** The icon shown. */
    public final Value<Icon> icon = Value.create(null);

    /**
     * Creates a new menu item with the given label.
     */
    public MenuItem (String label) {
        this(label, (Icon)null);
    }

    /**
     * Creates a new menu item with the given label and icon.
     */
    @Deprecated
    public MenuItem (String label, Image icon) {
        this(label, Icons.image(icon));
    }

    /**
     * Creates a new menu item with the given label and icon.
     */
    public MenuItem (String label, Icon icon) {
        this.text.update(label);
        this.text.connect(textDidChange());
        // update after connect so we trigger iconDidChange, in case our icon is a not-ready-image
        this.icon.connect(iconDidChange());
        this.icon.update(icon);
    }

    /**
     * Sets the text display mode for this menu item.
     */
    public MenuItem showText (ShowText value) {
        _showText = value;
        invalidate();
        return this;
    }

    /**
     * Sets the menu item to show its text when the item is selected
     */
    public MenuItem hideTextWhenInactive () { return showText(ShowText.WHEN_ACTIVE); }

    /**
     * Sets the menu item to only use an icon and no tex. This is useful for layouts that show the
     * text of the selected item in a central location.
     */
    public MenuItem hideText () { return showText(ShowText.NEVER); }

    /**
     * Sets the preferred size of the menu item.
     */
    public MenuItem setPreferredSize (float wid, float hei) {
        _preferredSize.setSize(wid, hei);
        invalidate();
        return this;
    }

    /**
     * Gets the signal that dispatches when a menu item is triggered. Most callers will just
     * connect to {@link Menu#itemTriggered}.
     */
    public SignalView<MenuItem> triggered () {
        return ((Behavior.Toggle<MenuItem>)_behave).clicked;
    }

    // from Togglable and Clickable
    @Override public Value<Boolean> selected () {
        return ((Behavior.Toggle<MenuItem>)_behave).selected;
    }
    @Override public SignalView<MenuItem> clicked () {
        return ((Behavior.Toggle<MenuItem>)_behave).clicked;
    }
    @Override public void click () {
        ((Behavior.Toggle<MenuItem>)_behave).click();
    }

    protected void trigger () {
        ((Behavior.Toggle<MenuItem>)_behave).click();
    }

    protected void setRelay (Connection relay) {
        if (_relay != null) _relay.disconnect();
        _relay = relay;
    }

    @Override protected Class<?> getStyleClass () { return MenuItem.class; }
    @Override protected Icon icon () { return icon.get(); }

    @Override protected Behavior<MenuItem> createBehavior () {
        return new Behavior.Toggle<MenuItem>(this) {
            @Override public void onPointerStart (Pointer.Event event) {}
            @Override public void onPointerDrag (Pointer.Event event) {}
            @Override public void onPointerEnd (Pointer.Event event) {}
            @Override protected void onClick (Pointer.Event event) { click(); }
        };
    }

    @Override protected String text () {
        switch (_showText) {
        case NEVER:
            return "";
        case WHEN_ACTIVE:
            return isSelected() ? text.get() : "";
        case ALWAYS:
        default:
            return text.get();
        }
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new SizableLayoutData(super.createLayoutData(hintX, hintY), _preferredSize);
    }

    protected Connection _relay;

    /** Size override. */
    protected final Dimension _preferredSize = new Dimension(0, 0);
    /** Text display mode. */
    protected ShowText _showText = ShowText.ALWAYS;
}
