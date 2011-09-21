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
import react.UnitSlot;

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

    public interface Page {
        String name ();
        Group createInterface ();
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
        final Root root = _iface.createRoot(AxisLayout.vertical().offStretch(), rootSheet);
        root.setSize(PlayN.graphics().width(), PlayN.graphics().height());
        root.addStyles(Styles.make(Style.BACKGROUND.is(Background.solid(0xFF99CCFF, 5)),
                                   Style.VALIGN.is(Style.VAlign.TOP)));
        PlayN.graphics().rootLayer().add(root.layer);

        Styles alignLeft = Styles.make(Style.HALIGN.is(Style.HAlign.LEFT));
        Group buttons = new Group(AxisLayout.horizontal(), alignLeft);
        root.add(buttons);

        Page[] pages = { new MiscPage() };
        for (final Page page : pages) {
            Button tab = new Button().setText(page.name());
            buttons.add(tab);
            tab.clicked().connect(new UnitSlot() {
                public void onEmit () {
                    if (root.childCount() > 1) root.removeAt(1);
                    root.add(page.createInterface());
                }
            });
        }
        root.add(pages[0].createInterface());
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
}
