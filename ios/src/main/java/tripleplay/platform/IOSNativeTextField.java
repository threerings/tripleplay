//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import cli.MonoTouch.Foundation.NSRange;
import cli.MonoTouch.Foundation.NSString;
import cli.MonoTouch.UIKit.UIFont;
import cli.MonoTouch.UIKit.UIKeyboardType;
import cli.MonoTouch.UIKit.UITextAutocapitalizationType;
import cli.MonoTouch.UIKit.UITextAutocorrectionType;
import cli.MonoTouch.UIKit.UITextField;
import cli.MonoTouch.UIKit.UITextFieldDelegate;
import cli.System.Drawing.RectangleF;

import pythagoras.f.IRectangle;

import playn.core.Font;
import playn.core.Keyboard;
import react.Signal;
import react.Slot;
import react.Value;

public class IOSNativeTextField implements NativeTextField
{
    public IOSNativeTextField (IOSTextFieldHandler handler) {
        _handler = handler;
        _field = new UITextField();

        // all fields close the keyboard when the return key is used
        _field.set_Delegate(new UITextFieldDelegate() {
            @Override public boolean ShouldReturn (UITextField field) {
                _pressedReturn = true;
                field.ResignFirstResponder();
                return false;
            }
            @Override public boolean ShouldChangeCharacters (UITextField uiTextField,
                NSRange nsRange, String s) {
                if (_maxInputLength < 1) return true;
                int newLength = new NSString(uiTextField.get_Text())
                    .Replace(nsRange, new NSString(s)).get_Length();
                return newLength <= _maxInputLength;
            }
        });

        _text.connect(new Slot<String>() {
            @Override public void onEmit (String value) {
                String current = _field.get_Text();
                if (current == null || !current.equals(value)) _field.set_Text(value);
            }
        });
    }

    @Override public Value<String> text () {
        return _text;
    }

    @Override public Signal<Boolean> finishedEditing () {
        return _finishedEditing;
    }

    @Override public IOSNativeTextField setTextType (Keyboard.TextType type) {
        switch (type) {
        case NUMBER:
            _field.set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.NumberPad));
            break;
        case EMAIL:
            _field.set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.EmailAddress));
            break;
        case URL:
            _field.set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.Url));
            break;
        case DEFAULT:
            _field.set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.Default));
            break;
        }
        return this;
    }

    @Override public IOSNativeTextField setFont (Font font) {
        _field.set_Font(_handler.getUIFont(font));
        updateBounds();
        return this;
    }

    @Override public IOSNativeTextField setBounds (IRectangle bounds) {
        _requestedBounds = bounds;
        updateBounds();
        return this;
    }

    @Override public IOSNativeTextField setAutocapitalization (boolean enable) {
        _field.set_AutocapitalizationType(UITextAutocapitalizationType.wrap(
            enable ? UITextAutocapitalizationType.Sentences : UITextAutocapitalizationType.None));
        return this;
    }

    @Override public IOSNativeTextField setAutocorrection (boolean enable) {
        _field.set_AutocorrectionType(UITextAutocorrectionType.wrap(
            enable ? UITextAutocorrectionType.Yes : UITextAutocorrectionType.No));
        return this;
    }

    @Override public IOSNativeTextField setSecureTextEntry (boolean enable) {
        _field.set_SecureTextEntry(enable);
        return this;
    }

    @Override public IOSNativeTextField setMaxInputLength (int maxLength) {
        _maxInputLength = maxLength;
        return this;
    }

    @Override public void add () {
        if (!_handler.isAdded(_field)) _handler.activate(this);
    }

    @Override public void remove () {
        if (_handler.isAdded(_field)) _handler.deactivate(this);
    }

    @Override public void focus () {
        _field.BecomeFirstResponder();
    }

    protected void updateBounds () {
        if (_requestedBounds == null) return;

        UIFont font = _field.get_Font();
        // field fudged to the left 1 pixel to match PlayN text rendering.
        RectangleF fieldBounds = new RectangleF(_requestedBounds.x() + 1, _requestedBounds.y(),
            _requestedBounds.width(), _requestedBounds.height());
        if (fieldBounds.get_Height() < font.get_LineHeight()) {
            // ensure we're tall enough for a single line of text and the text cursor
            fieldBounds.set_Height(font.get_LineHeight());
        }
        // offset upwards because the text field has some built in vertical padding we want to
        // ignore. The difference between the lineHeight and the ascender + descender (which is
        // negative) seems to represent this pretty well.
        fieldBounds.set_Y(Math.round(fieldBounds.get_Y() -
            (font.get_LineHeight() - (font.get_Ascender() - font.get_Descender()))));
        _field.set_Frame(fieldBounds);
    }

    protected void didFinish () {
        _finishedEditing.emit(_pressedReturn);
        _pressedReturn = false;
    }

    protected final IOSTextFieldHandler _handler;
    protected final UITextField _field;
    protected final Value<String> _text = Value.create("");
    protected final Signal<Boolean> _finishedEditing = Signal.create();

    protected IRectangle _requestedBounds;
    protected boolean _pressedReturn;
    protected int _maxInputLength;
}
