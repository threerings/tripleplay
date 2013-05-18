//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import playn.core.Keyboard;
import react.Value;
import react.ValueView;

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
    public abstract NativeTextField createNativeTextField ();

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
        @Override public NativeTextField createNativeTextField () {
            throw new UnsupportedOperationException();
        }
        @Override public void setVirtualKeyboardController (VirtualKeyboardController ctrl) { }
        @Override public void setVirtualKeyboardListener (Keyboard.Listener listener) { }
        @Override public ValueView<Boolean> virtualKeyboardActive () { return _false; }
        protected final Value<Boolean> _false = Value.create(false);
    }

    protected static TPPlatform _default = new Stub();
    protected static TPPlatform _instance = _default;
}
