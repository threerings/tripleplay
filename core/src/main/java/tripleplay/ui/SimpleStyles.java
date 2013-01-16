//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

/**
 * Provides a simple style sheet that is useful for development and testing.
 */
public class SimpleStyles
{
    /**
     * Creates and returns a simple default stylesheet.
     */
    public static Stylesheet newSheet () {
        return newSheetBuilder().create();
    }

    /**
     * Creates and returns a stylesheet builder configured with some useful default styles. The
     * caller can augment the sheet with additional styles and call {@code create}.
     */
    public static Stylesheet.Builder newSheetBuilder () {
        int bgColor = 0xFFCCCCCC, ulColor = 0xFFEEEEEE, brColor = 0xFFAAAAAA;
        Background butBg = Background.roundRect(bgColor, 5, ulColor, 2).inset(5, 6, 2, 6);
        Background butSelBg = Background.roundRect(bgColor, 5, brColor, 2).inset(6, 5, 1, 7);
        return Stylesheet.builder().
            add(Button.class,
                Style.BACKGROUND.is(butBg)).
            add(Button.class, Style.Mode.SELECTED,
                Style.BACKGROUND.is(butSelBg)).
            add(ToggleButton.class,
                Style.BACKGROUND.is(butBg)).
            add(ToggleButton.class, Style.Mode.SELECTED,
                Style.BACKGROUND.is(butSelBg)).
            add(CheckBox.class,
                Style.BACKGROUND.is(Background.roundRect(bgColor, 5, ulColor, 2).inset(3, 2, 0, 3))).
            add(CheckBox.class, Style.Mode.SELECTED,
                Style.BACKGROUND.is(Background.roundRect(bgColor, 5, brColor, 2).inset(3, 2, 0, 3))).
            // flip ul and br to make Field appear recessed
            add(Field.class,
                Style.BACKGROUND.is(Background.beveled(0xFFFFFFFF, brColor, ulColor).inset(5)),
                Style.HALIGN.left).
            add(Field.class, Style.Mode.DISABLED,
                Style.BACKGROUND.is(Background.beveled(0xFFCCCCCC, brColor, ulColor).inset(5))).
            add(Menu.class,
                Style.BACKGROUND.is(Background.bordered(0xFFFFFFFF,  0x00000000, 1).inset(6))).
            add(MenuItem.class,
                Style.BACKGROUND.is(Background.solid(0xFFFFFFFF)),
                Style.HALIGN.left).
            add(MenuItem.class, Style.Mode.SELECTED,
                Style.BACKGROUND.is(Background.solid(0xFF000000)),
                Style.COLOR.is(0xFFFFFFFF)).
            add(Tabs.class,
                Tabs.HIGHLIGHTER.is(Tabs.textColorHighlighter(0xFF000000, 0xFFFFFFFF)));
    }
}
