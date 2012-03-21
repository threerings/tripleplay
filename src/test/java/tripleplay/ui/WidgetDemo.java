//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Game;
import playn.core.PlayN;
import playn.java.JavaPlatform;

import react.UnitSlot;

import tripleplay.ui.layout.AxisLayout;

/**
 * A test app for demoing the UI widgets.
 */
public class WidgetDemo implements Game
{
    public static void main (String[] args) {
        _mainArgs = args;
        JavaPlatform.register();
        PlayN.run(new WidgetDemo());
    }

    public interface Page {
        String name ();
        Group createInterface ();
    }

    @Override // from interface Game
    public void init () {
        _iface = new Interface();
        PlayN.keyboard().setListener(_iface.klistener);

        // define our root stylesheet
        Stylesheet rootSheet = SimpleStyles.newSheetBuilder().
            add(Label.class, Style.HALIGN.left, Style.VALIGN.top).
            create();

        // create our demo interface
        final Root root = _iface.createRoot(AxisLayout.vertical().offStretch(), rootSheet,
                                            PlayN.graphics().rootLayer()).
            setSize(PlayN.graphics().width(), PlayN.graphics().height()).
            addStyles(Style.BACKGROUND.is(Background.solid(0xFF99CCFF, 5)), Style.VALIGN.top);

        Group buttons = new Group(AxisLayout.horizontal(), Style.HALIGN.left);
        root.add(buttons);

        Page[] pages = { new MiscPage(), new LayoutPage(), new LabelPage(), new SliderPage(),
            new BackgroundPage() };
        for (final Page page : pages) {
            Button tab = new Button(page.name());
            buttons.add(tab);
            tab.clicked().connect(new UnitSlot() {
                public void onEmit () {
                    if (root.childCount() > 1) root.destroyAt(1);
                    root.add(page.createInterface());
                }
            });
        }

        int selidx = (_mainArgs.length > 0) ? Integer.parseInt(_mainArgs[0]) : 0;
        root.add(pages[selidx].createInterface());
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

    protected static String[] _mainArgs;
}
