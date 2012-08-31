//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.IDimension;
import react.UnitSlot;
import tripleplay.util.DimensionValue;

/**
 * A widget that allows configuring its preferred size. The size is always returned when the size
 * of the widget is calculated, but the widget may end up being stretched when contained in a
 * layout that does so.
 */
public class SizableWidget<T extends SizableWidget<T>> extends Widget<T>
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
        preferredSize.connect(new UnitSlot() {
            @Override public void onEmit () {
                invalidate();
            }
        });
    }

    /** Creates the layout to which the widget's {@link Element.SizableLayoutData} will delegate. */
    protected BaseLayoutData createBaseLayoutData (float hintX, float hintY) {
        return null;
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        // use a sizable layout data with our preferred size and delegate to the base, if any
        return new SizableLayoutData(createBaseLayoutData(hintX, hintY), null, preferredSize.get());
    }
}
