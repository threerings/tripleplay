//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Keyboard;
import playn.core.Keyboard.TextType;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.util.Callback;

import pythagoras.f.Point;
import pythagoras.f.Rectangle;

import react.Signal;
import react.SignalView;
import react.Slot;
import react.Value;

import tripleplay.platform.NativeTextField;
import tripleplay.platform.TPPlatform;
import tripleplay.ui.util.Insets;

/**
 * Displays text which can be edited via the {@link Keyboard#getText} popup.
 */
public class Field extends TextWidget<Field>
{
    /** Creates a style binding for the given maximum lenght. */
    public static Style.Binding<Validator> maxLength (int max) {
        return VALIDATOR.is(new MaxLength(max));
    }

    /** Shim class for native text field to get at styles. */
    public static abstract class Native {
        /** The field. */
        public final Field field;

        /** Creates the shim around the given field. */
        protected Native (Field field) {
            this.field = field;
        }

        /** Resolves the given style for the field. */
        protected <T> T resolveStyle (Style<T> style) {
            return field.resolveStyle(style);
        }

        /** Tests if the proposed text is valid. */
        protected boolean isValid (String text) {
            return field.textIsValid(text);
        }

        /** Transforms the given text. */
        protected String transform (String text) {
            return field.transformText(text);
        }
    }

    /** For native text fields, decides whether to block a keypress based on the proposed content
     * of the field. */
    public interface Validator {
        /** Return false if the keypress causing this text should be blocked. */
        boolean isValid (String text);
    }

    /** For native text fields, transforms text during typing. */
    public interface Transformer {
        /** Transform the specified text. */
        public String transform (String text);
    }

    /** Blocks keypresses for a native text field when the length is at a given maximum. */
    public static class MaxLength implements Validator {
        /** The maximum length accepted. */
        public final int max;
        public MaxLength (int max) {
            this.max = max;
        }
        @Override public boolean isValid (String text) {
            return text.length() <= max;
        }
    }

    /** If on a platform that utilizes native fields and this is true, the native field is
     * displayed whenever this Field is visible, and the native field is responsible for all text
     * rendering. If false, the native field is only displayed while actively editing (after a user
     * click). */
    public static final Style.Flag FULLTIME_NATIVE_FIELD = Style.newFlag(false, true);

    /** Controls the behavior of native text fields with respect to auto-capitalization on
     * platforms that support it. */
    // TODO: iOS supports multiple styles of autocap, support them here?
    public static final Style.Flag AUTOCAPITALIZATION = Style.newFlag(false, true);

    /** Controls the behavior of native text fields with respect to auto-correction on platforms
     * that support it. */
    public static final Style.Flag AUTOCORRECTION = Style.newFlag(false, true);

    /** Controls secure text entry on native text fields: typically this will mean dots or asterix
     * displayed instead of the typed character. */
    public static final Style.Flag SECURE_TEXT_ENTRY = Style.newFlag(false, false);

    /** Sets the Keyboard.TextType in use by this Field. */
    public static final Style<TextType> TEXT_TYPE = Style.newStyle(false, TextType.DEFAULT);

    /** Sets the validator to use when censoring keypresses into native text fields.
     * @see MaxLength */
    public static final Style<Validator> VALIDATOR = Style.newStyle(true, null);

    /** Sets the transformner to use when updating native text fields while being typed into. */
    public static final Style<Transformer> TRANSFORMER = Style.newStyle(true, null);

    /** Sets the label used on the "return" key of the virtual keyboard on native keyboards. Be
     * aware that some platforms (such as iOS) have a limited number of options. The underlying
     * native implementation is responsible for attempting to match this style, but may be unable
     * to do so. Defaults to null (uses platform default). */
    public static final Style<String> RETURN_KEY_LABEL = Style.newStyle(false, null);

    /** Sets the field to allow the return key to insert a line break in the text. */
    public static final Style.Flag MULTILINE = Style.newFlag(false, false);

    /** The text displayed by this widget. */
    public final Value<String> text;

    public Field () {
        this("");
    }

    public Field (String initialText) {
        this(initialText, Styles.none());
    }

    public Field (Styles styles) {
        this("", styles);
    }

    public Field (String initialText, Styles styles) {
        setStyles(styles);

        if (TPPlatform.instance().hasNativeTextFields()) {
            _nativeField = TPPlatform.instance().createNativeTextField(this);
            text = _nativeField.text();
            _finishedEditing = _nativeField.finishedEditing();
            _finishedEditing.connect(new Slot<Boolean>() {
                @Override public void onEmit (Boolean event) {
                    if (!fulltimeNativeField()) updateMode(false);
                }
            });
        } else {
            _nativeField = null;
            text = Value.create("");
            _finishedEditing = Signal.create();
        }
        this.text.update(initialText);
        this.text.connect(textDidChange());
    }

    /** Returns a signal that is dispatched when text editing is complete. */
    public SignalView<Boolean> finishedEditing () {
        return _finishedEditing;
    }

    /**
     * Configures the label to be displayed when text is requested via a popup.
     */
    public Field setPopupLabel (String label) {
        _popupLabel = label;
        return this;
    }

