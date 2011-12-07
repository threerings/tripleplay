//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.CanvasLayer;
import playn.core.Key;
import playn.core.Keyboard;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.TextFormat;
import playn.core.TextLayout;

import pythagoras.f.MathUtil;
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

        // compute the position in the text that was clicked and start the cursor there
        Point parentEvent = new Point(x, y);
        float clickX = (_tlayer == null) ? 0 :
            Layer.Util.parentToLayer(_tlayer, parentEvent, parentEvent).x();
        int cursor = 0;
        float cx = 0;
        while (cursor < text.get().length()) {
            float ncx = getCursorX(cursor);
            if (clickX > ncx) cursor++;
            else {
                if (clickX - cx < ncx - clickX) cursor--;
                break;
            }
            cx = ncx;
        }
        moveCursor(cursor);

        // wire up a focus listener
        root._iface._focused.update((_listener = new FieldListener(cursor)));
        root._iface._focused.connect(new UnitSlot() {
            @Override public void onEmit () {
                _listener = null;
                _clayer.setVisible(false);
                defocused.emit(Field.this);
            }
        }).once();

        _clayer.setVisible(true);
    }

    protected float getCursorX (int cursor) {
        if (cursor == 0) return 0;
        // Get the width up to the cursor with an additional 'b' past that. If the cursor is right
        // past a space, layoutText will trim that space before getting the width. Adding in a 'b'
        // and subtracting it out later will get the width with spaces. Why 'b'? It's my favorite
        // letter, yo.
        TextFormat format = Style.createTextFormat(this);
        String withB = text.get().substring(0, cursor) + "b";
        TextLayout withBL = PlayN.graphics().layoutText(withB, format);
        TextLayout bL = PlayN.graphics().layoutText("b", format);
        return withBL.width() - bL.width() - 1;
    }

    @Override protected String getLayoutText () {
        String ltext = text.get();
        // we always want non-empty text so that we force ourselves to always have a text layer and
        // sane dimensions even if the text field contains no text
        return (ltext == null || ltext.length() == 0) ? " " : ltext;
    }

    @Override protected void createTextLayer (LayoutData ldata, float tx, float ty,
                                              float twidth, float theight,
                                              float availWidth, float availHeight) {
        super.createTextLayer(ldata, tx, ty, twidth, theight, availWidth, availHeight);

        // (re)create our cursor layer if needed
        int cheight = MathUtil.iceil(theight);
        if (_cx != tx || _cy != ty || _clayer == null || _clayer.canvas().height() != cheight) {
            _cx = tx; _cy = ty; // save these for later
            boolean wasVisible = (_clayer != null) && _clayer.visible();
            if (_clayer != null) _clayer.destroy();
            _clayer = PlayN.graphics().createCanvasLayer(2, cheight);
            _clayer.canvas().setFillColor(Styles.resolveStyle(this, Style.COLOR)).
                fillRect(0, 0, 2, theight);
            _clayer.setVisible(wasVisible);
            layer.add(_clayer);
        }

        // force the cursor to be repositioned
        int cursor = _cursor;
        _cursor = -1;
        moveCursor(cursor);
    }

    protected void moveCursor (int pos) {
        int ncursor = Math.max(0, Math.min(text.get().length(), pos));
        if (ncursor != _cursor) {
            _cursor = ncursor;
            float cx, cy;
            if (_tlayer != null) {
                cx = _tlayer.transform().tx();
                cy = _tlayer.transform().ty();
            } else {
                cx = _cx;
                cy = _cy;
            }
            _clayer.setTranslation(cx + getCursorX(_cursor), cy);
        }
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
            default:
                return; // avoid falling through to prevent default
            }

            // prevent the browser (in HTML land) from interpreting this keypress; otherwise, for
            // example, pressing backspace causes the browser to navigate to the previous page
            ev.setPreventDefault(true);
        }

        @Override public void onKeyTyped (Keyboard.TypedEvent ev) {
            String cur = text.get();
            text.update(cur.substring(0, _cursor) + ev.typedChar() + cur.substring(_cursor));
            _cursor++;
            // prevent the browser (in HTML land) from interpreting this keypress
            ev.setPreventDefault(true);
        }

        protected final String _initial = text.get();
    };

    protected FieldListener _listener;
    protected CanvasLayer _clayer;
    protected float _cx, _cy;
    protected int _cursor;
}
