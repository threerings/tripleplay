//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

/**
 * An abstract base class for widgets that contain text.
 */
public abstract class TextWidget extends Widget
{
    /**
     * Returns the text configured for this widget.
     */
    public abstract String text ();

    /**
     * Updates the text configured for this widget.
     */
    public abstract TextWidget setText (String text);
}