    /**
     * Forcibly notify the NativeTextField backing this field that its screen position has changed.
     *
     * @return this for call chaining.
     */
    public Field updateNativeFieldBounds () {
        if (_nativeField != null) _nativeField.setBounds(getNativeFieldBounds());
        return this;
    }

    /**
     * Returns true if this Field is backed by a native field and that field currently has keyboard
     * (virtual or physical) focus.
     */
    public boolean hasFocus () {
        return _nativeField != null && _nativeField.hasFocus();
    }

    @Override public Field setVisible (boolean visible) {
        if (_nativeField != null) {
            if (visible) {
                _nativeField.add();
            } else {
                _nativeField.remove();
            }
        }
        return super.setVisible(visible);
    }

    /**
     * Main entry point for deciding whether to reject keypresses on a native field. By default,
     * consults the current validator instance, set up by {@link #VALIDATOR}.
     */
    protected boolean textIsValid (String text) {
        return _validator == null || _validator.isValid(text);
    }

    /**
     * Called when the native field's value is changed. Override and return a modified value to
     * perform text transformation while the user is editing the field. By default, consults
     * the current transformer instance, set up by {@link #TRANSFORMER}.
     */
    protected String transformText (String text) {
        return _transformer == null ? text : _transformer.transform(text);
    }

    @Override protected Class<?> getStyleClass () {
        return Field.class;
    }

    @Override protected String text () {
        String ctext = text.get();
        // we always want non-empty text so that we force ourselves to always have a text layer and
        // sane dimensions even if the text field contains no text
        return (ctext == null || ctext.length() == 0) ? " " : ctext;
    }

    @Override protected Icon icon () {
        return null; // fields never have an icon
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        // make sure the field is gone
        updateMode(false);
    }

    @Override protected Behavior<Field> createBehavior () {
        return new Behavior<Field>(this) {
            @Override public void onPointerStart (Pointer.Event event) {
                super.onPointerStart(event);
                if (!isEnabled() || fulltimeNativeField()) return;
                if (_nativeField != null) event.capture();
                startEdit();
            }
        };
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new FieldLayoutData(hintX, hintY);
    }

    protected void startEdit () {
        if (_nativeField != null) {
            updateMode(true);
            _nativeField.focus();

        } else {
            // TODO: multi-line keyboard.getText
            PlayN.keyboard().getText(_textType, _popupLabel, text.get(), new Callback<String>() {
                @Override public void onSuccess (String result) {
                    // null result is a canceled entry dialog
                    if (result != null) text.update(result);
                    _finishedEditing.emit(result != null);
                }
                @Override public void onFailure (Throwable cause) { /* noop */ }
            });
        }
    }

    protected boolean fulltimeNativeField () {
        return _nativeField != null && resolveStyle(FULLTIME_NATIVE_FIELD);
    }

    protected Rectangle getNativeFieldBounds () {
        // TODO: handle alignments other than HAlign.LEFT and VAlign.TOP
        Insets insets = resolveStyle(Style.BACKGROUND).insets;
        Point screenCoords = Layer.Util.layerToScreen(layer, insets.left(), insets.top());
        return new Rectangle(screenCoords.x, screenCoords.y,
                             _size.width - insets.width(), _size.height - insets.height());
    }

    protected void updateMode (boolean nativeField) {
        if (_nativeField == null) return;
        if (nativeField) {
            NativeTextField.Mode mode = NativeTextField.Mode.NORMAL;
            boolean multiLine = resolveStyle(MULTILINE);
            if (resolveStyle(SECURE_TEXT_ENTRY)) {
                if (multiLine) Log.log.warning("Ignoring MULTILINE Style");
                mode = NativeTextField.Mode.SECURE;
            } else if (multiLine) {
                mode = NativeTextField.Mode.MULTI_LINE;
            }

            NativeTextField newField = _nativeField.refreshMode(mode);
            if (newField != _nativeField) {
                _nativeField.remove();
                _nativeField = newField;
            }
            _nativeField.validateStyles();
            _nativeField.setEnabled(isEnabled());
            updateNativeFieldBounds();
            _nativeField.add();
            setGlyphLayerAlpha(0);
        } else {
            _nativeField.remove();
            setGlyphLayerAlpha(1);
        }
    }

    protected void setGlyphLayerAlpha (float alpha) {
        if (_tglyph.layer() != null) _tglyph.layer().setAlpha(alpha);
    }

    protected class FieldLayoutData extends TextLayoutData {
        public FieldLayoutData (float hintX, float hintY) {
            super(hintX, hintY);
        }

        @Override public void layout (float left, float top, float width, float height) {
            super.layout(left, top, width, height);
            if (fulltimeNativeField()) updateMode(true);
            else if (_nativeField != null) _nativeField.validateStyles();

            // make sure our cached bits are up to date
            _validator = resolveStyle(VALIDATOR);
            _transformer = resolveStyle(TRANSFORMER);
            _textType = resolveStyle(TEXT_TYPE);
        }
    }

    protected NativeTextField _nativeField;
    protected Validator _validator;
    protected Transformer _transformer;
    protected TextType _textType;
    protected final Signal<Boolean> _finishedEditing;

    // used when popping up a text entry interface on mobile platforms
    protected String _popupLabel;
}
