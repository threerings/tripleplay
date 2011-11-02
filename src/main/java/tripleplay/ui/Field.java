//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Key;
import playn.core.Keyboard;
import playn.core.Layer;

import pythagoras.f.Point;

import react.Signal;
import react.UnitSlot;

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
        layoutText(withBLayout, text.get().substring(0, cursor) + "b", null, 0, 0);
        LayoutData bLayout = new LayoutData();
        layoutText(bLayout, "b", null, 0, 0);
        return withBLayout.text.width() - bLayout.text.width() - 1;
    }

    protected class FieldListener extends Keyboard.Adapter {

        public FieldListener(int cursor) {
            _cursor = cursor;
        }

        @Override public void onKeyDown (Keyboard.Event ev) {
            switch (ev.key()) {
                case HOME:
                    moveCursor(0);
                    break;
                case END:
                    moveCursor(text.get().length());
                    break;
                case RIGHT:
                    moveCursor(_cursor + 1);
                    break;
                case LEFT:
                    moveCursor(_cursor - 1);
                    break;
                case ENTER:
                    root()._iface._focused.update(null);
                    break;
                case ESCAPE:
                    text.update(_initial);
                    root()._iface._focused.update(null);
                    break;
                case BACKSPACE:
                    if (_cursor != 0) {
                        String cur = text.get();
                        text.update(cur.substring(0, _cursor-1) + cur.substring(_cursor));
                        _cursor--;
                    }
                    break;
                case DELETE: {
                    String cur = text.get();
                    if (_cursor < cur.length()) {
                        text.update(cur.substring(0, _cursor) + cur.substring(_cursor+1));
                    }
                    break;
                }
            }
        }

        @Override public void onKeyTyped (Keyboard.TypedEvent ev) {
            String cur = text.get();
            text.update(cur.substring(0, _cursor) + ev.typedChar() + cur.substring(_cursor));
            _cursor++;
        }

        protected void moveCursor (int pos) {
            int ncursor = Math.max(0, Math.min(text.get().length(), pos));
            if (ncursor != _cursor) {
                _cursor = ncursor;
                invalidate();
            }
        }

        protected int _cursor;
        protected final String _initial = text.get();
    };

    protected FieldListener _listener;
}
