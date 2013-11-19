//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import playn.core.Font;
import playn.core.PlayN;
import playn.java.SWTKeyboard;

import react.Connection;
import react.Slot;
import tripleplay.ui.Field;
import tripleplay.ui.Style;

public class SWTNativeTextField extends SWTNativeOverlay
    implements NativeTextField
{
    public SWTNativeTextField (Field.Native element) {
        _element = element;
        _textConnection = _element.field().text.connectNotify(new Slot<String>() {
            @Override public void onEmit (final String value) {
                if (_textCtrl == null) return;
                if (!_textCtrl.getText().equals(value)) {
                    _textNotifyInProgress = true;
                    _textCtrl.setText(value);
                    _textNotifyInProgress = false;
                }
            }});
    }

    @Override protected Control createControl (Composite parent) {
        int style = (_element.resolveStyle(Field.MULTILINE) ? SWT.MULTI : SWT.SINGLE) |
            (_element.resolveStyle(Field.SECURE_TEXT_ENTRY) ? SWT.PASSWORD : 0);
        switch (_element.resolveStyle(Style.HALIGN)) {
        case CENTER: style |= SWT.CENTER; break;
        case LEFT: style |= SWT.LEFT; break;
        case RIGHT: style |= SWT.RIGHT; break;
        }
        return new Text(parent, style);
    }

    @Override protected void didCreate () {
        _textCtrl = (Text)ctrl;
        _textCtrl.setText(_element.field().text.get());
        _textCtrl.addModifyListener(new ModifyListener() {
            @Override public void modifyText (ModifyEvent e) {
                if (!_textNotifyInProgress) _element.field().text.update(_textCtrl.getText());
            }
        });

        // TODO: add action listener for return key
        /* if (isField()) {
            asField().addActionListener(new ActionListener() {
                public void actionPerformed (ActionEvent event) {
                    emitOnMainThread(_element.finishedEditing(), true);
                }
            });
        }*/
        _textCtrl.addFocusListener(new FocusListener() {
            @Override public void focusLost (FocusEvent e) {
                _element.finishedEditing().emit(false);
                // TODO: is focus update required here like in swing?
                /* Component opposite = e.getOppositeComponent();
                if (opposite == null || !hasOverlayFor(opposite))
                    updateOnMainThread(instance()._focus, null); */
            }

            @Override public void focusGained (FocusEvent e) {
                SWTTPPlatform.instance()._focus.update(_element.field());
            }
        });
        _textCtrl.addKeyListener(new KeyListener() {
            void post (int code, boolean pressed) {
                SWTKeyboard keyboard = (SWTKeyboard)PlayN.keyboard();
                // TODO: is post necessary here? could be just dispatch
                // NOTE: no support for typed char; shouldn't be a problem for most uses
                keyboard.post(keyboard.translateKey(code), pressed, '\u0000');
            }

            @Override public void keyReleased (KeyEvent e) {
                post(e.keyCode, false);
            }

            @Override public void keyPressed (KeyEvent e) {
                post(e.keyCode, true);
            }
        });

        // TODO _textComp.setBorder(null);
        // TODO _textComp.setAutoscrolls(true);
        _textCtrl.setEnabled(_enabled);

        Font font = _element.resolveStyle(Style.FONT);
        _textCtrl.setFont(SWTTPPlatform.instance().convert().font(font));
        _textCtrl.setForeground(convert().color(_element.resolveStyle(Style.COLOR)));

        // TODO: Keyboard.TextType textType = resolveStyle(Field.TEXT_TYPE);
    }

    @Override
    protected void willDispose () {
        _textConnection.disconnect();
        _textConnection = null;
        // NOTE: all of our added SWT listeners are released in dispose()
    }

    @Override
    public void setEnabled (boolean enabled) {
        _enabled = enabled;
        if (_textCtrl != null) _textCtrl.setEnabled(enabled);
    }

    public void refresh () {
        boolean add = ctrl != null;
        remove();
        if (add) add();
    }

    @Override public void focus () {
        if (_textCtrl != null) _textCtrl.setFocus();
    }

    @Override public boolean insert (String text) {
        if (_textCtrl == null) return false;
        _textCtrl.insert(text);
        return true;
    }

    SWTConvert convert () {
        return SWTTPPlatform.instance().convert();
    }

    protected final Field.Native _element;
    protected Text _textCtrl;
    protected boolean _enabled = true;

    protected Connection _textConnection;
    protected volatile boolean _textNotifyInProgress;
}
