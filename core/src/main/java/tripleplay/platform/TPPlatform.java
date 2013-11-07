//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import playn.core.Image;
import playn.core.Keyboard;
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
     * Creates a native text field, if this platform supports it.
     *
     * @exception UnsupportedOperationException thrown if the platform lacks support for native
     * text fields, use {@link #hasNativeTextFields} to check.
     */
    public NativeTextField createNativeTextField (
        Field.Native field, NativeTextField.Mode mode) {
        throw new UnsupportedOperationException();
    }

    /**
     * Refreshes a native text field to match the given mode. Depending on the implementation,
     * if the mode is different, a new native field may be returned, or the given one adjusted.
     */
    public NativeTextField refreshNativeTextField (
            NativeTextField previous, NativeTextField.Mode mode) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the instance of VirtualKeyboardController to use for virtual keyboard management, or
     * null for none.
     */
    public void setVirtualKeyboardController (VirtualKeyboardController ctrl) {}

    /**
     * Set a keyboard listener to receive onKeyTyped events when a native field is active.
     */
    public void setVirtualKeyboardListener (Keyboard.Listener listener) {}

    public ImageOverlay createImageOverlay (Image image) {
        throw new UnsupportedOperationException();
    }

    /** Gets a view of the Field that is currently in focus. Implemented in iOS and JRE if
     * {@link #hasNativeTextFields()}, otherwise remains null. Corresponds to the tripleplay
     * field currently receiving native keyboard input. */
    public ValueView<Field> focus () {
        return _focus;
    }

    /** Called by the static register methods in the per-platform backends. */
    static void register (TPPlatform instance) {
        if (_instance != _default) {
            throw new IllegalStateException("TPPlatform instance already registered.");
        }
        _instance = instance;
    }

    protected Value<Field> _focus = Value.create(null);

    protected static TPPlatform _default = new TPPlatform() {};
    protected static TPPlatform _instance = _default;
}
