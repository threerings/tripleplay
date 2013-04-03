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

/**
 * Displays text which can be edited via the {@link Keyboard#getText} popup.
 */
public class Field extends TextWidget<Field>
{
    /** If on a platform that utilizes native fields and this is true, the native field is
     * displayed whenever this Field is visible, and the native field is responsible for all text
     * rendering. If false, the native field is only displayed while actively editing (after a user
     * click). */
    public static final Style<Boolean> FULLTIME_NATIVE_FIELD = Style.newStyle(false, true);

    /** Controls the behavior of native text fields with respect to auto-capitalization on
     * platforms that support it. */
    // TODO: iOS supports multiple styles of autocap, support them here?
    public static final Style<Boolean> AUTOCAPITALIZATION = Style.newStyle(false, true);

    /** Controls the behavior of native text fields with respect to auto-correction on platforms
     * that support it. */
    public static final Style<Boolean> AUTOCORRECTION = Style.newStyle(false, true);

    /** Controls secure text entry on native text fields: typically this will mean dots or asterix
     * displayed instead of the typed character. */
    public static final Style<Boolean> SECURE_TEXT_ENTRY = Style.newStyle(false, false);

    /** Sets the Keyboard.TextType in use by this Field. */
    public static final Style<TextType> TEXT_TYPE = Style.newStyle(false, TextType.DEFAULT);

    /** Sets the maximum number of characters that can be entered by this Field. Currently only
     * supported by Native fields. Anything less than 1 indicates unlimited (or limited by the
     * platform only). */
    public static final Style<Integer> MAXIMUM_INPUT_LENGTH = Style.newStyle(false, 0);

    /** Sets the label used on the "return" key of the virtual keyboard on native keyboards. Be
     * aware that some platforms (such as iOS) have a limited number of options. The underlying
     * native implementation is responsible for attempting to match this style, but may be unable
     * to do so. Defaults to null (uses platform default). */
    public static final Style<String> RETURN_KEY_LABEL = Style.newStyle(false, null);

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
        enableInteraction();
        setStyles(styles);

        if (TPPlatform.instance().hasNativeTextFields()) {
            _nativeField = TPPlatform.instance().createNativeTextField();
            // set our default validator and transformer
            setValidator(null);
            setTransformer(null);
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
     * Configures the keyboard type to use when text is requested via a popup.
     * @deprecated Use the TEXT_TYPE Style instead.
     */
    @Deprecated public Field setTextType (Keyboard.TextType type) {
        return addStyles(TEXT_TYPE.is(type));
    }

    /**
     * Configures the label to be displayed when text is requested via a popup.
     */
    public Field setPopupLabel (String label) {
        _popupLabel = label;
        return this;
    }

    /**
     * Set the text field validator for use with this field. The default is to go through
     * {@link #textIsValid(String)}. Pass in null to set/restore the default validator.
     *
     * Note that setting a custom validator will bypass checking of the
     * {@link #MAXIMUM_INPUT_LENGTH} style.
     *
     * @return this for call chaining.
     */
    public Field setValidator (NativeTextField.Validator validator) {
        if (_nativeField != null)
            _nativeField.setValidator(validator == null ? _defaultValidator : validator);
        return this;
    }

    /**
     * Set the text field transformer for use with this field. The default is to go through
     * {@link #transformText(String)}. Pass in null to set/restore the default transformer.
     *
     * @return this for call chaining.
     */
    public Field setTransformer (NativeTextField.Transformer transformer) {
        if (_nativeField != null)
            _nativeField.setTransformer(transformer == null ? _defaultTransformer : transformer);
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

    /**
     * Used with native fields. Returning false form this method will cancel a text edit from the
     * user. The default implementation supplied here honors the MAXIMUM_INPUT_LENGTH Field style.
     */
    protected boolean textIsValid (String text) {
        int maxLength = _maxFieldLength;
        int textLength = text == null ? 0 : text.length();
        return maxLength < 1 || textLength <= maxLength;
    }

    /**
     * Called when the native field's value is changed. Override and return a modified value to
     * perform text transformation while the user is editing the field.
     */
    protected String transformText (String text) {
        return text;
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

    @Override protected void onPointerStart (Pointer.Event event, float x, float y) {
        super.onPointerStart(event, x, y);
        if (!isEnabled() || fulltimeNativeField()) return;

        if (_nativeField != null) {
            updateMode(true);
            _nativeField.focus();

        } else {
            PlayN.keyboard().getText(resolveStyle(TEXT_TYPE), _popupLabel, text.get(),
                new Callback<String>() { @Override public void onSuccess (String result) {
                    // null result is a canceled entry dialog
                    if (result != null) text.update(result);
                    _finishedEditing.emit(result != null);
                }
                @Override public void onFailure (Throwable cause) { /* noop */ }
            });
        }
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        // make sure the field is gone
        updateMode(false);
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new FieldLayoutData(hintX, hintY);
    }

    protected boolean fulltimeNativeField () {
        return _nativeField != null && resolveStyle(FULLTIME_NATIVE_FIELD);
    }

    protected Rectangle getNativeFieldBounds () {
        // TODO: handle alignments other than HAlign.LEFT and VAlign.TOP
        Background bg = resolveStyle(Style.BACKGROUND);
        Point screenCoords = Layer.Util.layerToScreen(layer, bg.left, bg.top);
        return new Rectangle(screenCoords.x, screenCoords.y,
                             _size.width - bg.width(), _size.height - bg.height());
    }

    protected void updateMode (boolean nativeField) {
        if (_nativeField == null) return;
        if (nativeField) {
            _nativeField.setTextType(resolveStyle(TEXT_TYPE))
                .setFont(resolveStyle(Style.FONT))
                .setAutocapitalization(resolveStyle(AUTOCAPITALIZATION))
                .setAutocorrection(resolveStyle(AUTOCORRECTION))
                .setSecureTextEntry(resolveStyle(SECURE_TEXT_ENTRY))
                .setReturnKeyLabel(resolveStyle(RETURN_KEY_LABEL));
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

    protected class FieldLayoutData extends TextLayoutData
    {
        public FieldLayoutData (float hintX, float hintY) {
            super(hintX, hintY);
        }

        @Override  public void layout (float left, float top, float width, float height) {
            super.layout(left, top, width, height);
            if (fulltimeNativeField()) updateMode(true);

            // make sure our cached value is up to date
            _maxFieldLength = resolveStyle(MAXIMUM_INPUT_LENGTH);
        }
    }

    protected final NativeTextField _nativeField;
    protected final NativeTextField.Transformer _defaultTransformer =
        new NativeTextField.Transformer() {
            @Override public String transform (String text) { return transformText(text); }
        };
    protected final NativeTextField.Validator _defaultValidator =
        new NativeTextField.Validator() {
            @Override public boolean isValid (String text) { return textIsValid(text); }
        };
    protected final Signal<Boolean> _finishedEditing;

    // used when popping up a text entry interface on mobile platforms
    protected String _popupLabel;

    // Set via the MAXIMUM_INPUT_LENGTH style during widget validation
    protected int _maxFieldLength = 0;
}
