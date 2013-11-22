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
                if (_textCtrl.getText().equals(value)) return;

                _textNotifyInProgress = true;
                try {
                    _textCtrl.setText(value);
                } finally {
                    _textNotifyInProgress = false;
                }
            }
        });
    }

    @Override protected Control createControl (Composite parent) {
        // TODO: transparent background. these style flags don't work
        return new Text(parent, resolveStyle() | SWT.NO_BACKGROUND | SWT.TRANSPARENT);
    }

    @Override protected void didCreate () {
        // get our casted member
        _textCtrl = (Text)ctrl;

        // copy in the tp field text
        _textCtrl.setText(_element.field().text.get());

        // listen for changes and propagate back to field
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

        // listen for focus changes and dispatch via the platform
        _textCtrl.addFocusListener(new FocusListener() {
            @Override public void focusLost (FocusEvent e) {
                _element.finishedEditing().emit(false);
                SWTTPPlatform.instance()._focus.update(null);
                // TODO: is focus update required here like in swing?
                /* Component opposite = e.getOppositeComponent();
                if (opposite == null || !hasOverlayFor(opposite))
                    updateOnMainThread(instance()._focus, null); */
            }

            @Override public void focusGained (FocusEvent e) {
                SWTTPPlatform.instance()._focus.update(_element.field());
            }
        });

        // listen for keypresses and dispatch via platform (for apps to handle tab/arrows etc)
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

        refresh();
    }

    @Override protected void willDispose () {
        _textCtrl = null;
    }

    @Override public void setEnabled (boolean enabled) {
        _enabled = enabled;
        if (_textCtrl != null) _textCtrl.setEnabled(enabled);
    }

    public void refresh () {
        if (ctrl == null) return;

        // check the desired style against the existing ones; the mask is needed to filter
        // SWT's other internal styles
        int style = resolveStyle();
        if (style != (_textCtrl.getStyle() & REALLOC_STYLES)) {
            // we need a new instance, just remove and add (this is recursive but next time, the
            // else branch will be taken)
            remove();
            add();
            return;
        }

        // TODO _textComp.setBorder(null);
        // TODO _textComp.setAutoscrolls(true);

        _textCtrl.setEnabled(_enabled);

        // don't set the font unless it's changed, it makes the caret disappear
        Font nfont = _element.resolveStyle(Style.FONT);
        if (_font == null || !_font.equals(nfont))
            _textCtrl.setFont(convert().font(_font = nfont));

        // set foreground
        _textCtrl.setForeground(convert().color(_element.resolveStyle(Style.COLOR)));
        // TODO: Keyboard.TextType textType = resolveStyle(Field.TEXT_TYPE);
        updateBounds();
    }

    @Override public void focus () {
        if (_textCtrl != null) _textCtrl.setFocus();
    }

    @Override public boolean insert (String text) {
        if (_textCtrl == null) return false;
        _textCtrl.insert(text);
        return true;
    }

    protected int resolveStyle () {
        int style = (_element.resolveStyle(Field.MULTILINE) ? SWT.MULTI : SWT.SINGLE) |
            (_element.resolveStyle(Field.SECURE_TEXT_ENTRY) ? SWT.PASSWORD : 0);
        switch (_element.resolveStyle(Style.HALIGN)) {
        case CENTER: style |= SWT.CENTER; break;
        case LEFT: style |= SWT.LEFT; break;
        case RIGHT: style |= SWT.RIGHT; break;
        }
        return style;
    }

    protected SWTConvert convert () {
        return SWTTPPlatform.instance().convert();
    }

    protected final Field.Native _element;
    protected Text _textCtrl;
    protected boolean _enabled = true;
    protected Font _font;

    protected Connection _textConnection;
    protected volatile boolean _textNotifyInProgress;

    /** Styles that force reallocation of the text control if changed. */
    protected static int REALLOC_STYLES = SWT.MULTI | SWT.SINGLE | SWT.PASSWORD |
            SWT.CENTER | SWT.LEFT | SWT.RIGHT;
}
