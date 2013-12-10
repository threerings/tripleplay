//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import playn.core.Image;
import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;
import react.SignalView;
import react.UnitSignal;
import react.Value;
import react.ValueView;
import tripleplay.ui.Field;

/**
 * The entry point for per-platform services made available by TriplePlay. This is akin to the
 * mechanism used by PlayN for its per-platform backends, and must be configured in a similar way.
 * In your per-platform bootstrap class, you must initialize the appropriate TriplePlay platform
 * backend if you wish to make use of these services.
 *
 * <pre>{@code
 * public class TripleDemoJava {
 *   public static void main (String[] args) {
 *     JavaPlatform platform = JavaPlatform.register();
 *     JavaTPPlatform.register(platform);
 *     // etc.
 *   }
 * }
 * }</pre>
 */
public abstract class TPPlatform
{
    /** Returns the currently registered TPPlatform instance. */
    public static TPPlatform instance () {
        return _instance;
    }

    /**
     * Returns true if this platform supports native text fields.
     */
    public boolean hasNativeTextFields () {
        return false;
    }

    /**
     * Creates a native text field, if this platform supports it. If the platform requires it,
     * the field's styles may be resolved at this time.
     *
     * @exception UnsupportedOperationException thrown if the platform lacks support for native
     * text fields, use {@link #hasNativeTextFields} to check.
     */
    public NativeTextField createNativeTextField (Field.Native field) {
        throw new UnsupportedOperationException();
    }

    /**
     * Refreshes a native text field to match the current styles of its associated field instance.
     * Depending on the implementation, a new native field may be returned, or the given one
     * adjusted.
     */
    public NativeTextField refresh (NativeTextField previous) {
        throw new UnsupportedOperationException();
    }

    /** Sets the instance of KeyboardFocusController to use for keyboard focus management, or
     * null for none. */
    public void setKeyboardFocusController (KeyboardFocusController ctrl) {
        _kfc = ctrl;
    }

    /** Signal emitted when the user interacts with a native text field. This allows games to
     * qualify native text field usage as non-idle user behavior. */
    public SignalView<Void> keyboardActivity () {
        return _activity;
    }

    public ImageOverlay createImageOverlay (Image image) {
        throw new UnsupportedOperationException();
    }

    /** Gets a view of the Field that is currently in focus. Implemented in iOS and JRE if
     * {@link #hasNativeTextFields()}, otherwise remains null. Corresponds to the tripleplay
     * field currently receiving native keyboard input. */
    public ValueView<Field> focus () {
        return _focus;
    }

    /** Clears the currently focused field, if any. */
    public void clearFocus () {
    }

    /** Hides the native widgets under the given screen area, using platform clipping if
     * supported. The PlayN layers underneath the area should show through. Pointer events should
     * also not interact with the native widgets within the area.
     * @param area the new area to hide, in screen coordinates, or null to show all */
    public void hideNativeOverlays (IRectangle area) {
        if ((_hidden == null && area == null) ||
                (_hidden != null && _hidden.equals(area))) return;
        _hidden = area == null ? null : new Rectangle(area);
        updateHidden();
    }

    protected void updateHidden () {
    }

    /** Called by the static register methods in the per-platform backends. */
    static void register (TPPlatform instance) {
        if (_instance != _default) {
            throw new IllegalStateException("TPPlatform instance already registered.");
        }
        _instance = instance;
    }

    protected Value<Field> _focus = Value.create(null);
    protected KeyboardFocusController _kfc;
    protected UnitSignal _activity = new UnitSignal();
    protected Rectangle _hidden;

    protected static TPPlatform _default = new TPPlatform() {};
    protected static TPPlatform _instance = _default;
}
