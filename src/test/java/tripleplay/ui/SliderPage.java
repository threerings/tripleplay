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

        Slider slider, slider2;
        Label sliderValue, slider2Value;
        Group iface = new Group(AxisLayout.vertical().gap(15)).add(
            new Shim(15, 15),
            new Label("Click and drag the slider to change the value:"),
            slider = new Slider(0, -100, 100),
            sliderValue = new Label("0").
                setConstraint(Constraints.minSize("-000")).
                setStyles(Style.HALIGN.right, Style.FONT.is(fixedFont)),
            new Shim(15, 15),
            new Label("This one counts by 2s:"),
            slider2 = new Slider(0, -50, 50).setIncrement(2),
            slider2Value = new Label("0").
                setConstraint(Constraints.minSize("-00")).
                setStyles(Style.HALIGN.right, Style.FONT.is(fixedFont)));
        slider.value.map(FORMATTER).connect(sliderValue.text.slot());
        slider2.value.map(FORMATTER).connect(slider2Value.text.slot());
        return iface;
    }

    protected Function<Float,String> FORMATTER = new Function<Float,String>() {
        public String apply (Float value) {
            return String.valueOf(value.intValue());
        }
    };
}
