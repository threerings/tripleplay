//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import cli.MonoTouch.Foundation.NSRange;
import cli.MonoTouch.Foundation.NSString;
import cli.MonoTouch.UIKit.IUITextInputTraits;
import cli.MonoTouch.UIKit.UIColor;
import cli.MonoTouch.UIKit.UIControlContentVerticalAlignment;
import cli.MonoTouch.UIKit.UIFont;
import cli.MonoTouch.UIKit.UIKeyboardType;
import cli.MonoTouch.UIKit.UIReturnKeyType;
import cli.MonoTouch.UIKit.UITextAlignment;
import cli.MonoTouch.UIKit.UITextAutocapitalizationType;
import cli.MonoTouch.UIKit.UITextAutocorrectionType;
import cli.MonoTouch.UIKit.UITextField;
import cli.MonoTouch.UIKit.UITextFieldDelegate;
import cli.MonoTouch.UIKit.UITextRange;
import cli.MonoTouch.UIKit.UITextView;
import cli.MonoTouch.UIKit.UIView;
import cli.System.Drawing.RectangleF;

import playn.core.Events;
import pythagoras.f.IRectangle;

import playn.core.Color;
import playn.core.Font;
import playn.core.Keyboard;
import react.Connection;
import react.Slot;
import tripleplay.ui.Field;
import tripleplay.ui.Style;

