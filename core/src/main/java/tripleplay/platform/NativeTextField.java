//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

/**
 * Provides access to a platform-native text field, which can be overlaid onto a PlayN game.
 * A TP Field is required for integration. See {@link TPPlatform#createNativeTextField(
 * tripleplay.ui.Field.Native)}.
 */
public interface NativeTextField extends NativeOverlay
{
    /** Sets the enabled state of the field. */
    void setEnabled (boolean enabled);

    /** Request focus for the native text field */
    void focus ();

    /** Inserts the given text at the current caret position, or if there is a selected region,
     * replaces the region with the given text.
     * @return true if the operation was successful */
    boolean insert (String text);
}
