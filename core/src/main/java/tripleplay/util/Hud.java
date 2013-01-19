//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.ArrayList;
import java.util.List;

import react.Value;

import playn.core.Font;
import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.ImageLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import static playn.core.PlayN.graphics;

/**
 * Maintains a (usually debugging) HUD with textual information displayed in one or two colums. The
 * text is all rendered to a single {@link CanvasImage} (and updated only when values change) to
 * put as little strain on the renderer as possible.
 */
public class Hud
{
    /** The image layer that contains this HUD. Add to the scene graph where desired. */
    public final ImageLayer layer;

    public Hud () {
        layer = graphics().createImageLayer(_image);
    }

    /** Configures the font used to display the HUD. Must be called before adding rows. */
    public Hud setFont (Font font) {
        if (!_rows.isEmpty()) throw new IllegalStateException("Set font before adding rows.");
        _fmt = _fmt.withFont(font);
        return this;
    }

    /** Configures the foreground and background colors. Must be called before adding rows. */
    public Hud setColors (int textColor, int bgColor) {
        if (!_rows.isEmpty()) throw new IllegalStateException("Set colors before adding rows.");
        _textColor = textColor;
        _bgColor = bgColor;
        return this;
    }

    /** Adds a static label that spans the width of the HUD. */
    public void add (String label, final boolean header) {
        final TextLayout layout = graphics().layoutText(label, _fmt);
        _rows.add(new Row() {
            public void update () {} // noop
            public float labelWidth () { return 0; }
            public float width () { return layout.width(); }
            public float height() { return layout.height(); }
            public void render (Canvas canvas, float x, float y, float valueX) {
                if (header) canvas.drawLine(0, y-1, canvas.width(), y-1);
                canvas.fillText(layout, x, y);
                float by = y + layout.height();
                if (header) canvas.drawLine(0, by, canvas.width(), by);
            }
        });
    }

    /** Adds a changing label that spans the width of the HUD. */
    public void add (final Value<?> label) {
        _rows.add(new Row() {
            public void update () {
                _layout = graphics().layoutText(String.valueOf(label.get()), _fmt);
            }
            public float labelWidth () { return 0; }
            public float width () { return _layout.width(); }
            public float height() { return _layout.height(); }
            public void render (Canvas canvas, float x, float y, float valueX) {
                canvas.fillText(_layout, x, y);
            }
            protected TextLayout _layout;
        });
    }

    /** Adds a static label and changing value, which will be rendered in two columns. */
    public void add (String label, final Value<?> value) {
        final TextLayout llayout = graphics().layoutText(label, _fmt);
        _rows.add(new Row() {
            public void update () {
                _vlayout = graphics().layoutText(String.valueOf(value.get()), _fmt);
            }
            public float labelWidth () { return llayout.width(); }
            public float width () { return llayout.width() + GAP + _vlayout.width(); }
            public float height() { return Math.max(llayout.height(), _vlayout.height()); }
            public void render (Canvas canvas, float x, float y, float valueX) {
                canvas.fillText(llayout, x, y);
                canvas.fillText(_vlayout, valueX, y);
            }
            protected TextLayout _vlayout;
        });
    }

    /** Updates the HUDs rendered image. Call this after all of its values have been updated
     * (usually once per second). */
    public void update () {
        // update all of our rows and compute layout metrics
        float width = 0, height = 0, labelWidth = 0;
        for (Row row : _rows) {
            row.update();
            width = Math.max(row.width(), width);
            labelWidth = Math.max(row.labelWidth(), labelWidth);
            height += row.height();
        }
        // add in borders
        width += 2*GAP;
        height += GAP*_rows.size()+GAP;
        // create a new image if necessary
        if (_image.width() < width || _image.height() < height) {
            layer.setImage(_image = graphics().createImage(width, height));
        }
        // clear our image and render our rows
        Canvas canvas = _image.canvas();
        canvas.clear();
        canvas.setFillColor(_bgColor).fillRect(0, 0, width, height);
        canvas.setStrokeColor(_textColor).setFillColor(_textColor);
        float x = GAP, y = GAP, valueX = labelWidth+2*GAP;
        for (Row row : _rows) {
            row.render(canvas, x, y, valueX);
            y += row.height()+GAP;
        }
    }

    protected interface Row {
        void update ();
        float labelWidth ();
        float width ();
        float height ();
        void render (Canvas canvas, float x, float y, float valueX);
    }

    protected final List<Row> _rows = new ArrayList<Row>();
    protected TextFormat _fmt = new TextFormat().withFont(
        graphics().createFont("Helvetica", Font.Style.PLAIN, 12));
    protected int _textColor = 0xFF000000, _bgColor = 0xFFFFFFFF;
    protected CanvasImage _image = graphics().createImage(1, 1);

    protected static final float GAP = 5;
}
