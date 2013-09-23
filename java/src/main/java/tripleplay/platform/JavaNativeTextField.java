//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
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

        if (oldField != null) {
            // reach in and disconnect the old field
            oldField._textConnection.disconnect();
            // copy bounds of old field
            component.setBounds(oldField.component.getBounds());
        }

        field().setText(element.field().text.get());
        field().getDocument().addDocumentListener(new DocumentListener() {
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
                if (!_textNotifyInProgress) _element.field().text.update(field().getText());
            }
        });
        if (component instanceof JTextField) {
            ((JTextField)component).addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent event) {
                    _element.finishedEditing().emit(true);
                }
            });
        }
        component.setBorder(BorderFactory.createEmptyBorder());
        component.setBackground(new Color(0xffffff, false)); // TODO(bruno): Transparency

        _textConnection = _element.field().text.connectNotify(new Slot<String>() {
            @Override public void onEmit (String value) {
                if (!field().getText().equals(value)) {
                    _textNotifyInProgress = true;
                    field().setText(value);
                    _textNotifyInProgress = false;
                }
            }});
    }

    @Override public void validateStyles () {
        Font font = _element.resolveStyle(Style.FONT);
        field().setFont(new java.awt.Font(font.name(), awtFontStyle(font.style()),
            FloatMath.round(font.size())));

        // TODO: Keyboard.TextType textType = resolveStyle(Field.TEXT_TYPE);
        // TODO: Style.HAlign halign = resolveStyle(Style.HALIGN);
        // TODO: int color = resolveStyle(Style.COLOR);
    }

    @Override
    public void setEnabled (boolean enabled) {
        component.setEnabled(enabled);
    }

    public JavaNativeTextField refreshMode (Mode mode) {
        return _mode == mode ? this : new JavaNativeTextField(_element, mode, this);
    }

    @Override public void focus () {
        component.requestFocus();
    }

    @Override public boolean hasFocus () {
        return component.hasFocus();
    }

    public JTextComponent field () {
        return (JTextComponent)component;
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

    protected Connection _textConnection;
    protected boolean _textNotifyInProgress;
}
