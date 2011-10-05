//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Keyboard;

import tripleplay.util.Key;

public class Field extends TextWidget<Field>
{
    public Field () {
        this("");
    }

    public Field (String initialText) {
        this(initialText, Styles.none());
    }

    public Field (Styles styles) {
        this("", styles);
    }

    public Field (String initialText, Styles styles) {
        setStyles(styles).text.update(initialText);
    }

    @Override protected void onPointerEnd (float x, float y) {
        super.onPointerEnd(x, y);
        Root root = root();
        if (root == null) return;
        root._iface.setFocus(new FieldListener());
    }

    protected class FieldListener implements Keyboard.Listener {
        @Override public void onKeyDown (Keyboard.Event ev) {
            Key key = Key.get(ev.keyCode());
            if (key == Key.SHIFT) {
                _shift = true;
            }
        }

        @Override public void onKeyUp (Keyboard.Event ev) {
            Key key = Key.get(ev.keyCode());
            if (key == null) {
                System.out.println("Unknown keycode " + key.code);
                return;
            }
            switch (key) {
                case SHIFT: _shift = false; return;
                case RIGHT: _cursor = Math.min(text.get().length(), _cursor + 1); return;
                case LEFT: _cursor = Math.max(0, _cursor - 1); return;
            }

            String precursor = text.get().substring(0, _cursor);
            String postcursor = text.get().substring(_cursor, text.get().length());

            if (key == Key.BACK_SPACE) {
                if (_cursor != 0) {
                    text.update(precursor.substring(0, precursor.length() - 1) + postcursor);
                    _cursor--;
                }
                return;
            }
            if (key.character == null) {
                System.out.println("Not handling " + key);
            } else {
                char toAppend = _shift ? Character.toUpperCase(key.character) : key.character;
                text.update(precursor + toAppend + postcursor);
                _cursor++;
            }
        }

        protected boolean _shift;
        protected int _cursor = text.get().length();
    };
}
