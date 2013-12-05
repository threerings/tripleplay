//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.GroupLayer;
import playn.core.TextFormat;
import playn.core.TextLayout;

/**
 * A {@link Glyph} that maintains a rendered bit of text. Handles resizing the glyph as needed to
 * accommodate changed text, and also avoids rerendering text when a request is made to render with
 * the exact same configuration as the previous rendering. This saves clients from the trouble of
 * caching and comparing the substantial configuration associated with rendered text.
 */
public class TextGlyph extends Glyph
{
    public TextGlyph (GroupLayer parent) {
        super(parent);
    }

    public TextGlyph (GroupLayer parent, float depth) {
        super(parent, depth);
    }

    /**
     * Prepares the glyph at size {@code width x height} and renders {@code text} into it with the
     * specified stylings.
     */
    public void renderText (float width, float height, TextLayout text, EffectRenderer renderer,
                            int color, boolean underlined) {
        renderText(width, height, text, renderer, color, underlined, 0, 0);
    }

    /**
     * Prepares the glyph at size {@code width x height} and renders {@code text} into it with the
     * specified stylings and at the specified offset ({@code tx, ty}).
     */
    public void renderText (float width, float height, TextLayout text, EffectRenderer renderer,
                            int color, boolean underlined, float tx, float ty) {
        if (width != _rwidth || height != _rheight || color != _rcolor ||
            underlined != _runderlined || !renderer.equals(_rrenderer) ||
            !text.format().equals(_rformat) || !text.text().equals(_rtext)) {
            prepare(width, height);
            renderer.render(canvas(), text, color, underlined, tx, ty);
            _rwidth = width;
            _rheight = height;
            _rrenderer = renderer;
            _rcolor = color;
            _runderlined = underlined;
            _rformat = text.format();
            _rtext = text.text();
        }
    }

    protected float _rwidth, _rheight;
    protected String _rtext;
    protected TextFormat _rformat;
    protected EffectRenderer _rrenderer;
    protected int _rcolor;
    protected boolean _runderlined;
}
