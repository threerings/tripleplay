//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import cli.MonoTouch.Foundation.NSRange;
import cli.MonoTouch.Foundation.NSString;
import cli.MonoTouch.UIKit.UIControlContentVerticalAlignment;
import cli.MonoTouch.UIKit.UIFont;
import cli.MonoTouch.UIKit.UIKeyboardType;
import cli.MonoTouch.UIKit.UIReturnKeyType;
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
        _field.set_VerticalAlignment(
            UIControlContentVerticalAlignment.wrap(UIControlContentVerticalAlignment.Center));

        // all fields close the keyboard when the return key is used
        _field.set_Delegate(new UITextFieldDelegate() {
            @Override public boolean ShouldReturn (UITextField field) {
                _pressedReturn = true;
                if (_handler._virtualKeyboardCtrl == null ||
                    _handler._virtualKeyboardCtrl.hideKeyboardOnEnter()) {
                    field.ResignFirstResponder();
                } else {
                    didFinish();
                }
                return false;
            }
            @Override public boolean ShouldChangeCharacters (UITextField uiTextField,
                NSRange nsRange, String s) {
                if (_validator == null) return true;
                String newString =  new NSString(uiTextField.get_Text())
                    .Replace(nsRange, new NSString(s)).ToString();
                return _validator.isValid(newString);
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

    @Override public NativeTextField setValidator (Validator validator) {
        _validator = validator;
        return this;
    }

    @Override public NativeTextField setTransformer (Transformer transformer) {
        _transformer = transformer;
        return this;
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

    @Override public NativeTextField setReturnKeyLabel (String label) {
        if (label == null || label.isEmpty()) {
            _field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Default));
            return this;
        }
        label = label.toLowerCase();
        if (label.equals(_field.get_ReturnKeyType().ToString().toLowerCase())) {
            // NOOP
            return this;
        }

        if (label.equals("go")) {
            _field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Go));
        } else if (label.equals("google")) {
            _field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Google));
        } else if (label.equals("join")) {
            _field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Join));
        } else if (label.equals("next")) {
            _field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Next));
        } else if (label.equals("route")) {
            _field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Route));
        } else if (label.equals("search")) {
            _field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Search));
        } else if (label.equals("send")) {
            _field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Send));
        } else if (label.equals("yahoo")) {
            _field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Yahoo));
        } else if (label.equals("done")) {
            _field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Done));
        } else if (label.equals("emergencycall")) {
            _field.set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.EmergencyCall));
        }
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

    @Override public boolean hasFocus () {
        return _field.get_IsFirstResponder();
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
    protected Validator _validator;
    protected Transformer _transformer;
}
