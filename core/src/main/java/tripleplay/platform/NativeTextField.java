//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import pythagoras.f.IRectangle;

import playn.core.Font;
import playn.core.Keyboard;

import react.Signal;
import react.Value;

/**
 * Provides access to a platform-native text field, which can be overlaid onto a PlayN game, or
 * TPUI interface.
 */
public interface NativeTextField
{
    /** The current value of the text field. */
    Value<String> text ();

    /** A signal that is dispatched when the native text field has lost focus. Value is false if
     * editing was canceled */
    Signal<Boolean> finishedEditing();

    /** Configures the type of text expected to be entered in this field.
     * @return {@code this} for call chaining. */
    NativeTextField setTextType (Keyboard.TextType type);

    /** Configures the font used by the field to render text.
     * @return {@code this} for call chaining. */
    NativeTextField setFont (Font font);

    /** Configures the bounds of the native text field (in top-level screen coordinates).
     * @return {@code this} for call chaining. */
    NativeTextField setBounds (IRectangle bounds);

    /** Adds the field to the view. */
    void add ();

    /** Removes the field from the view. */
    void remove ();

    /** Request focus for the native text field */
    void focus ();
}
