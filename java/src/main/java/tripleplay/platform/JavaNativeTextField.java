//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import pythagoras.f.FloatMath;

import playn.core.Font;

import react.Connection;
import react.Slot;
import tripleplay.ui.Field;
import tripleplay.ui.Style;

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
                if (!_textNotifyInProgress) _element.field().text.update(_textComp.getText());
            }
        });
        if (isField()) {
            asField().addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent event) {
                    _element.finishedEditing().emit(true);
                }
            });
        }
        _textComp.addFocusListener(new FocusListener() {
            @Override public void focusLost (FocusEvent e) {
                _element.finishedEditing().emit(false);
            }
            @Override public void focusGained (FocusEvent e) {
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

    protected final Field.Native _element;
    protected final Mode _mode;
    protected final JTextComponent _textComp;

    protected Connection _textConnection;
    protected boolean _textNotifyInProgress;
}
