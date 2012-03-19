//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Font;
import playn.core.PlayN;

import react.Function;

import tripleplay.ui.layout.AxisLayout;

public class SliderPage implements WidgetDemo.Page
{
    public String name () {
        return "Slider";
    }

    public Group createInterface () {
        Font fixedFont = PlayN.graphics().createFont("Fixed", Font.Style.PLAIN, 16);

        Slider slider;
        Label sliderValue;
        Group iface = new Group(AxisLayout.vertical().gap(15)).add(
            slider = new Slider(0, -100, 100),
            sliderValue = new Label("0").
                setConstraint(Constraints.minSize("-000")).
                setStyles(Style.HALIGN.right, Style.FONT.is(fixedFont)));
        slider.value.map(FORMATTER).connect(sliderValue.text.slot());
        return iface;
    }

    protected Function<Float,String> FORMATTER = new Function<Float,String>() {
        public String apply (Float value) {
            return String.valueOf(value.intValue());
        }
    };
}
