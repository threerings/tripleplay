//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import pythagoras.f.IPoint;
import pythagoras.f.Points;

import playn.core.Image;
import playn.core.ImageLayer;

/**
 * A simple implementation of {@link Frames} that uses an untrimmed horizontal strip image.
 */
public class SimpleFrames implements Frames
{
    /**
     * Creates an instance with the supplied source image. The frames are assumed to be all in a
     * single row, thus the height of the image defines the height of the frame.
     * @param width the width of each frame.
     */
    public SimpleFrames (Image source, float width) {
        this(source, width, source.height());
    }

    /**
     * Creates an instance with the supplied source image. The image is assumed to contain a
     * complete sheet of frames, each {@code width x height} in size.
     * @param width the width of each frame.
     * @param height the width of each frame.
     */
    public SimpleFrames (Image source, float width, float height) {
        this(source, width, height, (int)(source.height()/height) * (int)(source.width()/width));
    }

    /**
     * Creates an instance with the supplied source image. The image is assumed to contain {@code
     * count} frames, each {@code width x height} in size, in row major order (any missing frames
     * are on the right side of the bottom row).
     * @param width the width of each frame.
     * @param height the width of each frame.
     */
    public SimpleFrames (Image source, float width, float height, int count) {
        _source = source;
        _width = width;
        _height = height;
        _count = count;
    }

    @Override public float width () {
        return _width;
    }

    @Override public float height () {
        return _height;
    }

    @Override public int count () {
        return _count;
    }

    @Override public Image frame (int index) {
        int cols = cols(), row = (index % cols), col = (index / cols);
        return _source.subImage(_width * row, _height * col, _width, _height);
    }

    @Override public IPoint offset (int index) {
        return Points.ZERO; // we have no offsets
    }

    @Override public void apply (int index, ImageLayer layer) {
        layer.setTranslation(0, 0);
        int cols = cols(), row = (index % cols), col = (index / cols);
        Image cur = layer.image();
        if (cur instanceof Image.Region) {
            Image.Region curr = (Image.Region)cur;
            if (curr.parent() == _source) {
                curr.setBounds(_width * row, _height * col, _width, _height);
                return;
            }
        }
        layer.setImage(frame(index));
    }

    protected int cols () {
        return (int)(_source.width() / _width);
    }

    protected final Image _source;
    protected final float _width, _height;
    protected final int _count;
}
