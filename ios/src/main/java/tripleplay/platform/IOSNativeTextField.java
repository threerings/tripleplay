//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import cli.MonoTouch.UIKit.UIKeyboardType;
import cli.MonoTouch.UIKit.UITextAutocapitalizationType;
import cli.MonoTouch.UIKit.UITextAutocorrectionType;
import cli.MonoTouch.UIKit.UITextField;
import cli.MonoTouch.UIKit.UITextFieldDelegate;
import cli.System.Drawing.RectangleF;

import pythagoras.f.IRectangle;

import playn.core.Font;
import playn.core.Keyboard;
import playn.ios.IOSPlatform;

import react.Slot;
import react.Value;

import static tripleplay.platform.Log.log;

public class IOSNativeTextField implements NativeTextField
{
    public IOSNativeTextField (IOSTextFieldHandler handler) {
        _handler = handler;
        _field = new UITextField();

        // TODO: probably want to make these customizable. Certainly in the case of SecureTextEntry
        _field.set_AutocorrectionType(
            UITextAutocorrectionType.wrap(UITextAutocorrectionType.Yes));
        _field.set_AutocapitalizationType(
            UITextAutocapitalizationType.wrap(UITextAutocapitalizationType.Sentences));
        _field.set_SecureTextEntry(false);

        _field.set_Delegate(CLOSE_ON_RETURN);

        _text = Value.create("");
        _text.connect(new Slot<String>() {
            @Override public void onEmit (String value) {
                _field.set_Text(value);
            }
        });
    }

    @Override public Value<String> text () {
        return _text;
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
        return this;
    }

    @Override public IOSNativeTextField setBounds (IRectangle bounds) {
        _field.set_Frame(new RectangleF(bounds.x(), bounds.y(), bounds.width(), bounds.height()));
        return this;
    }

    @Override public void add () {
        if (!_handler.isAdded(_field)) _handler.activate(this);
    }

    @Override public void remove () {
        if (_handler.isAdded(_field)) _handler.deactivate(this);
    }

    protected final IOSTextFieldHandler _handler;
    protected final UITextField _field;
    protected final Value<String> _text;

    // all fields close the keyboard when the return key is used
    protected static final UITextFieldDelegate CLOSE_ON_RETURN = new UITextFieldDelegate() {
        @Override public boolean ShouldReturn (UITextField field) {
            field.ResignFirstResponder();
            return false;
        }
    };
}
