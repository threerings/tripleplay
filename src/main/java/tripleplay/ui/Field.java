//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.ImmediateLayer;
import playn.core.Key;
import playn.core.Keyboard;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.Surface;
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
        // create our cursor layer
        _clayer = PlayN.graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            public void render (Surface surf) {
                surf.setFillColor(_ccolor);
                surf.fillRect(0, 0, 1, _cheight);
            }
        });
        _clayer.setVisible(false);
        layer.add(_clayer);
    }

    public boolean isFocused () { return _listener != null; }

    /**
     * Sets the focus to this field.
     */
    public void focus () {
        Root root = root();
        if (root == null) return;
        int cursor = Math.max(0, text.get().length() - 1);
        moveCursor(cursor);
        startFocus(root, cursor);
    }

    @Override protected void onPointerStart (Pointer.Event event, float x, float y) {
        super.onPointerStart(event, x, y);
        Root root = root();
        if (root == null) return;

        // compute the position in the text that was clicked and start the cursor there
        Point parentEvent = new Point(x, y);
        float clickX = (_tglyph.layer() == null) ? 0 :
            Layer.Util.parentToLayer(_tglyph.layer(), parentEvent, parentEvent).x();
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
        startFocus(root, cursor);
    }

    protected void startFocus (Root root, int cursor) {
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

        // note some bits for our cursor
        _cx = tx; _cy = ty;
        _cheight = theight;
        _ccolor = Styles.resolveStyle(this, Style.COLOR);

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
            if (_tglyph.layer() != null) {
                cx = _tglyph.layer().transform().tx();
                cy = _tglyph.layer().transform().ty();
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
    protected ImmediateLayer _clayer;
    protected float _cx, _cy;
    protected int _cursor;
    protected float _cheight;
    protected int _ccolor;
}
