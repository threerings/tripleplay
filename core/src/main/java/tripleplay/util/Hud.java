//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.ArrayList;
import java.util.List;

import react.Value;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.ImageLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;
import playn.core.gl.GLContext;
import static playn.core.PlayN.graphics;

/**
 * Maintains a (usually debugging) HUD with textual information displayed in one or two colums. The
 * text is all rendered to a single {@link CanvasImage} (and updated only when values change) to
 * put as little strain on the renderer as possible. Example usage:
 * <pre>{@code
 * class MyGame extends Game.Default {
 *   private Hud.Stock hud = new Hud.Stock();
 *   public void init () {
 *     hud.layer.setDepth(Short.MAX_VALUE);
 *     graphics().rootLayer().add(hud.layer);
 *   }
 *   public void update (int delta) {
 *     hud.update(delta);
 *   }
 *   public void paint (float alpha) {
 *     hud.paint();
 *   }
 * }
 * }</pre>
 */
public class Hud
{
    /** A stock HUD that provides a bunch of standard PlayN performance info and handles
     * once-per-second updating. */
    public static class Stock extends Hud {
        public Stock () {
            _haveGL = (graphics().ctx() != null);
            if (_haveGL) {
                add("Shader info:", true);
                add(_quadShader);
                add(_trisShader);
            }
            add("Per second:", true);
            add("Frames:", _frames);
            if (_haveGL) {
                add("Shader creates:", _shaderCreates);
                add("FB creates:", _fbCreates);
                add("Tex creates:", _texCreates);
                add("Per frame:", true);
                add("Shader binds:", _shaderBinds);
                add("FB binds:", _fbBinds);
                add("Tex binds:", _texBinds);
                add("Quads drawn:", _rQuads);
                add("Tris drawn:", _rTris);
                add("Shader flushes:", _shaderFlushes);
            }
        }

        /** Call this from your {@code Game.update} method (or similar). */
        public void update (int delta) {
            long now = System.currentTimeMillis();
            if (now > _nextSec) {
                willUpdate();
                update();
                _nextSec = now + 1000;
            }
        }

        /** Call this from your {@code Game.paint} method (or similar). */
        public void paint () {
            _paints += 1;
        }

        /** Called when the HUD is about to update its display. Values added to the HUD should be
         * updated by this call if they've not been already. Must call {@code super.willUpdate()}. */
        protected void willUpdate () {
            if (_haveGL) {
                GLContext.Stats stats = graphics().ctx().stats();
                int frames = Math.max(stats.frames, 1);
                _frames.update(frames);
                _shaderCreates.update(stats.shaderCreates);
                _fbCreates.update(stats.frameBufferCreates);
                _texCreates.update(stats.texCreates);
                _shaderBinds.update(stats.shaderBinds/frames);
                _fbBinds.update(stats.frameBufferBinds/frames);
                _texBinds.update(stats.texBinds/frames);
                _rQuads.update(stats.quadsRendered/frames);
                _rTris.update(stats.trisRendered/frames);
                _shaderFlushes.update(stats.shaderFlushes/frames);
                stats.reset();
                _quadShader.update("Quad: " + graphics().ctx().quadShaderInfo());
                _trisShader.update("Tris: " + graphics().ctx().trisShaderInfo());
            } else {
                _frames.update(_paints);
                _paints = 0;
            }
        }

        protected final boolean _haveGL;
        protected long _nextSec;

        protected final Value<Integer> _frames = Value.create(0);
        protected final Value<Integer> _shaderCreates = Value.create(0);
        protected final Value<Integer> _fbCreates = Value.create(0);
        protected final Value<Integer> _texCreates = Value.create(0);
        protected final Value<Integer> _shaderBinds = Value.create(0);
        protected final Value<Integer> _fbBinds = Value.create(0);
        protected final Value<Integer> _texBinds = Value.create(0);
        protected final Value<Integer> _rQuads = Value.create(0);
        protected final Value<Integer> _rTris = Value.create(0);
        protected final Value<Integer> _shaderFlushes = Value.create(0);

        protected final Value<String> _quadShader = Value.create("");
        protected final Value<String> _trisShader = Value.create("");
    }

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
        width += 5*GAP;
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
    protected int _paints;

    protected TextFormat _fmt = new TextFormat().withFont(
        graphics().createFont("Helvetica", Font.Style.PLAIN, 12));
    protected int _textColor = 0xFF000000, _bgColor = 0xFFFFFFFF;
    protected CanvasImage _image = graphics().createImage(1, 1);

    protected static final float GAP = 5;
}
