//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Graphics;
import playn.core.Keyboard;
import playn.scene.Layer;
import playn.scene.LayerUtil;
import playn.scene.Pointer;

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
    /** Creates a style binding for the given maximum length. */
    public static Style.Binding<Validator> maxLength (int max) {
        return VALIDATOR.is(new MaxLength(max));
    }

    /** Checks if the platform has native text fields. */
    public static boolean hasNative () {
        return TPPlatform.instance().hasNativeTextFields();
    }

    /** Exposes protected field information required for native fields. */
    public final class Native {
        /** Resolves the given style for the field. */
        public <T> T resolveStyle (Style<T> style) {
            return Field.this.resolveStyle(style);
        }

        /** Tests if the proposed text is valid. */
        public boolean isValid (String text) {
            return Field.this.textIsValid(text);
        }

        /** Transforms the given text. */
        public String transform (String text) {
            return Field.this.transformText(text);
        }

        /** A signal that is dispatched when the native text field has lost focus. Value is false if
         * editing was canceled */
        public Signal<Boolean> finishedEditing () {
            return _finishedEditing;
        }

        /** Refreshes the bounds of this field's native field. Used as a platform callback to
         * support some degree of animation for UI containing native fields. */
        public void refreshBounds () {
            updateNativeFieldBounds();
        }

        public Field field () {
            return Field.this;
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
    public static final Style<Keyboard.TextType> TEXT_TYPE = Style.newStyle(
        false, Keyboard.TextType.DEFAULT);

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

        text = Value.create("");
        _finishedEditing = Signal.create();

        if (hasNative()) {
            _finishedEditing.connect(new Slot<Boolean>() {
                @Override public void onEmit (Boolean event) {
                    if (!_fullTimeNative) updateMode(false);
                }
            });
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

    /** Attempt to focus on this field, if it is backed by a native field. If the platform
     * uses a virtual keyboard, this will cause it slide up, just as though the use had tapped
     * the field. For hardware keyboard, a blinking caret will appear in the field. */
    public void focus () {
        if (_nativeField != null) _nativeField.focus();
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

    /** Returns this field's native text field, if it has one, otherwise null. */
    public NativeTextField exposeNativeField () {
        return _nativeField;
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
        return new Behavior.Select<Field>(this) {
            @Override public void onClick (Pointer.Interaction iact) {
                if (!_fullTimeNative) startEdit();
            }
        };
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new FieldLayoutData(hintX, hintY);
    }

    protected void startEdit () {
        if (hasNative()) {
            updateMode(true);
            _nativeField.focus();

        } else {
            // TODO: multi-line keyboard.getText
            root().iface.plat.input().getText(_textType, _popupLabel, text.get()).
                onSuccess(new Slot<String>() {
                    @Override public void onEmit (String result) {
                        // null result is a canceled entry dialog
                        if (result != null) text.update(result);
                        _finishedEditing.emit(result != null);
                    }
                });
        }
    }

    protected Rectangle getNativeFieldBounds () {
        Insets insets = resolveStyle(Style.BACKGROUND).insets;
        Point screenCoords = LayerUtil.layerToScreen(layer, insets.left(), insets.top());
        return new Rectangle(screenCoords.x, screenCoords.y,
                             _size.width - insets.width(), _size.height - insets.height());
    }

    protected void updateMode (boolean nativeField) {
        if (!hasNative()) return;
        if (nativeField) {
            _nativeField = _nativeField == null ?
                TPPlatform.instance().createNativeTextField(new Native()) :
                TPPlatform.instance().refresh(_nativeField);

            _nativeField.setEnabled(isEnabled());
            updateNativeFieldBounds();
            _nativeField.add();
            setGlyphLayerVisible(false);
        } else if (_nativeField != null) {
            _nativeField.remove();
            setGlyphLayerVisible(true);
        }
    }

    protected void setGlyphLayerVisible (boolean visible) {
        if (_tglyph.layer() != null) _tglyph.layer().setVisible(visible);
    }

    protected class FieldLayoutData extends TextLayoutData {
        public FieldLayoutData (float hintX, float hintY) {
            super(hintX, hintY);
        }

        @Override public void layout (float left, float top, float width, float height) {
            super.layout(left, top, width, height);
            _fullTimeNative = hasNative() && resolveStyle(FULLTIME_NATIVE_FIELD);
            if (_fullTimeNative || _nativeField != null) updateMode(true);

            // make sure our cached bits are up to date
            _validator = resolveStyle(VALIDATOR);
            _transformer = resolveStyle(TRANSFORMER);
            _textType = resolveStyle(TEXT_TYPE);
        }
    }

    protected NativeTextField _nativeField;
    protected Validator _validator;
    protected Transformer _transformer;
    protected Keyboard.TextType _textType;
    protected boolean _fullTimeNative;
    protected final Signal<Boolean> _finishedEditing;

    // used when popping up a text entry interface on mobile platforms
    protected String _popupLabel;
}
