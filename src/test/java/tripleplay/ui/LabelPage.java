//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;
import playn.core.PlayN;

import tripleplay.ui.layout.AxisLayout;

/**
 * Various label tests.
 */
public class LabelPage implements WidgetDemo.Page
{
    public String name () {
        return "Label";
    }

    public Group createInterface () {
        Image smiley = PlayN.assets().getImage("images/smiley.png");
        Styles wrapped = Styles.make(Style.TEXT_WRAP.is(true));
        Styles greenBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFCCFF99, 0)));
        Styles redBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFFF0000, 0)));
        Group iface = new Group(AxisLayout.vertical().gap(15), redBg).add(
            // display some wrapped text
            new Group(AxisLayout.horizontal(), greenBg.add(Style.VALIGN.top)).add(
                new Label(MiscPage.TEXT1, wrapped.add(Style.ICON_GAP.is(5))).setIcon(smiley).
                    setConstraint(AxisLayout.stretched()),
                new Label(MiscPage.TEXT2, wrapped).setConstraint(AxisLayout.stretched()),
                new Label(MiscPage.TEXT3, wrapped).setConstraint(AxisLayout.stretched()))
            );

        return iface;
    }
}
