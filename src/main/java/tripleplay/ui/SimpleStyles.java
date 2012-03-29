//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
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
        Styles buttonStyles = Styles.none().
            add(Style.BACKGROUND.is(Background.beveled(bgColor, ulColor, brColor, 5))).
            addSelected(
                Style.BACKGROUND.is(Background.beveled(bgColor, brColor, ulColor, 6, 4, 4, 6)));
        return Stylesheet.builder().
            add(Field.class, Styles.none().
                // flip ul and br to appear recessed
                add(Style.BACKGROUND.is(Background.beveled(0xFFFFFFFF, brColor, ulColor, 5))).
                addDisabled(
                    Style.BACKGROUND.is(Background.beveled(0xFFCCCCCC, brColor, ulColor, 5)))).
            add(Button.class, buttonStyles).
            add(ToggleButton.class, buttonStyles);
    }
}
