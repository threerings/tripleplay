//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import pythagoras.f.FloatMath;

import playn.core.Font;
import playn.core.Key;
import playn.core.Keyboard;
import playn.core.PlayN;
import playn.java.JavaKeyboard;

import react.Connection;
import react.Slot;
import tripleplay.ui.Field;
import tripleplay.ui.Style;

import static tripleplay.platform.JavaTPPlatform.*;

public class JavaNativeTextField extends JavaNativeOverlay
    implements NativeTextField
{
    public JavaNativeTextField (Field.Native element, Mode mode, JavaNativeTextField oldField) {
        super(mode == Mode.MULTI_LINE ? new JTextArea() :
            mode == Mode.SECURE ? new JPasswordField() : new JTextField());
        _element = element;
        _mode = mode;
        _textComp = (JTextComponent)component;

        if (oldField != null) {
            // reach in and disconnect the old field
            oldField._textConnection.disconnect();
            // copy bounds of old field
            component.setBounds(oldField.component.getBounds());
        }

        _textComp.setText(element.field().text.get());
        _textComp.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate (DocumentEvent event) {
                update();
            }
            public void insertUpdate (DocumentEvent event) {
                update();
            }
            public void removeUpdate (DocumentEvent event) {
                update();
            }
            protected void update () {
                if (!_textNotifyInProgress)
                    updateOnMainThread(_element.field().text, _textComp.getText());
            }
        });
        if (isField()) {
            asField().addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent event) {
                    emitOnMainThread(_element.finishedEditing(), true);
                }
            });
        }
        _textComp.addFocusListener(new FocusListener() {
            @Override public void focusGained (FocusEvent e) {
                updateOnMainThread(instance()._focus, _element.field());
            }
            @Override public void focusLost (FocusEvent e) {
                JavaTPPlatform.emitOnMainThread(_element.finishedEditing(), false);
                Component opposite = e.getOppositeComponent();
                if (opposite == null || !hasOverlayFor(opposite))
                    updateOnMainThread(instance()._focus, null);
            }
        });
        _textComp.addKeyListener(new KeyListener() {
            void post (Key key, boolean pressed, char typed) {
                // no need to hop threads here, the JavaKeyboard does that
                ((JavaKeyboard)PlayN.keyboard()).post(key, pressed, typed);
            }
            @Override public void keyTyped (KeyEvent e) {
                post(null, false, e.getKeyChar());
            }

            @Override public void keyReleased (KeyEvent e) {
                post(translateKeyCode(e.getKeyCode()), false, '\u0000');
            }

            @Override public void keyPressed (KeyEvent e) {
                post(translateKeyCode(e.getKeyCode()), true, '\u0000');
            }
        });

        // this should cause the field to have no background, but isn't working
        // TODO: figure out how to *really* not draw a white box underneath the field
        _textComp.setOpaque(false);

        _textComp.setBorder(null);
        _textComp.setAutoscrolls(true);

        _textConnection = _element.field().text.connectNotify(new Slot<String>() {
            @Override public void onEmit (String value) {
                if (!_textComp.getText().equals(value)) {
                    _textNotifyInProgress = true;
                    _textComp.setText(value);
                    _textNotifyInProgress = false;
                }
            }});
    }

    @Override public void validateStyles () {
        Font font = _element.resolveStyle(Style.FONT);
        _textComp.setFont(new java.awt.Font(font.name(), awtFontStyle(font.style()),
            FloatMath.round(font.size())));
        Color col = new Color(_element.resolveStyle(Style.COLOR));
        _textComp.setForeground(col);
        _textComp.setCaretColor(col);

        if (isField()) {
            switch (_element.resolveStyle(Style.HALIGN)) {
            case CENTER:
                asField().setHorizontalAlignment(JTextField.CENTER);
                break;
            case LEFT:
                asField().setHorizontalAlignment(JTextField.LEFT);
                break;
            case RIGHT:
                asField().setHorizontalAlignment(JTextField.RIGHT);
                break;
            }
        }

        // TODO: Keyboard.TextType textType = resolveStyle(Field.TEXT_TYPE);
    }

    @Override
    public void setEnabled (boolean enabled) {
        _textComp.setEnabled(enabled);
    }

    public JavaNativeTextField refreshMode (Mode mode) {
        return _mode == mode ? this : new JavaNativeTextField(_element, mode, this);
    }

    @Override public void focus () {
        _textComp.requestFocus();
    }

    @Override public boolean hasFocus () {
        return _textComp.hasFocus();
    }

    @Override public boolean insert (String text) {
        _textComp.replaceSelection(text);
        return true;
    }

    protected boolean isField () {
        return _textComp instanceof JTextField;
    }

    protected JTextField asField () {
        return (JTextField)_textComp;
    }

    protected static int awtFontStyle (Font.Style style) {
        switch (style) {
        case BOLD: return java.awt.Font.BOLD;
        case BOLD_ITALIC: return java.awt.Font.BOLD | java.awt.Font.ITALIC;
        case ITALIC: return java.awt.Font.ITALIC;
        case PLAIN: return java.awt.Font.PLAIN;
        }

        // Unknown style, use plain.
        return java.awt.Font.PLAIN;
    }

    /** Translates an AWT key code into a playn key, if possible. May return {@link Key#UNKNOWN} if
     * there is no translation. */
    protected static Key translateKeyCode (int code) {
        switch (code) {
            case KeyEvent.VK_ENTER: return Key.ENTER;
            case KeyEvent.VK_BACK_SPACE: return Key.BACKSPACE;
            case KeyEvent.VK_TAB: return Key.TAB;
            case KeyEvent.VK_CANCEL: return Key.UNKNOWN;
            case KeyEvent.VK_CLEAR: return Key.UNKNOWN;
            case KeyEvent.VK_SHIFT: return Key.SHIFT;
            case KeyEvent.VK_CONTROL: return Key.CONTROL;
            case KeyEvent.VK_ALT: return Key.ALT;
            case KeyEvent.VK_PAUSE: return Key.PAUSE;
            case KeyEvent.VK_CAPS_LOCK: return Key.CAPS_LOCK;
            case KeyEvent.VK_ESCAPE: return Key.ESCAPE;
            case KeyEvent.VK_SPACE: return Key.SPACE;
            case KeyEvent.VK_PAGE_UP: return Key.PAGE_UP;
            case KeyEvent.VK_PAGE_DOWN: return Key.PAGE_DOWN;
            case KeyEvent.VK_END: return Key.END;
            case KeyEvent.VK_HOME: return Key.HOME;
            case KeyEvent.VK_LEFT: return Key.LEFT;
            case KeyEvent.VK_UP: return Key.UP;
            case KeyEvent.VK_RIGHT: return Key.RIGHT;
            case KeyEvent.VK_DOWN: return Key.DOWN;
            case KeyEvent.VK_COMMA: return Key.COMMA;
            case KeyEvent.VK_MINUS: return Key.MINUS;
            case KeyEvent.VK_PERIOD: return Key.PERIOD;
            case KeyEvent.VK_SLASH: return Key.SLASH;
            case KeyEvent.VK_0: return Key.K0;
            case KeyEvent.VK_1: return Key.K1;
            case KeyEvent.VK_2: return Key.K2;
            case KeyEvent.VK_3: return Key.K3;
            case KeyEvent.VK_4: return Key.K4;
            case KeyEvent.VK_5: return Key.K5;
            case KeyEvent.VK_6: return Key.K6;
            case KeyEvent.VK_7: return Key.K7;
            case KeyEvent.VK_8: return Key.K8;
            case KeyEvent.VK_9: return Key.K9;
            case KeyEvent.VK_SEMICOLON: return Key.SEMICOLON;
            case KeyEvent.VK_EQUALS: return Key.EQUALS;
            case KeyEvent.VK_A: return Key.A;
            case KeyEvent.VK_B: return Key.B;
            case KeyEvent.VK_C: return Key.C;
            case KeyEvent.VK_D: return Key.D;
            case KeyEvent.VK_E: return Key.E;
            case KeyEvent.VK_F: return Key.F;
            case KeyEvent.VK_G: return Key.G;
            case KeyEvent.VK_H: return Key.H;
            case KeyEvent.VK_I: return Key.I;
            case KeyEvent.VK_J: return Key.J;
            case KeyEvent.VK_K: return Key.K;
            case KeyEvent.VK_L: return Key.L;
            case KeyEvent.VK_M: return Key.M;
            case KeyEvent.VK_N: return Key.N;
            case KeyEvent.VK_O: return Key.O;
            case KeyEvent.VK_P: return Key.P;
            case KeyEvent.VK_Q: return Key.Q;
            case KeyEvent.VK_R: return Key.R;
            case KeyEvent.VK_S: return Key.S;
            case KeyEvent.VK_T: return Key.T;
            case KeyEvent.VK_U: return Key.U;
            case KeyEvent.VK_V: return Key.V;
            case KeyEvent.VK_W: return Key.W;
            case KeyEvent.VK_X: return Key.X;
            case KeyEvent.VK_Y: return Key.Y;
            case KeyEvent.VK_Z: return Key.Z;
            case KeyEvent.VK_OPEN_BRACKET: return Key.LEFT_BRACKET;
            case KeyEvent.VK_BACK_SLASH: return Key.BACKSLASH;
            case KeyEvent.VK_CLOSE_BRACKET: return Key.RIGHT_BRACKET;
            case KeyEvent.VK_NUMPAD0: return Key.NP0;
            case KeyEvent.VK_NUMPAD1: return Key.NP1;
            case KeyEvent.VK_NUMPAD2: return Key.NP2;
            case KeyEvent.VK_NUMPAD3: return Key.NP3;
            case KeyEvent.VK_NUMPAD4: return Key.NP4;
            case KeyEvent.VK_NUMPAD5: return Key.NP5;
            case KeyEvent.VK_NUMPAD6: return Key.NP6;
            case KeyEvent.VK_NUMPAD7: return Key.NP7;
            case KeyEvent.VK_NUMPAD8: return Key.NP8;
            case KeyEvent.VK_NUMPAD9: return Key.NP9;
            case KeyEvent.VK_MULTIPLY: return Key.MULTIPLY;
            case KeyEvent.VK_ADD: return Key.NP_ADD;
            case KeyEvent.VK_SEPARATOR: return Key.UNKNOWN;
            case KeyEvent.VK_SUBTRACT: return Key.NP_SUBTRACT;
            case KeyEvent.VK_DECIMAL: return Key.NP_DECIMAL;
            case KeyEvent.VK_DIVIDE: return Key.NP_DIVIDE;
            case KeyEvent.VK_DELETE: return Key.DELETE;
            case KeyEvent.VK_NUM_LOCK: return Key.NP_NUM_LOCK;
            case KeyEvent.VK_SCROLL_LOCK: return Key.SCROLL_LOCK;
            case KeyEvent.VK_F1: return Key.F1;
            case KeyEvent.VK_F2: return Key.F2;
            case KeyEvent.VK_F3: return Key.F3;
            case KeyEvent.VK_F4: return Key.F4;
            case KeyEvent.VK_F5: return Key.F5;
            case KeyEvent.VK_F6: return Key.F6;
            case KeyEvent.VK_F7: return Key.F7;
            case KeyEvent.VK_F8: return Key.F8;
            case KeyEvent.VK_F9: return Key.F9;
            case KeyEvent.VK_F10: return Key.F10;
            case KeyEvent.VK_F11: return Key.F11;
            case KeyEvent.VK_F12: return Key.F12;
            /* omitted cases for F13-F24, these are not standard */
            case KeyEvent.VK_PRINTSCREEN: return Key.PRINT_SCREEN;
            case KeyEvent.VK_INSERT: return Key.INSERT;
            case KeyEvent.VK_HELP: return Key.UNKNOWN;
            case KeyEvent.VK_META: return Key.META;
            case KeyEvent.VK_BACK_QUOTE: return Key.BACKQUOTE;
            case KeyEvent.VK_QUOTE: return Key.QUOTE;
            case KeyEvent.VK_KP_UP: return Key.NP_UP;
            case KeyEvent.VK_KP_DOWN: return Key.NP_DOWN;
            case KeyEvent.VK_KP_LEFT: return Key.NP_LEFT;
            case KeyEvent.VK_KP_RIGHT: return Key.NP_RIGHT;
            case KeyEvent.VK_AMPERSAND: return Key.AMPERSAND;
            case KeyEvent.VK_ASTERISK: return Key.ASTERISK;
            case KeyEvent.VK_QUOTEDBL: return Key.DOUBLE_QUOTE;
            case KeyEvent.VK_LESS: return Key.LESS;
            case KeyEvent.VK_GREATER: return Key.GREATER;
            case KeyEvent.VK_BRACELEFT: return Key.LEFT_BRACE;
            case KeyEvent.VK_BRACERIGHT: return Key.RIGHT_BRACE;
            case KeyEvent.VK_AT: return Key.AT;
            case KeyEvent.VK_COLON: return Key.COLON;
            case KeyEvent.VK_CIRCUMFLEX: return Key.CIRCUMFLEX;
            case KeyEvent.VK_DOLLAR: return Key.DOLLAR;
            case KeyEvent.VK_LEFT_PARENTHESIS: return Key.LEFT_PAREN;
            case KeyEvent.VK_EXCLAMATION_MARK: return Key.BANG;
            case KeyEvent.VK_NUMBER_SIGN: return Key.HASH;
            case KeyEvent.VK_PLUS: return Key.PLUS;
            case KeyEvent.VK_RIGHT_PARENTHESIS: return Key.RIGHT_PAREN;
            case KeyEvent.VK_UNDERSCORE: return Key.UNDERSCORE;
            case KeyEvent.VK_WINDOWS: return Key.WINDOWS;
            // case KeyEvent.VK_CONTEXT_MENU: return Key.CONTEXT_MENU;
            // TODO: "Edit menu" keys in playn?
            // case KeyEvent.VK_CUT: return Key.CUT;
            // case KeyEvent.VK_COPY: return Key.COPY;
            // case KeyEvent.VK_PASTE: return Key.PASTE;
            // case KeyEvent.VK_UNDO: return Key.UNDO;
            // case KeyEvent.VK_AGAIN: return Key.AGAIN;
            // case KeyEvent.VK_FIND: return Key.FIND;
            // case KeyEvent.VK_PROPS: return Key.PROPS;
            // case KeyEvent.VK_STOP: return Key.STOP;
            // case KeyEvent.VK_COMPOSE: return Key.COMPOSE;
            // case KeyEvent.VK_ALT_GRAPH: return Key.ALT_GRAPH;
            // case KeyEvent.VK_BEGIN: return Key.BEGIN;
            // TODO: European keyboard support in playn?
            // case KeyEvent.VK_DEAD_GRAVE: return Key.DEAD_GRAVE;
            // case KeyEvent.VK_DEAD_ACUTE: return Key.DEAD_ACUTE;
            // case KeyEvent.VK_DEAD_CIRCUMFLEX: return Key.DEAD_CIRCUMFLEX;
            // case KeyEvent.VK_DEAD_TILDE: return Key.DEAD_TILDE;
            // case KeyEvent.VK_DEAD_MACRON: return Key.DEAD_MACRON;
            // case KeyEvent.VK_DEAD_BREVE: return Key.DEAD_BREVE;
            // case KeyEvent.VK_DEAD_ABOVEDOT: return Key.DEAD_ABOVEDOT;
            // case KeyEvent.VK_DEAD_DIAERESIS: return Key.DEAD_DIAERESIS;
            // case KeyEvent.VK_DEAD_ABOVERING: return Key.DEAD_ABOVERING;
            // case KeyEvent.VK_DEAD_DOUBLEACUTE: return Key.DEAD_DOUBLEACUTE;
            // case KeyEvent.VK_DEAD_CARON: return Key.DEAD_CARON;
            // case KeyEvent.VK_DEAD_CEDILLA: return Key.DEAD_CEDILLA;
            // case KeyEvent.VK_DEAD_OGONEK: return Key.DEAD_OGONEK;
            // case KeyEvent.VK_DEAD_IOTA: return Key.DEAD_IOTA;
            // case KeyEvent.VK_DEAD_VOICED_SOUND: return Key.DEAD_VOICED_SOUND;
            // case KeyEvent.VK_DEAD_SEMIVOICED_SOUND: return Key.DEAD_SEMIVOICED_SOUND;
            // case KeyEvent.VK_EURO_SIGN: return Key.EURO_SIGN;
            // case KeyEvent.VK_INVERTED_EXCLAMATION_MARK: return Key.INVERTED_EXCLAMATION_MARK;
            // TODO: support asian keyboards in playn?
            // case KeyEvent.VK_FINAL: return Key.FINAL;
            // case KeyEvent.VK_CONVERT: return Key.CONVERT;
            // case KeyEvent.VK_NONCONVERT: return Key.NONCONVERT;
            // case KeyEvent.VK_ACCEPT: return Key.ACCEPT;
            // case KeyEvent.VK_MODECHANGE: return Key.MODECHANGE;
            // case KeyEvent.VK_KANA: return Key.KANA;
            // case KeyEvent.VK_KANJI: return Key.KANJI;
            // case KeyEvent.VK_ALPHANUMERIC: return Key.ALPHANUMERIC;
            // case KeyEvent.VK_KATAKANA: return Key.KATAKANA;
            // case KeyEvent.VK_HIRAGANA: return Key.HIRAGANA;
            // case KeyEvent.VK_FULL_WIDTH: return Key.FULL_WIDTH;
            // case KeyEvent.VK_HALF_WIDTH: return Key.HALF_WIDTH;
            // case KeyEvent.VK_ROMAN_CHARACTERS: return Key.ROMAN_CHARACTERS;
            // case KeyEvent.VK_ALL_CANDIDATES: return Key.ALL_CANDIDATES;
            // case KeyEvent.VK_PREVIOUS_CANDIDATE: return Key.PREVIOUS_CANDIDATE;
            // case KeyEvent.VK_CODE_INPUT: return Key.CODE_INPUT;
            // case KeyEvent.VK_JAPANESE_KATAKANA: return Key.JAPANESE_KATAKANA;
            // case KeyEvent.VK_JAPANESE_HIRAGANA: return Key.JAPANESE_HIRAGANA;
            // case KeyEvent.VK_JAPANESE_ROMAN: return Key.JAPANESE_ROMAN;
            // case KeyEvent.VK_KANA_LOCK: return Key.KANA_LOCK;
            // case KeyEvent.VK_INPUT_METHOD_ON_OFF: return Key.INPUT_METHOD_ON_OFF;
            default: return Key.UNKNOWN;
        }
    }

    protected final Field.Native _element;
    protected final Mode _mode;
    protected final JTextComponent _textComp;

    protected Connection _textConnection;
    protected volatile boolean _textNotifyInProgress;
}
