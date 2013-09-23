//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

/**
 * Provides access to a platform-native text field, which can be overlaid onto a PlayN game.
 * A TP Field is required for integration. See {@link TPPlatform#createNativeTextField(
 * tripleplay.ui.Field.Native, Mode)}.
 */
public interface NativeTextField extends NativeOverlay
{
    /**
     * Modes for native text fields. The set of modes corresponds to all states that may change
     * the class of the underlying implementation for one or more platforms. For example, Java has
     * a class for password fields, while iOS uses a property.
     */
    public enum Mode {
        /** Single line, visible text. */
        NORMAL,
        /** Single line, text obscured. */
        SECURE,
        /** Mutliple lines. */
        MULTI_LINE
    }

    /** Updates native styles to match those currently applied to the field. */
    void validateStyles ();

    /** Sets the enabled state of the field. */
    void setEnabled (boolean enabled);

    /** Request focus for the native text field */
    void focus ();

    /** Returns true if this native text field currently has focus. */
    boolean hasFocus ();
}
