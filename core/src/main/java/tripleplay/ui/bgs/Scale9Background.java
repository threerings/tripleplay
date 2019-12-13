//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import pythagoras.f.IDimension;
import pythagoras.f.MathUtil;

import playn.core.Surface;
import playn.core.Tile;
import playn.core.Tint;
import playn.scene.Layer;

import tripleplay.ui.Background;
import tripleplay.ui.Style.HAlign;
import tripleplay.ui.Style.VAlign;
import tripleplay.ui.util.Scale9;

/**
 * A background constructed by scaling the parts of a source image to fit the target width and
 * height. Uses {@link Scale9}.
 */
public class Scale9Background extends Background
{
    /** Creates a new background using the given texture. The texture is assumed to be divided into
     * aa 3x3 grid of 9 equal pieces.
     */
    public Scale9Background (Tile tile) {
        _tile = tile;
        _s9 = new Scale9(tile.width(), tile.height());
    }

    /** Returns the scale 9 instance for mutation. Be sure to finish mutation prior to binding. */
    public Scale9 scale9 () {
        return _s9;
    }

    /** Sets the width of the left and right edges of the source axes to the given value. NOTE:
     * {@code xborder} may be zero, to indicate that the source image has no left or right pieces,
     * i.e. just three total pieces: top, bottom and center.
     */
    public Scale9Background xborder (float xborder) {
        _s9.xaxis.resize(0, xborder);
        _s9.xaxis.resize(2, xborder);
        return this;
    }

    /** Sets the height of the top and bottom edges of the source axes to the given value. NOTE:
     * {@code yborder} may be zero, to indicate that the source image has no top or bottom pieces,
     * i.e. just three pieces: left, right and center.
     */
    public Scale9Background yborder (float yborder) {
        _s9.yaxis.resize(0, yborder);
        _s9.yaxis.resize(2, yborder);
        return this;
    }

    /** Sets all edges of the source axes to the given value. Equivalent of calling {@code
     * xborder(border).yborder(border)}.
     */
    public Scale9Background corners (float size) {
        return xborder(size).yborder(size);
    }

    /** Sets an overall destination scale for the background. When instantiated, the target width
     * and height are divided by this value, and when rendering the layer scale is multiplied by
     * this value. This allows games to use high res images with smaller screen sizes.
     */
    public Scale9Background destScale (float scale) {
        _destScale = scale;
        return this;
    }

    @Override
    protected Instance instantiate (final IDimension size) {
        return new LayerInstance(size, new Layer() {
            // The destination scale 9.
            Scale9 dest = new Scale9(size.width() / _destScale, size.height() / _destScale, _s9);
            @Override protected void paintImpl (Surface surf) {
                surf.saveTx();
                surf.scale(_destScale, _destScale);
                Float alpha = Scale9Background.this.alpha;
                if (alpha != null) surf.setAlpha(alpha);
                if (_tint != Tint.NOOP_TINT) surf.setTint(_tint);
                // issue the 9 draw calls
                for (int yy = 0; yy < 3; ++yy) for (int xx = 0; xx < 3; ++xx) {
                    drawPart(surf, xx, yy);
                }
                if (alpha != null) surf.setAlpha(1); // alpha is not part of save/restore
                surf.restoreTx();
            }

            protected void drawPart (Surface surf, int x, int y) {
                float dw = dest.xaxis.size(x), dh = dest.yaxis.size(y);
                if (dw == 0 || dh == 0) return;
                float pw = _s9.xaxis.size(x), ph = _s9.yaxis.size(y);
                int xTimes = MathUtil.iceil(dw / pw);
                int yTimes = MathUtil.iceil(dh / ph);
                float startX = dest.xaxis.coord(x);
                float startY = dest.yaxis.coord(y);
                if (_repeatMode) {
                    float absStartX = startX + surf.tx().tx, absStartY = startY + surf.tx().ty;
                    surf.startClipped(MathUtil.ifloor(absStartX), MathUtil.ifloor(absStartY), MathUtil.iceil(dw), MathUtil.iceil(dh));
                    if(_verticalAlignment == VAlign.BOTTOM) startY -= Math.abs(yTimes*ph-dh);
                    if(_horizontalAlignment == HAlign.RIGHT) startX -= Math.abs(xTimes*pw-dw);
                    for (int c = 0; c < xTimes; c++) {
                        if (startX > dest.xaxis.coord(x) + dw) break;
                        for (int r = 0; r < yTimes; r++) {
                            if (startY > dest.yaxis.coord(y) + dh) break;
                            surf.draw(_tile, startX, startY, pw, ph, _s9.xaxis.coord(x), _s9.yaxis.coord(y),
                                    _s9.xaxis.size(x), _s9.yaxis.size(y));
                            startY += ph;
                        }
                        startY = dest.yaxis.coord(y);
                        startX += pw;
                    }
                    surf.endClipped();
                } else {
                    surf.draw(_tile, dest.xaxis.coord(x), dest.yaxis.coord(y), dw, dh,
                            _s9.xaxis.coord(x), _s9.yaxis.coord(y),
                            _s9.xaxis.size(x),  _s9.yaxis.size(y));
                }
            }
        });
    }

    /**
     * Controls wether the the extended parts are scaled or repeated. 
     * @param repeat Repeats the extended parts if true otherwise scales them.
     */
    public void setRepeatMode (boolean repeat) {
        _repeatMode = repeat;
    }

    /**
     * Sets the tint for this background, as {@code ARGB}.
     *
     * <p> <em>NOTE:</em> this will overwrite any value configured via {@link #alpha}. Either
     * include your desired alpha in the high bits of {@code tint} or set {@link #alpha} after
     * calling this method. </p>
     *
     * <p> <em>NOTE:</em> the RGB components of a layer's tint only work on GL-based backends. It
     * is not possible to tint layers using the HTML5 canvas and Flash backends. </p>
     */
    public Scale9Background setTint (int tint) {
        _tint = tint;
        this.alpha = ((tint >> 24) & 0xFF) / 255f;
        return this;
    }

    /**
     * Defines how the repeated parts are aligned horizontally when the repeatMode is turned on.
     * By default the repeated parts are aligned to the left border and left corners respectively.
     * 
     * @param horizontalAlignment Only left or right alignment are supported.
     */
    public void setRepeatAlignment (HAlign horizontalAlignment) {
        _horizontalAlignment = horizontalAlignment;
    }

    /**
     * Defines how the repeated parts are aligned vertically when the repeatMode is turned on.
     * By default the repeated parts are aligned to the top border and top corners respectively.
     * 
     * @param verticalAlignment Only top or bottom alignment are supported.
     */
    public void setRepeatAlignment (VAlign verticalAlignment) {
        _verticalAlignment = verticalAlignment;
    }

    protected Tile _tile;
    protected Scale9 _s9;
    protected float _destScale = 1;
    protected int _tint = Tint.NOOP_TINT;
    protected boolean _repeatMode = false;
    protected HAlign _horizontalAlignment = HAlign.LEFT;
    protected VAlign _verticalAlignment = VAlign.TOP; 
}
