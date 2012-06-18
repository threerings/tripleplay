//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import playn.core.Font;
import playn.core.PlayN;

import react.Function;

import tripleplay.ui.Background;
import tripleplay.ui.Constraints;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Layout;
import tripleplay.ui.Shim;
import tripleplay.ui.Slider;
import tripleplay.ui.Style;
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
        Font fixedFont = PlayN.graphics().createFont("Fixed", Font.Style.PLAIN, 16);
        Background bg = Background.solid(0xffffffff).inset(4, 14, 4, 14);

        Slider sliders[] = {null, null, null};
        Label sliderValues[] = {null, null, null};
        Group iface = new Group(AxisLayout.vertical().gap(10)).add(
            new Shim(15, 15),
            new Label("Click and drag the slider to change the value:"),
            sliders[0] = new Slider(0, -100, 100),
            sliderValues[0] = new Label("0").
                setConstraint(Constraints.minSize("-000")).
                setStyles(Style.HALIGN.right, Style.FONT.is(fixedFont)),
            new Shim(15, 15),
            new Label("This one counts by 2s:"),
            sliders[1] = new Slider(0, -50, 50).setIncrement(2),
            sliderValues[1] = new Label("0").
                setConstraint(Constraints.minSize("-00")).
                setStyles(Style.HALIGN.right, Style.FONT.is(fixedFont)),
            new Shim(15, 15),
            new Label("With a background and thumb image:"),
            sliders[2] = new Slider(0, -50, 50).
                setThumb(PlayN.assets().getImage("images/smiley.png")).
                addStyles(Style.BACKGROUND.is(bg)),
            sliderValues[2] = new Label("0").
                setConstraint(Constraints.minSize("-00")).
                setStyles(Style.HALIGN.right, Style.FONT.is(fixedFont)));

        for (int ii = 0; ii < sliders.length; ++ii) {
            sliders[ii].value.map(FORMATTER).connect(sliderValues[ii].text.slot());
        }

        return iface;
    }

    protected Function<Float,String> FORMATTER = new Function<Float,String>() {
        public String apply (Float value) {
            return String.valueOf(value.intValue());
        }
    };
}
