//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;

import playn.core.Layer;
import pythagoras.f.IDimension;
import pythagoras.f.Point;
import tripleplay.ui.Element;
import tripleplay.ui.Style.HAlign;
import tripleplay.ui.Style.VAlign;

/** Defines a point relative to a box. */
public class BoxPoint
{
    /** The top left corner. */
    public static final BoxPoint TL = new BoxPoint(0, 0);

    /** The bottom left corner. */
    public static final BoxPoint BL = new BoxPoint(0, 1);

    /** The top right corner. */
    public static final BoxPoint TR = new BoxPoint(1, 0);

    /** The bottom right corner. */
    public static final BoxPoint BR = new BoxPoint(1, 1);

    /** The center of the box. */
    public static final BoxPoint CENTER = new BoxPoint(.5f, .5f);

    /** Normalized x, y coordinates. For example, nx = 1 is the right edge. */
    public final float nx, ny;

    /** Absolute x, y offsets. */
    public final float ox, oy;

    /** Creates a new box point that will resolve to the given normalized coordinates. */
    public BoxPoint (float nx, float ny) {
        this(nx, ny, 0, 0);
    }

    /** Creates a new box point that will resolve to the given normalized coordinates plus
     * the given absolute coordinates. */
    public BoxPoint (float nx, float ny, float ox, float oy) {
        this.nx = nx;
        this.ny = ny;
        this.ox = ox;
        this.oy = oy;
    }

    /** Creates a new box point that is equivalent to this one except with an x coordinate that
     * will resolve to the left edge of the box. */
    public BoxPoint left () {
        return nx(0);
    }

    /** Creates a new box point that is equivalent to this one except with an x coordinate that
     * will resolve to the right edge of the box. */
    public BoxPoint right () {
        return nx(1);
    }

    /** Creates a new box point that is equivalent to this one except with a y coordinate that
     * will resolve to the top edge of the box. */
    public BoxPoint top () {
        return ny(0);
    }

    /** Creates a new box point that is equivalent to this one except with a y coordinate that
     * will resolve to the top bottom of the box. */
    public BoxPoint bottom () {
        return ny(1);
    }

    /** Creates a new box point that is equivalent to this one except with x, y coordinates that
     * will resolve to the center of the box. */
    public BoxPoint center () {
        return new BoxPoint(.5f, .5f, ox, oy);
    }

    /** Creates a new box point that is equivalent to this one except with given offset
     * coordinates. */
    public BoxPoint offset (float x, float y) {
        return new BoxPoint(nx, ny, x, y);
    }

    /** Creates a new box point that is equivalent to this one except with the given normalized
     * y coordinate. */
    public BoxPoint ny (float ny) {
        return new BoxPoint(nx, ny, ox, oy);
    }

    /** Creates a new box point that is equivalent to this one except with the given horizontal
     * and vertical alignment. */
    public BoxPoint align (HAlign halign, VAlign valign) {
        return new BoxPoint(halign.offset(0, 1), valign.offset(0, 1), ox, oy);
    }

    /** Creates a new box point that is equivalent to this one except with the given y alignment.
     * This is a shortcut for calling {@link #ny(float)} with 0, .5, or 1. */
    public BoxPoint valign (VAlign valign) {
        return ny(valign.offset(0, 1));
    }

    /** Creates a new box point that is equivalent to this one except with the given normalized
     * x coordinate. */
    public BoxPoint nx (float nx) {
        return new BoxPoint(nx, ny, ox, oy);
    }

    /** Creates a new box point that is equivalent to this one except with the given x alignment.
     * This is a shortcut for calling {@link #nx(float)} with 0, .5, or 1. */
    public BoxPoint halign (HAlign halign) {
        return nx(halign.offset(0, 1));
    }

    /** Finds the screen coordinates of the point, using the given element as the box. */
    public Point resolve (Element<?> elem, Point dest) {
        Layer.Util.layerToScreen(elem.layer, dest.set(0, 0), dest);
        return resolve(dest.x, dest.y, elem.size().width(), elem.size().height(), dest);
    }

    /** Finds the coordinates of the point, using the box defined by the given coordinates. */
    public Point resolve (float x, float y, float width, float height, Point dest) {
        return dest.set(x + ox + nx * width, y + oy + ny * height);
    }

    /** Finds the coordinates of the point, using the box with top left of 0, 0 and the given
     * dimension. */
    public Point resolve (IDimension size, Point dest) {
        return resolve(0, 0, size.width(), size.height(), dest);
    }
}