public abstract class IOSNativeTextField extends IOSNativeOverlay
    implements NativeTextField
{
    public static class SingleLine extends IOSNativeTextField
    {
        public SingleLine (IOSTextFieldHandler handler, IOSNativeTextField prior,
                Field.Native field) {
            super(handler, new UITextField(), prior, field);
            _field = (UITextField)view;
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
                    if (_handler._keyboardListener != null && s.length() < 2) {
                        double time = System.currentTimeMillis();
                        // 8 is java.awt.event.KeyEvent.VK_BACK_SPACE, use it if it looks like
                        // the incoming change is a delete.
                        char keyChar = s.length() == 0 ? (char)8 : s.charAt(0);
                        _handler._keyboardListener.onKeyTyped(new Keyboard.TypedEvent.Impl(
                            new Events.Flags.Impl(), time, keyChar));
                    }

                    String newString = new NSString(uiTextField.get_Text())
                        .Replace(nsRange, new NSString(s)).ToString();
                    return _element.isValid(newString);
                }
            });
        }

        @Override public void setEnabled (boolean enabled) {
            _field.set_Enabled(enabled);
        }

        public NativeTextField refreshMode (Mode mode) {
            if (mode == Mode.MULTI_LINE) return new MultiLine(_handler, this, _element);
            _field.set_SecureTextEntry(mode == Mode.SECURE);
            return this;
        }

        @Override public boolean insert (String text) {
            UITextRange range = _field.get_SelectedTextRange();
            if (range == null) {
                return false;
            }
            _field.ReplaceText(range, text);
            return true;
        }

        @Override protected UIFont getNativeFont () {
            return _field.get_Font();
        }

        @Override protected void setNativeFont (UIFont font) {
            _field.set_Font(font);
        }

        @Override protected String getNativeText () {
            return _field.get_Text();
        }

        @Override protected void setNativeText (String text) {
            _field.set_Text(text);
        }

        @Override protected IUITextInputTraits getTraits () {
            return _field;
        }

        @Override protected void didFinish () {
            _element.finishedEditing().emit(_pressedReturn);
            _pressedReturn = false;
        }

        @Override protected void setAlignment (UITextAlignment halign) {
            _field.set_TextAlignment(halign);
        }

        @Override protected void setColor(UIColor color) {
            _field.set_TextColor(color);
        }

        protected final UITextField _field;
        protected boolean _pressedReturn;
    }

    public static class MultiLine extends IOSNativeTextField
    {
        public MultiLine (IOSTextFieldHandler handler, IOSNativeTextField prior,
                Field.Native field) {
            super(handler, new UITextView(), prior, field);
            _field = (UITextView)view;
            _field.set_Editable(true);
            // TODO: do we need to call set_Delegate?
        }

        @Override public void setEnabled (boolean enabled) {
            _field.set_Editable(enabled);
        }

        public NativeTextField refreshMode (Mode mode) {
            return mode == Mode.MULTI_LINE ? this :
                new SingleLine(_handler, this, _element).refreshMode(mode);
        }

        @Override protected UIFont getNativeFont () {
            return _field.get_Font();
        }

        @Override protected void setNativeFont (UIFont font) {
            _field.set_Font(font);
        }

        @Override protected String getNativeText () {
            return _field.get_Text();
        }

        @Override protected void setNativeText (String text) {
            _field.set_Text(text);
        }

        @Override protected IUITextInputTraits getTraits () {
            return _field;
        }

        @Override protected void didFinish () {
            _element.finishedEditing().emit(false);
        }

        @Override protected void setAlignment (UITextAlignment halign) {
            _field.set_TextAlignment(halign);
        }

        @Override protected void setColor(UIColor color) {
            _field.set_TextColor(color);
        }

        @Override public boolean insert (String text) {
            UITextRange range = _field.get_SelectedTextRange();
            if (range == null) {
                return false;
            }
            _field.ReplaceText(range, text);
            return true;
        }

        protected final UITextView _field;
    }

    public IOSNativeTextField (IOSTextFieldHandler handler, UIView view, IOSNativeTextField prior,
            Field.Native field) {
        super(view);
        _element = field;
        _handler = handler;

        if (prior != null) {
            prior._textConn.disconnect();
        }

        _textConn = _element.field().text.connect(new Slot<String>() {
            @Override public void onEmit (String value) {
                String current = getNativeText();
                if (current == null || !current.equals(value)) {
                    setNativeText(value);
                }
            }
        });
    }

    public UIView getView () {
        return view;
    }

    @Override public void validateStyles () {
        // Keyboard type
        Keyboard.TextType type = _element.resolveStyle(Field.TEXT_TYPE);
        switch (type) {
        case NUMBER:
            getTraits().set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.NumberPad));
            break;
        case EMAIL:
            getTraits().set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.EmailAddress));
            break;
        case URL:
            getTraits().set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.Url));
            break;
        case DEFAULT:
            getTraits().set_KeyboardType(UIKeyboardType.wrap(UIKeyboardType.Default));
            break;
        }

        // Font
        Font font = _element.resolveStyle(Style.FONT);
        setNativeFont(_handler.getUIFont(font));

        // Automatic capitalization
        boolean enable = _element.resolveStyle(Field.AUTOCAPITALIZATION);
        getTraits().set_AutocapitalizationType(UITextAutocapitalizationType.wrap(
            enable ? UITextAutocapitalizationType.Sentences : UITextAutocapitalizationType.None));

        // Automatic correction
        enable = _element.resolveStyle(Field.AUTOCORRECTION);
        getTraits().set_AutocorrectionType(UITextAutocorrectionType.wrap(
            enable ? UITextAutocorrectionType.Yes : UITextAutocorrectionType.No));

        // Return key label
        String label = _element.resolveStyle(Field.RETURN_KEY_LABEL);
        setReturnKeyLabel(label);

        Style.HAlign halign = _element.resolveStyle(Style.HALIGN);
        setAlignment(UITextAlignment.wrap(toMono(halign)));

        // Color
        int color = _element.resolveStyle(Style.COLOR);
        setColor(UIColor.FromRGB(Color.red(color), Color.green(color), Color.blue(color)));
    }

    @Override protected void didAdd () {
        _handler.activate(this);
        setNativeText(_element.field().text.get());
    }

    @Override protected void didRemove () {
        _handler.deactivate(this);
    }

    @Override public void focus () {
        view.BecomeFirstResponder();
    }

    @Override public boolean hasFocus () {
        return view.get_IsFirstResponder();
    }

    abstract public NativeTextField refreshMode (Mode mode);

    abstract protected UIFont getNativeFont ();
    abstract protected void setNativeFont (UIFont font);
    abstract protected String getNativeText ();
    abstract protected void setNativeText (String text);
    abstract protected IUITextInputTraits getTraits ();
    abstract protected void setAlignment (UITextAlignment halign);
    abstract protected void setColor (UIColor color);
    abstract protected void didFinish ();

    protected void didStart () {
        TPPlatform.instance()._focus.update(_element.field());
    }

    protected void setReturnKeyLabel (String label) {
        if (label == null || label.isEmpty()) {
            getTraits().set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Default));
            return;
        }
        label = label.toLowerCase();
        if (label.equals(getTraits().get_ReturnKeyType().ToString().toLowerCase())) {
            // NOOP
            return;
        }

        if (label.equals("go")) {
            getTraits().set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Go));
        } else if (label.equals("google")) {
            getTraits().set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Google));
        } else if (label.equals("join")) {
            getTraits().set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Join));
        } else if (label.equals("next")) {
            getTraits().set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Next));
        } else if (label.equals("route")) {
            getTraits().set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Route));
        } else if (label.equals("search")) {
            getTraits().set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Search));
        } else if (label.equals("send")) {
            getTraits().set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Send));
        } else if (label.equals("yahoo")) {
            getTraits().set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Yahoo));
        } else if (label.equals("done")) {
            getTraits().set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.Done));
        } else if (label.equals("emergencycall")) {
            getTraits().set_ReturnKeyType(UIReturnKeyType.wrap(UIReturnKeyType.EmergencyCall));
        }
    }

    protected void handleNewValue () {
        String value = getNativeText();
        String transformed = _element.transform(value);
        if (!transformed.equals(value)) {
            // update the field ourselves in case transformed is the same value
            // currently held in field.text(), and therefore the update below
            // will NOOP.
            setNativeText(transformed);
            value = transformed;
        }
        _element.field().text.update(value);
    }

    @Override protected void adjustBounds (RectangleF fieldBounds) {
        // field fudged to the left 1 pixel to match PlayN text rendering.
        fieldBounds.set_X(fieldBounds.get_X() + 1);

        // ensure we're tall enough for a single line of text and the text cursor
        UIFont font = getNativeFont();
        if (fieldBounds.get_Height() < font.get_LineHeight()) {
            fieldBounds.set_Height(font.get_LineHeight());
        }
    }

    protected static int toMono (Style.HAlign halign) {
        switch (halign) {
        case LEFT: default: return UITextAlignment.Left;
        case CENTER: return UITextAlignment.Center;
        case RIGHT: return UITextAlignment.Right;
        }
    }

    protected final Field.Native _element;
    protected final IOSTextFieldHandler _handler;
    protected final Connection _textConn;

    protected IRectangle _requestedBounds;
}
