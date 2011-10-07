//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Keyboard;
import playn.core.Layer;

import pythagoras.f.Point;

import react.Signal;
import react.UnitSlot;

import tripleplay.util.Key;

public class Field extends TextWidget<Field>
{
    public final Signal<Field> defocused = Signal.create();

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

    public boolean isFocused () { return _listener != null; }

    @Override protected void onPointerEnd (float x, float y) {
        super.onPointerEnd(x, y);
        Root root = root();
        if (root == null) return;
        Point parentEvent = new Point(x, y);
        float tLayerX = Layer.Util.parentToLayer(_tlayer, parentEvent, parentEvent).x();
        int cursor = 0;
        while (cursor < text.get().length() && getCursorX(cursor) < tLayerX) cursor++;
        root._iface._focused.update((_listener = new FieldListener(cursor)));
        root._iface._focused.connect(new UnitSlot() {
            @Override public void onEmit () {
                _listener = null;
                defocused.emit(Field.this);
                invalidate();
            }
        }).once();

        invalidate();// Redraw with our cursor
    }

    @Override protected void renderLayout (LayoutData ldata, float x, float y, float width,
        float height) {
        super.renderLayout(ldata, x, y, width, height);
        if (_tlayer == null || _listener == null ) return;

        float cursorX = getCursorX(_listener._cursor);
        _tlayer.canvas().setStrokeWidth(1).setStrokeColor(0xFF000000).
            drawLine(cursorX, 0, cursorX, _tlayer.height());
    }

    protected float getCursorX (int cursor) {
        if (cursor == 0) return 0;
        // Get the width up to the cursor with an additional 'b' past that. If the cursor is right
        // past a space, layoutText will trim that space before getting the width. Adding in a 'b'
        // and subtracting it out later will get the width with spaces. Why 'b'? It's my favorite
        // letter, yo.
        LayoutData withBLayout = new LayoutData();
        layoutText(withBLayout, text.get().substring(0, cursor) + "b", 0, 0);
        LayoutData bLayout = new LayoutData();
        layoutText(bLayout, "b", 0, 0);
        return withBLayout.text.width() - bLayout.text.width() - 1;
    }

    protected class FieldListener implements Keyboard.Listener {

        public FieldListener(int cursor) {
            _cursor = cursor;
        }

        @Override public void onKeyDown (Keyboard.Event ev) {
            Key key = Key.get(ev.keyCode());
            if (key == Key.SHIFT) _shift = true;
        }

        @Override public void onKeyUp (Keyboard.Event ev) {
            Key key = Key.get(ev.keyCode());
            if (key == null) {
                System.out.println("Unknown keycode " + ev.keyCode());
                return;
            }
            switch (key) {
                case SHIFT: _shift = false; return;
                case RIGHT:
                    _cursor = Math.min(text.get().length(), _cursor + 1);
                    invalidate();// Redraw cursor
                    return;
                case LEFT:
                    _cursor = Math.max(0, _cursor - 1);
                    invalidate();// Redraw cursor
                    return;
                case ENTER: root()._iface._focused.update(null); return;
                case ESCAPE:
                    text.update(_initial);
                    root()._iface._focused.update(null);
                    return;
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
                char toAppend = _shift ? key.upper : key.character;
                text.update(precursor + toAppend + postcursor);
                _cursor++;
            }
        }

        protected boolean _shift;
        protected int _cursor;
        protected final String _initial = text.get();
    };

    protected FieldListener _listener;
}
