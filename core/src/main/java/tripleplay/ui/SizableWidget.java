//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.IDimension;

import tripleplay.util.DimensionValue;
import tripleplay.util.Glyph;

import static tripleplay.ui.Log.log;

/**
 * A widget that allows configuring its preferred size. The size is always returned when the size
 * of the widget is calculated, but the widget may end up being stretched when contained in a
 * layout that does so.
 */
public abstract class SizableWidget<T extends SizableWidget<T>> extends Widget<T>
{
    /** The preferred size of this widget. Update at will. */
    public final DimensionValue preferredSize = new DimensionValue(0, 0);

    /** Creates the sizable widget with preferred width and height of 0. Note that this will
     * cause the base layout preferred size to be used, if overridden. */
    public SizableWidget () {
        this(0, 0);
    }

    /** Creates the sizable widget with the given preferred size. */
    public SizableWidget (IDimension size) {
        this(size.width(), size.height());
    }

    /** Creates the sizable widget with preferred width and height. */
    public SizableWidget (float width, float height) {
        preferredSize.update(width, height);
        preferredSize.connect(ps -> invalidate());
    }

    /** Creates the layout to which the widget's {@link Element.SizableLayoutData} will delegate. */
    protected LayoutData createBaseLayoutData (float hintX, float hintY) {
        return null;
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        // use a sizable layout data with our preferred size and delegate to the base, if any
        return new SizableLayoutData(createBaseLayoutData(hintX, hintY), null, preferredSize.get());
    }

    /**
     * Returns a new {@link Glyph} that has been prepared to this SizableWidget's
     * {@link #preferredSize}. If that size is 0 in either dimension, a warning is logged and null
     * is returned.
     */
    protected Glyph prepareGlyph () {
        return prepareGlyph(null);
    }

    /**
     * Prepares the given {@link Glyph} (or creates a new one if null) that has been prepared to
     * this SizableWidget's {@link #preferredSize}. If that size is 0 in either dimension, a warning
     * is logged and null is returned.
     */
    protected Glyph prepareGlyph (Glyph glyph) {
        IDimension size = preferredSize.get();
        if (size.width() == 0 || size.height() == 0) {
            log.warning("SizableWidget cannot prepare a glyph with a 0 dimension", "size", size);
            return null;
        }

        glyph = glyph == null ? new Glyph(layer) : glyph;
        glyph.prepare(root().iface.plat.graphics(), size);
        return glyph;
    }
}
