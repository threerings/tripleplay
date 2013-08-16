//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import pythagoras.f.IRectangle;

import react.Signal;
import react.Value;
import tripleplay.ui.Field;

/**
 * Provides access to a platform-native text field, which can be overlaid onto a PlayN game, or
 * TPUI interface.
 */
public abstract class NativeTextField extends Field.Native
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
    public abstract Value<String> text ();

    /** A signal that is dispatched when the native text field has lost focus. Value is false if
     * editing was canceled */
    public abstract Signal<Boolean> finishedEditing ();

    /** Updates native styles to match those currently applied to the field. */
    public abstract void validateStyles ();

    /** Sets the validator for use with this native field.
     * @return {@code this} for call chaining. */
    public abstract void setValidator (Validator validator);

    /** Sets the transformer for use with this native field.
     * @return {@code this} for call chaining. */
    public abstract void setTransformer (Transformer transformer);

    /** Configures the bounds of the native text field (in top-level screen coordinates).
     * @return {@code this} for call chaining. */
    public abstract void setBounds (IRectangle bounds);

    /**
     * Sets the enabled state of the field.
     */
    public abstract void setEnabled (boolean enabled);

    /** Updates or creates a new native text field with the given mode. If a value != this is
     * returned, the caller must then repopulate all the properties of the fields.*/
    public abstract NativeTextField refreshMode (Mode mode);

    /** Adds the field to the view. */
    public abstract void add ();

    /** Removes the field from the view. */
    public abstract void remove ();

    /** Request focus for the native text field */
    public abstract void focus ();

    /** Returns true if this native text field currently has focus. */
    public abstract boolean hasFocus ();

    protected NativeTextField (Field field) {
        super(field);
    }
}
