//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

/**
* A button that displays text, or an icon, or both.
*/
public class Button extends ClickableTextWidget<Button>
{
    @Override public String toString () {
        return "Button(" + text() + ")";
    }
}
