//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

/** @deprecated Just use TextWidget. */
@Deprecated
public abstract class ClickableTextWidget<T extends ClickableTextWidget<T>> extends TextWidget<T>
{
    protected ClickableTextWidget () {
        enableInteraction();
    }
}
