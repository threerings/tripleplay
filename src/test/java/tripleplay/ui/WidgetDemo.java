//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import forplay.core.ForPlay;
import forplay.core.Game;
import forplay.java.JavaPlatform;

import react.Signals;

/**
 * A test app for demoing the UI widgets.
 */
public class WidgetDemo implements Game
{
    public static void main (String[] args) {
        JavaPlatform platform = JavaPlatform.register();
        platform.assetManager().setPathPrefix("src/main/resources");
        ForPlay.run(new WidgetDemo());
    }

    @Override // from interface Game
    public void init () {
        _iface = new Interface(null);
        _iface.activate();

        // define our root stylesheet
        Styles buttonStyles = Styles.none().
            add(Style.BACKGROUND.is(Background.solid(0xFFFFFFFF, 5))).
            addDown(Style.BACKGROUND.is(Background.solid(0xFFCCCCCC, 6, 4, 4, 6)));
        Stylesheet rootSheet = Stylesheet.builder().
            add(Button.class, buttonStyles).
            create();

        // create our demo interface
        Root root = _iface.createRoot(AxisLayout.vertical(), rootSheet);
        root.setSize(ForPlay.graphics().width(), ForPlay.graphics().height());
        root.addStyles(Styles.make(Style.BACKGROUND.is(Background.solid(0xFF99CCFF, 5))));
        ForPlay.graphics().rootLayer().add(root.layer);

        Group bits;
        Button toggle;
        Label label2;
        Styles greenBg = Styles.make(Style.BACKGROUND.is(Background.solid(0xFFCCFF99, 5)));
        root.add(new Group(AxisLayout.horizontal().alignTop()).add(
                     new Label().setText(TEXT1), AxisLayout.stretched()).add(
                     new Label().setText(TEXT2), AxisLayout.stretched()).add(
                     new Label().setText(TEXT3), AxisLayout.stretched()),
                 new Group(AxisLayout.horizontal().alignTop(), greenBg).add(
                     new Group(AxisLayout.vertical()).add(
                         new Label().setText("Toggle viz:"),
                         toggle = new Button().setText("Toggle")),
                     new Group(AxisLayout.vertical()).add(
                         new Label().setText("Label 1"),
                         label2 = new Label().setText("Label 2"),
                         new Label().setText("Label 3"))));
        Signals.toggler(toggle.click, true).connect(label2.visibleSlot());
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

    protected Interface _iface;

    protected static final String TEXT1 = "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";
    protected static final String TEXT2 = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.";
    protected static final String TEXT3 = "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness.";
}
