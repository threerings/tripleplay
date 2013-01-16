//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.IDimension;
import react.UnitSlot;
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
        preferredSize.connect(new UnitSlot() {
            @Override public void onEmit () {
                invalidate();
            }
        });
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        // use a sizable layout data with the usual layout and hybrid size
        return new SizableLayoutData(super.createLayoutData(hintX, hintY), preferredSize.get());
    }
}
