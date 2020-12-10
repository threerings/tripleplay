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
    private Take widthFn = Take.PREFERRED_IF_SET;
    private Take heightFn = Take.PREFERRED_IF_SET;

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

    /**
     * Sets the way in which widths are combined to calculate the resulting preferred size.
     * For example, {@code new SizeableGroup(...).forWidth(Take.MAX)}.
     */
    public SizableGroup forWidth (Take fn) {
        widthFn = fn;
        return this;
    }

    /**
     * Sets the way in which heights are combined to calculate the resulting preferred size.
     * For example, {@code new SizeableGroup(...).forHeight(Take.MAX)}.
     */
    public SizableGroup forHeight (Take fn) {
        heightFn = fn;
        return this;
    }

    @Override
    protected Dimension computeSize (LayoutData ldata, float hintX, float hintY) {
        IDimension pSize = preferredSize.get();
        float pWidth = pSize.width();
        float pHeight = pSize.height();

        Dimension size = super.computeSize(ldata, select(pWidth, hintX), select(pHeight, hintY));
        return new Dimension(widthFn.apply(pWidth, size.width), heightFn.apply(pHeight, size.height));
    }

    private static float select (float pref, float base) {
        return pref == 0 ? base : pref;
    }
}
