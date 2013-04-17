//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
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
    public interface Validator {
        /** Return false if the text is not valid for any reason. */
        boolean isValid (String text);
    }

    public interface Transformer {
        /** Transform the specified text in some way, or simply return the text untransformed. */
        public String transform (String text);
    }

    /** A native text field be in one of three modes. In general, modes correspond to different
     * underlying native classes, and must be refreshed prior to other set methods using
     * {@link NativeTextField#refreshMode(Mode)}. */
    public enum Mode {
        /** Single line, visible text. */
        NORMAL,
        /** Single line, text obscured. */
        SECURE,
        /** Mutliple lines. */
        MULTI_LINE
    }

    /** The current value of the text field. */
    Value<String> text ();

    /** A signal that is dispatched when the native text field has lost focus. Value is false if
     * editing was canceled */
    Signal<Boolean> finishedEditing ();

    /** Sets the validator for use with this native field.
     * @return {@code this} for call chaining. */
    NativeTextField setValidator (Validator validator);

    /** Sets the transformer for use with this native field.
     * @return {@code this} for call chaining. */
    NativeTextField setTransformer (Transformer transformer);

    /** Configures the type of text expected to be entered in this field.
     * @return {@code this} for call chaining. */
    NativeTextField setTextType (Keyboard.TextType type);

    /** Configures the font used by the field to render text.
     * @return {@code this} for call chaining. */
    NativeTextField setFont (Font font);

    /** Configures the bounds of the native text field (in top-level screen coordinates).
     * @return {@code this} for call chaining. */
    NativeTextField setBounds (IRectangle bounds);

    /** Configures the autocapitalization behavior of the field on a virtual keyboard.
     * @return {@code this} for call chaining. */
    NativeTextField setAutocapitalization (boolean useAutocapitalization);

    /** Configures the autocorrection behavior of the field on a virtual keyboard.
     * @return {@code this} for call chaining. */
    NativeTextField setAutocorrection (boolean useAutocorrection);

    /** Configures the label of the return key on virtual keyboards. Underlying platform is
     * responsible for attempting to match this value as well as it is able. Null indicates platform
     * default.
     * @return {@code this} for call chaining. */
    NativeTextField setReturnKeyLabel (String label);

    /**
     * Sets the enabled state of the field.
     */
    NativeTextField setEnabled (boolean enabled);

    /** Updates or creates a new native text field with the given mode. If a value != this is
     * returned, the caller must then repopulate all the properties of the fields.*/
    NativeTextField refreshMode (Mode mode);

    /** Adds the field to the view. */
    void add ();

    /** Removes the field from the view. */
    void remove ();

    /** Request focus for the native text field */
    void focus ();

    /** Returns true if this native text field currently has focus. */
    boolean hasFocus ();
}
