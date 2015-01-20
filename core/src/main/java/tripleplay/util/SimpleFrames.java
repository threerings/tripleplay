//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import pythagoras.f.IPoint;
import pythagoras.f.IRectangle;
import pythagoras.f.Points;
import pythagoras.f.Rectangle;

import playn.core.Texture;
import playn.scene.ImageLayer;

/**
 * A simple implementation of {@link Frames} that uses an untrimmed horizontal strip image.
 */
public class SimpleFrames implements Frames
{
    /**
     * Creates an instance with the supplied source texture. The frames are assumed to be all in a
     * single row, thus the height of the image defines the height of the frame.
     * @param width the width of each frame.
     */
    public SimpleFrames (Texture source, float width) {
        this(source, width, source.displayHeight);
    }

    /**
     * Creates an instance with the supplied source texture. The image is assumed to contain a
     * complete sheet of frames, each {@code width x height} in size.
     * @param width the width of each frame.
     * @param height the width of each frame.
     */
    public SimpleFrames (Texture source, float width, float height) {
        this(source, width, height,
             (int)(source.displayHeight/height) * (int)(source.displayWidth/width));
    }

    /**
     * Creates an instance with the supplied source texture. The image is assumed to contain {@code
     * count} frames, each {@code width x height} in size, in row major order (any missing frames
     * are on the right side of the bottom row).
     * @param width the width of each frame.
     * @param height the width of each frame.
     */
    public SimpleFrames (Texture source, float width, float height, int count) {
        _source = source;
        _width = width;
        _height = height;
        _count = count;
    }

    @Override public float width () { return _width; }
    @Override public float height () { return _height; }
    @Override public int count () { return _count; }
    @Override public IRectangle bounds (int index) { return bounds(index, new Rectangle()); }
    @Override public IPoint offset (int index) { return Points.ZERO; } // we have no offsets

    @Override public void apply (int index, ImageLayer layer) {
        layer.setTile(_source);
        layer.setTranslation(0, 0);
        Rectangle r = layer.region;
        if (r == null) r = (layer.region = new Rectangle());
        bounds(index, r);
    }

    protected int cols () { return (int)(_source.displayWidth / _width); }

    protected Rectangle bounds (int index, Rectangle r) {
        int cols = cols(), row = (index % cols), col = (index / cols);
        r.x = _width * row;
        r.y = _height * col;
        r.width = _width;
        r.height = _height;
        return r;
    }

    protected final Texture _source;
    protected final float _width, _height;
    protected final int _count;
}
