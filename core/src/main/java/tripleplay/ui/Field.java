//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.Keyboard;
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
            text = _nativeField.text();
            _finishedEditing = _nativeField.finishedEditing();
            _finishedEditing.connect(new Slot<Boolean>() {
                @Override public void onEmit (Boolean event) {
                    if (!fulltimeNativeField()) {
                        updateMode(false);
                    }
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
     */
    public Field setTextType (Keyboard.TextType type) {
        _textType = type;
        return this;
    }

    /**
     * Configures the label to be displayed when text is requested via a popup.
     */
    public Field setPopupLabel (String label) {
        _popupLabel = label;
        return this;
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

    @Override protected Image icon () {
        return null; // fields never have an icon
    }

    @Override protected void onPointerStart (Pointer.Event event, float x, float y) {
        super.onPointerStart(event, x, y);
        if (!isEnabled() || fulltimeNativeField()) return;

        if (_nativeField != null) {
            updateMode(true);
            _nativeField.focus();

        } else {
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

    @Override protected void wasRemoved () {
        super.wasRemoved();
        // make sure the field is gone
        updateMode(false);
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new TextLayoutData(hintX, hintY) {
            @Override public void layout (float left, float top, float width, float height) {
                super.layout(left, top, width, height);

                if (fulltimeNativeField()) {
                    updateMode(true);
                }
            }
        };
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
            _nativeField.setTextType(_textType).setFont(resolveStyle(Style.FONT)).
                setBounds(getNativeFieldBounds());
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

    protected final NativeTextField _nativeField;
    protected final Signal<Boolean> _finishedEditing;

    // used when popping up a text entry interface on mobile platforms
    protected Keyboard.TextType _textType = Keyboard.TextType.DEFAULT;
    protected String _popupLabel;
}
