//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Game;
import playn.core.Image;
import playn.core.PlayN;
import playn.java.JavaPlatform;

import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

import react.SignalView;
import react.Signals;

import static react.Functions.TO_STRING;

/**
 * A test app for demoing the UI widgets.
 */
public class WidgetDemo implements Game
{
    public static void main (String[] args) {
        JavaPlatform platform = JavaPlatform.register();
        platform.assetManager().setPathPrefix("src/test/resources");
        PlayN.run(new WidgetDemo());
    }

    @Override // from interface Game
    public void init () {
        _iface = new Interface(null);
        PlayN.pointer().setListener(_iface.plistener);

        // define our root stylesheet
        Styles buttonStyles = Styles.none().
            add(Style.BACKGROUND.is(Background.solid(0xFFFFFFFF, 5))).
            addSelected(Style.BACKGROUND.is(Background.solid(0xFFCCCCCC, 6, 4, 4, 6)));
        Styles labelStyles = Styles.none().
            add(Style.HALIGN.is(Style.HAlign.LEFT), Style.VALIGN.is(Style.VAlign.TOP));
        Stylesheet rootSheet = Stylesheet.builder().
            add(Button.class, buttonStyles).
            add(Label.class, labelStyles).
            create();

        // create our demo interface
        Root root = _iface.createRoot(AxisLayout.vertical().gap(15), rootSheet);
        root.setSize(PlayN.graphics().width(), PlayN.graphics().height());
        root.addStyles(Styles.make(Style.BACKGROUND.is(Background.solid(0xFF99CCFF, 5))));
        PlayN.graphics().rootLayer().add(root.layer);

        Image smiley = PlayN.assetManager().getImage("images/smiley.png");
        Image squares = PlayN.assetManager().getImage("images/squares.png");
        Styles wrapped = Styles.make(Style.TEXT_WRAP.is(true));

        Button toggle;
        Slider slider;
        Label label2, iconRight, sliderValue;

        Styles alignTop = Styles.make(Style.VALIGN.is(Style.VAlign.TOP));
        Styles greenBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFCCFF99, 5)));
        root.add(new Group(AxisLayout.horizontal(), alignTop).add(
                     new Label(wrapped).setConstraint(AxisLayout.stretched()).setText(TEXT1),
                     new Label(wrapped).setConstraint(AxisLayout.stretched()).setText(TEXT2),
                     new Label(wrapped).setConstraint(AxisLayout.stretched()).setText(TEXT3)),
                 new Group(AxisLayout.horizontal().gap(15), greenBg.merge(alignTop)).add(
                     new Group(AxisLayout.vertical()).add(
                         new Label().setText("Toggle viz:"),
                         toggle = new Button().setText("Toggle"),
                         new Button().setText("Disabled").setEnabled(false)),
                     new Group(AxisLayout.vertical()).add(
                         new Label().setText("Label 1"),
                         label2 = new Label().setText("Label 2"),
                         new Label().setIcon(smiley).setText("Label 3")),
                     new Group(new TableLayout(2).gaps(10, 10)).add(
                         new Label(Styles.make(Style.ICON_POS.is(Style.Pos.LEFT))).
                             setText("Left").setIcon(squares, getIBounds(0)),
                         iconRight = new Label(Styles.make(Style.ICON_POS.is(Style.Pos.RIGHT))).
                             setText("Right").setIcon(squares, getIBounds(1)),
                         new Label(Styles.make(Style.ICON_POS.is(Style.Pos.ABOVE),
                                               Style.HALIGN.is(Style.HAlign.CENTER))).
                             setText("Above").setIcon(squares, getIBounds(2)),
                         new Label(Styles.make(Style.ICON_POS.is(Style.Pos.BELOW),
                                               Style.HALIGN.is(Style.HAlign.CENTER))).
                             setText("Below").setIcon(squares, getIBounds(3)))),
                 new Group(AxisLayout.vertical()).add(
                     slider = new Slider(0, -1, 1),
                     sliderValue = new Label("0")));
        SignalView<Boolean> toggler = Signals.toggler(toggle.clicked(), true);
        toggler.connect(label2.visibleSlot());
        toggler.connect(iconRight.visibleSlot());
        slider.value.map(TO_STRING).connect(sliderValue.textSlot());
    }

    @Override // from interface Game
    public void update (float delta) {
        _iface.update(delta);
    }

    @Override // from interface Game
    public void paint (float alpha) {
        _iface.paint(alpha);
    }

    @Override // from interface Game
    public int updateRate () {
        return 30;
    }

    protected IRectangle getIBounds (int index) {
        final float iwidth = 16, iheight = 16;
        return new Rectangle(index*iwidth, 0, iwidth, iheight);
    }

    protected Interface _iface;

    protected static final String TEXT1 = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";
    protected static final String TEXT2 = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.";
    protected static final String TEXT3 = "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness.";
}
