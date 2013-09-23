//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import playn.core.Image;
import playn.core.Keyboard;
import pythagoras.f.IRectangle;
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
    public abstract boolean hasNativeTextFields ();

    /**
     * Creates a native text field, if this platform supports it.
     *
     * @exception UnsupportedOperationException thrown if the platform lacks support for native
     * text fields, use {@link #hasNativeTextFields} to check.
     */
    public abstract NativeTextField createNativeTextField (
        Field.Native field, NativeTextField.Mode mode);

    /**
     * Refreshes a native text field to match the given mode. Depending on the implementation,
     * if the mode is different, a new native field may be returned, or the given one adjusted.
     */
    public abstract NativeTextField refreshNativeTextField (
        NativeTextField previous, NativeTextField.Mode mode);

    /**
     * Sets the instance of VirtualKeyboardController to use for virtual keyboard management, or
     * null for none.
     */
    public abstract void setVirtualKeyboardController (VirtualKeyboardController ctrl);

    /**
     * Set a keyboard listener to receive onKeyTyped events when a native field is active.
     */
    public abstract void setVirtualKeyboardListener (Keyboard.Listener listener);

    /**
     * A value indicating whether the virtual keyboard is currently active or not (if the platform
     * has one).
     */
    public abstract ValueView<Boolean> virtualKeyboardActive ();

    public abstract ImageOverlay createImageOverlay (Image image);

    /** Called by the static register methods in the per-platform backends. */
    static void register (TPPlatform instance) {
        if (_instance != _default) {
            throw new IllegalStateException("TPPlatform instance already registered.");
        }
        _instance = instance;
    }

    protected static class Stub extends TPPlatform {
        @Override public boolean hasNativeTextFields () {
            return false;
        }
        @Override public NativeTextField createNativeTextField (
                Field.Native field, NativeTextField.Mode mode) {
            throw new UnsupportedOperationException();
        }
        @Override public NativeTextField refreshNativeTextField (
                NativeTextField previous, NativeTextField.Mode mode) {
            throw new UnsupportedOperationException();
        }
        @Override public void setVirtualKeyboardController (VirtualKeyboardController ctrl) { }
        @Override public void setVirtualKeyboardListener (Keyboard.Listener listener) { }
        @Override public ValueView<Boolean> virtualKeyboardActive () { return _false; }
        @Override public ImageOverlay createImageOverlay (final Image image) {
            return new ImageOverlay() {
                @Override public void setBounds (IRectangle bounds) {}
                @Override public void add () {}
                @Override public void remove () {}
                @Override public Image image () { return image; }
                @Override public void repaint () {}
            };
        }
        protected final Value<Boolean> _false = Value.create(false);
    }

    protected static TPPlatform _default = new Stub();
    protected static TPPlatform _instance = _default;
}
