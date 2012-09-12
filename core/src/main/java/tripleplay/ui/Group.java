//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

/**
* A grouping element that contains other elements and lays them out according to a layout policy.
*/
public class Group extends Elements<Group>
{
    /**
     * Creates a group with the specified layout and styles.
     */
    public Group (Layout layout, Styles styles) {
        super(layout);
        setStyles(styles);
    }

    /**
     * Creates a group with the specified layout and styles (in the DEFAULT mode).
     */
    public Group (Layout layout, Style.Binding<?>... styles) {
        super(layout);
        setStyles(styles);
    }

    /**
     * Creates a group with the specified layout.
     */
    public Group (Layout layout) {
        super(layout);
    }

    @Override protected Class<?> getStyleClass ()
    {
        return Group.class;
    }
}
