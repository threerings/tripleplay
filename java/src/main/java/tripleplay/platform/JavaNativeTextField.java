//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import pythagoras.f.FloatMath;
import pythagoras.f.IRectangle;

import playn.core.Font;
import playn.core.Keyboard;

import react.Connection;
import react.Signal;
import react.Slot;
import react.Value;

public class JavaNativeTextField
    implements NativeTextField
{
    public JavaNativeTextField (JLayeredPane root) {
        _root = root;

        setupField();
    }

    protected void setupField () {
        if (_textConnection != null) _textConnection.disconnect();

        JTextComponent oldField = _field;

        // Use an appropriate field type based on our security mode.
        _field = _mode == Mode.MULTI_LINE ? new JTextArea() :
            _mode == Mode.SECURE ? new JPasswordField() : new JTextField();

        // Restore any of our settings that we can directly from the old field.
        if (oldField != null) {
            _field.setBounds(oldField.getBounds());
            _field.setFont(oldField.getFont());
            _field.setText(oldField.getText());

            if (oldField.getParent() != null) {
                boolean focused = oldField.isFocusOwner();

                _root.remove(oldField);
                _root.add(_field, JLayeredPane.POPUP_LAYER);
                _field.setCaretPosition(oldField.getCaretPosition());
                if (focused) _field.requestFocus();
            }
        }

        _field.getDocument().addDocumentListener(new DocumentListener() {
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
                if (!_textNotifyInProgress) _text.update(_field.getText());
            }
        });
        if (_field instanceof JTextField) {
            ((JTextField)_field).addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent event) {
                    _finishedEditing.emit(true);
                }
            });
        }
        _field.setBorder(BorderFactory.createEmptyBorder());
        _field.setBackground(new Color(0xffffff, false)); // TODO(bruno): Transparency

        _textConnection = _text.connectNotify(new Slot<String>() {
            @Override public void onEmit (String value) {
                if (!_field.getText().equals(value)) {
                    _textNotifyInProgress = true;
                    _field.setText(value);
                    _textNotifyInProgress = false;
                }
            }});
    }

    @Override public Value<String> text () {
        return _text;
    }

    @Override public Signal<Boolean> finishedEditing () {
        return _finishedEditing;
    }

    @Override public JavaNativeTextField setValidator (Validator validator) {
        // TODO
        return this;
    }

    @Override public JavaNativeTextField setTransformer (Transformer transformer) {
        // TODO
        return this;
    }

    @Override public JavaNativeTextField setTextType (Keyboard.TextType type) {
        // TODO
        return this;
    }

    @Override public JavaNativeTextField setFont (Font font) {
        _field.setFont(new java.awt.Font(font.name(), awtFontStyle(font.style()),
            FloatMath.round(font.size())));
        return this;
    }

    @Override public JavaNativeTextField setBounds (IRectangle bounds) {
        _field.setBounds((int)bounds.x(), (int)bounds.y(),
            (int)bounds.width(), (int)bounds.height());
        return this;
    }

    @Override public JavaNativeTextField setAutocapitalization (boolean enable) {
        // nada - only for virtual keyboards
        return this;
    }

    @Override public JavaNativeTextField setAutocorrection (boolean enable) {
        // nada - only for virtual keyboards
        return this;
    }

    @Override
    public JavaNativeTextField setEnabled (boolean enabled) {
        _field.setEnabled(enabled);
        return this;
    }

    @Override public JavaNativeTextField refreshMode (Mode mode) {
        if (_mode != mode) {
            _mode = mode;
            setupField();
        }
        return this;
    }

    @Override public NativeTextField setReturnKeyLabel (String label) {
        // nada - only for virtual keyboards
        return this;
    }

    @Override public void add () {
        if (_field.getParent() == null) _root.add(_field);
    }

    @Override public void remove () {
        if (_field.getParent() != null) _root.remove(_field);
    }

    @Override public void focus () {
        _field.requestFocus();
    }

    @Override public boolean hasFocus () {
        return _field.hasFocus();
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

    protected JLayeredPane _root;
    protected JTextComponent _field;

    protected Value<String> _text = Value.create("");
    protected Signal<Boolean> _finishedEditing = Signal.create();

    protected Mode _mode;

    protected Connection _textConnection;

    protected boolean _textNotifyInProgress;
}
