//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Font;
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
        Image smiley = PlayN.assets().getImage("smiley.png");
        Styles wrapped = Styles.make(Style.TEXT_WRAP.is(true));
        Styles greenBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFF99CC66).inset(5)));
        Styles bigLabel = Styles.make(
            Style.FONT.is(PlayN.graphics().createFont("Times New Roman", Font.Style.PLAIN, 24)),
            Style.HALIGN.center);
        Group iface = new Group(AxisLayout.vertical().offStretch()).add(
            // display some wrapped text
            new Shim(15, 15),
            new Label("Wrapped text").addStyles(Style.HALIGN.center),
            new Group(AxisLayout.horizontal(), greenBg.add(Style.VALIGN.top)).add(
                AxisLayout.stretch(new Label(TEXT1, smiley).
                                   addStyles(wrapped.add(Style.ICON_GAP.is(5)))),
                AxisLayout.stretch(new Label(TEXT2).addStyles(wrapped)),
                AxisLayout.stretch(new Label(TEXT3).addStyles(wrapped))),
            new Shim(15, 15),
            new Label("Styled text").addStyles(Style.HALIGN.center),
            new Group(AxisLayout.horizontal()).add(
                AxisLayout.stretch(new Label("Plain").addStyles(bigLabel)),
                AxisLayout.stretch(new Label("Pixel Outline").addStyles(
                                       bigLabel.add(Style.TEXT_EFFECT.pixelOutline))),
                AxisLayout.stretch(new Label("Vector Outline").addStyles(
                                       bigLabel.add(Style.TEXT_EFFECT.vectorOutline,
                                                    Style.OUTLINE_WIDTH.is(2f)))),
                AxisLayout.stretch(new Label("Shadow").addStyles(
                                       bigLabel.add(Style.TEXT_EFFECT.shadow)))));

        return iface;
    }

    protected static final String TEXT1 = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";
    protected static final String TEXT2 = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.";
    protected static final String TEXT3 = "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness.";
}
