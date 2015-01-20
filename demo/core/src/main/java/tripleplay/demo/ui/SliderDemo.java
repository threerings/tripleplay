//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import playn.core.Font;

import react.Function;

import tripleplay.ui.Background;
import tripleplay.ui.Behavior;
import tripleplay.ui.Constraints;
import tripleplay.ui.Group;
import tripleplay.ui.Icons;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
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

    @Override protected Group createIface (Root root) {
        Group iface = new Group(AxisLayout.vertical().gap(10)).add(
            new Shim(15, 15),
            new Label("Click and drag the slider to change the value:"),
            sliderAndLabel(new Slider(0, -100, 100), "-000"),
            new Shim(15, 15),
            new Label("This one counts by 2s:"),
            sliderAndLabel(new Slider(0, -50, 50).setIncrement(2).addStyles(
                Behavior.Track.HOVER_LIMIT.is(35f)), "-00"),
            new Shim(15, 15),
            new Label("With a background, custom bar and thumb image:"),
            sliderAndLabel(
                new Slider(0, -50, 50).addStyles(
                    Style.BACKGROUND.is(Background.roundRect(graphics(), 0xFFFFFFFF, 16).inset(4)),
                    Slider.THUMB_IMAGE.is(Icons.image(assets().getImage("images/smiley.png"))),
                    Slider.BAR_HEIGHT.is(18f),
                    Slider.BAR_BACKGROUND.is(
                        Background.roundRect(graphics(), 0xFFFF0000, 9))), "-00"));

        return iface;
    }

    protected Group sliderAndLabel (Slider slider, String minText) {
        ValueLabel label = new ValueLabel(slider.value.map(FORMATTER)).
            setStyles(Style.HALIGN.right, Style.FONT.is(FIXED)).
            setConstraint(Constraints.minSize(graphics(), minText));
        return new Group(AxisLayout.horizontal()).add(slider, label);
    }

    protected Function<Float,String> FORMATTER = new Function<Float,String>() {
        public String apply (Float value) {
            return String.valueOf(value.intValue());
        }
    };

    protected static Font FIXED = new Font("Fixed", 16);
}
