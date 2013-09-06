//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import playn.core.Font;
import playn.core.PlayN;

import react.Function;

import tripleplay.ui.Background;
import tripleplay.ui.Constraints;
import tripleplay.ui.Group;
import tripleplay.ui.Icons;
import tripleplay.ui.Label;
import tripleplay.ui.Shim;
import tripleplay.ui.Slider;
import tripleplay.ui.Style;
import tripleplay.ui.ValueLabel;
import tripleplay.ui.layout.AxisLayout;

import tripleplay.demo.DemoScreen;

public class SliderDemo extends DemoScreen
{
    @Override protected String name () {
        return "Sliders";
    }
    @Override protected String title () {
        return "UI: Sliders";
    }

    @Override protected Group createIface () {
        Group iface = new Group(AxisLayout.vertical().gap(10)).add(
            new Shim(15, 15),
            new Label("Click and drag the slider to change the value:"),
            sliderAndLabel(new Slider(0, -100, 100), "-000"),
            new Shim(15, 15),
            new Label("This one counts by 2s:"),
            sliderAndLabel(new Slider(0, -50, 50).setIncrement(2), "-00"),
            new Shim(15, 15),
            new Label("With a background, custom bar and thumb image:"),
            sliderAndLabel(
                new Slider(0, -50, 50).addStyles(
                    Style.BACKGROUND.is(Background.roundRect(0xFFFFFFFF, 16).inset(4)),
                    Slider.THUMB_IMAGE.is(Icons.image(PlayN.assets().getImage("images/smiley.png"))),
                    Slider.BAR_HEIGHT.is(18f),
                    Slider.BAR_BACKGROUND.is(Background.roundRect(0xFFFF0000, 9))), "-00"));

        return iface;
    }

    protected Group sliderAndLabel (Slider slider, String minText) {
        ValueLabel label = new ValueLabel(slider.value.map(FORMATTER)).
            setStyles(Style.HALIGN.right, Style.FONT.is(FIXED)).
            setConstraint(Constraints.minSize(minText));
        return new Group(AxisLayout.horizontal()).add(slider, label);
    }

    protected Function<Float,String> FORMATTER = new Function<Float,String>() {
        public String apply (Float value) {
            return String.valueOf(value.intValue());
        }
    };

    protected static Font FIXED = PlayN.graphics().createFont("Fixed", Font.Style.PLAIN, 16);
}
