package tripleplay.ui;

import playn.core.Image;
import playn.core.Pointer;
import pythagoras.f.Dimension;
import react.Signal;
import react.SignalView;
import react.Value;

/**
 * An item in a menu. This overrides clicking with a two phase click behavior: clicking an
 * unselected menu item selects it; clicking a selected menu item triggers it.
 */
public class MenuItem extends TogglableTextWidget<MenuItem>
{
    /** Modes of text display. */
    public enum ShowText {
        ALWAYS, NEVER, WHEN_ACTIVE
    }

    /** The text shown. */
    public final Value<String> text = Value.create(null);

    /** The icon shown. */
    public final Value<Image> icon = Value.create(null);

    /**
     * Creates a new menu item with the given label.
     */
    public MenuItem (String label) {
        this(label, null);
    }

    /**
     * Creates a new menu item with the given label and icon.
     */
    public MenuItem (String label, Image icon) {
        text.update(label);
        this.icon.update(icon);
        text.connect(textDidChange());
        this.icon.connect(iconDidChange());
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
     * Gets the signal that dispatches when a menu item is triggered.
     */
    public SignalView<MenuItem> triggered () { return _triggered; }

    @Override public SignalView<MenuItem> clicked () { return _clicked; }
    @Override protected Class<?> getStyleClass () { return MenuItem.class; }
    @Override protected void onClick () { _clicked.emit(this); }
    @Override protected Image icon () { return icon.get(); }
    @Override protected void onPointerStart (Pointer.Event event, float x, float y) {}
    @Override protected void onPointerDrag (Pointer.Event event, float x, float y) {}

    protected void trigger () { _triggered.emit(this); }

    @Override protected void onPointerEnd (Pointer.Event event, float x, float y) {
        if (contains(x, y)) {
            boolean isSelected = selected.get();
            if (!isSelected) {
                // click to select
                selected.update(true);
                onClick();
            }
            // trigger if this is the 2nd click -or- we always show text
            if (isSelected || _showText == ShowText.ALWAYS) trigger();
        }
    }

    @Override protected String text () {
        switch (_showText) {
        case NEVER:
            return "";
        case WHEN_ACTIVE:
            return selected.get() ? text.get() : "";
        case ALWAYS:
        default:
            return text.get();
        }
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new SizableLayoutData(super.createLayoutData(hintX, hintY), _preferredSize);
    }

    /** Dispatched when the item is triggered. */
    protected final Signal<MenuItem> _triggered = Signal.create();
    /** Dispatched when the item is clicked. */
    protected final Signal<MenuItem> _clicked = Signal.create();
    /** Size override. */
    protected final Dimension _preferredSize = new Dimension(0, 0);
    /** Text display mode. */
    protected ShowText _showText = ShowText.ALWAYS;
}
