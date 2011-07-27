//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import forplay.core.Canvas;
import forplay.core.CanvasLayer;
import forplay.core.ForPlay;
import forplay.core.Layer;

/**
 * The base class for all user interface widgets. Provides helper methods for managing a canvas
 * into which a widget is rendered when its state changes.
 */
public abstract class Widget extends Element
{
    /**
     * Prepares a canvas layer as a rendering target of the supplied dimensions. The canvas may be
     * null, in which case a new canvas will be created. If the supplied canvas is too small, it
     * will be destroyed and a new canvas created. Otherwise the canvas will be cleared. If a new
     * canvas is created, it will automatically be added to this widget's {@link #layer}.
     */
    protected CanvasLayer prepareCanvas (CanvasLayer cl, float width, float height) {
        // recreate our canvas if we need more room than we have (TODO: should we ever shrink it?)
        int cwidth = (int)Math.ceil(width), cheight = (int)Math.ceil(height);
        if (cl == null || cl.canvas().width() < cwidth || cl.canvas().height() < cheight) {
            if (cl != null) cl.destroy();
            layer.add(cl = ForPlay.graphics().createCanvasLayer(cwidth, cheight));
        } else {
            cl.canvas().clear();
        }
        return cl;
    }
}
