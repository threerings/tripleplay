//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import pythagoras.f.IRectangle;

import playn.core.PlayN;
import playn.core.Font;
import playn.core.Keyboard;

import react.Signal;
import react.Slot;
import react.Value;

public class JavaNativeTextField
    implements NativeTextField
{
    public JavaNativeTextField (Container root) {
        _root = root;

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
                _text.update(_field.getText());
            }
        });
        _field.addActionListener(new ActionListener() {
            public void actionPerformed (ActionEvent event) {
                _finishedEditing.emit(true);
            }
        });
        _field.setBorder(BorderFactory.createEmptyBorder());
        _field.setBackground(new Color(0xffffff, false)); // TODO(bruno): Transparency

        _text.connect(new Slot<String>() { public void onEmit (String value) {
            if (!_field.getText().equals(value)) {
                _field.setText(value);
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
        // TODO
        return this;
    }

    @Override public JavaNativeTextField setBounds (IRectangle bounds) {
        _field.setBounds((int)bounds.x(), (int)bounds.y(),
            (int)bounds.width(), (int)bounds.height());
        return this;
    }

    @Override public JavaNativeTextField setAutocapitalization (boolean enable) {
        // TODO
        return this;
    }

    @Override public JavaNativeTextField setAutocorrection (boolean enable) {
        // TODO
        return this;
    }

    @Override public JavaNativeTextField setSecureTextEntry (boolean enable) {
        // TODO
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

    protected Container _root;
    protected JTextField _field = new JTextField();

    protected Value<String> _text = Value.create("");
    protected Signal<Boolean> _finishedEditing = Signal.create();
}
