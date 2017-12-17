//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Canvas;

import tripleplay.util.Glyph;

/**
 * Base for widgets that consist of a single glyph. Performs all boilerplate layout stuff and
 * delegates the painting to subclasses: {@link #paint(Canvas)}. Note this should only be used for
 * widgets that need to do some composition of images or other drawing for their display. Otherwise
 * they should do their own arranging of group and image layers etc.
 */
public abstract class GlyphWidget<T extends GlyphWidget<T>> extends SizableWidget<T>
{
    /**
     * Redraws this widget's glyph if the widget is visible and laid out. Called automatically
     * whenever the widget is laid out. Note that this is not the same as {@link #invalidate()}.
     * That's protected and causes all parent containers to re-layout. This simply updates the image.
     */
    public void render () {
        if (isShowing() && _glyph.layer() != null) {
            Canvas canvas = _glyph.begin();
            paint(canvas);
            _glyph.end();
        }
    }

    /**
     * Creates a new glyph widget with no initial size and optionally interactive. The widget will
     * not be functional until one of the sizing methods is called (in {@link SizableWidget}.
     */
    protected GlyphWidget () {}

    /**
     * Creates a new glyph widget with the given preferred size.
     */
    protected GlyphWidget (float width, float height) {
        preferredSize.update(width, height);
    }

    /**
     * Paints this widget onto the given canvas. This is called by render. The canvas is from
     * the {@link #_glyph} member, which is already prepared to the correct laid out size.
     */
    protected abstract void paint (Canvas canvas);

    @Override protected void layout (LayoutData ldata, float left, float top,
                                     float width, float height) {
        super.layout(ldata, left, top, width, height);

        // prepare the glyph
        if (width == 0 && height == 0) {
            _glyph.close();
            return;
        }

        _glyph.prepare(root().iface.plat.graphics(), width, height);
        _glyph.layer().setTranslation(left, top);
        render();
    }

    protected final Glyph _glyph = new Glyph(layer);
}
