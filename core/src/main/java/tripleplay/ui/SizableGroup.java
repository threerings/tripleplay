//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import tripleplay.util.DimensionValue;

/**
 * A group that allows configuring its preferred size. The size is always returned when the size
 * of the group is calculated, but the group may end up being stretched when contained in a
 * layout that does so.
 */
public class SizableGroup extends Group
{
    /** The preferred size of this widget. Update at will. */
    public final DimensionValue preferredSize = new DimensionValue(0, 0);

    /** Creates the sizable group with preferred width and height of 0. Note that this will
     * cause the base layout preferred size to be used, if overridden. */
    public SizableGroup (Layout layout) {
        this(layout, 0, 0);
    }

    /** Creates the sizable group with the given preferred size. */
    public SizableGroup (Layout layout, IDimension size) {
        this(layout, size.width(), size.height());
    }

    /** Creates the sizable group with preferred width and height. */
    public SizableGroup (Layout layout, float wid, float hei) {
        super(layout);
        preferredSize.update(wid, hei);
        preferredSize.connect(invalidateSlot());
    }

    @Override
    protected Dimension computeSize(LayoutData ldata, float hintX, float hintY) {
        IDimension pSize = preferredSize.get();
        float pWidth = pSize.width();
        float pHeight = pSize.height();

        Dimension size = super.computeSize(ldata, select(pWidth, hintX), select(pHeight, hintY));
        return new Dimension(select(pWidth, size.width), select(pHeight, size.height));
    }

    private static float select (float pref, float base) {
        return pref == 0 ? base : pref;
    }
}
