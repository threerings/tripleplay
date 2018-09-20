//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import playn.core.*;
import pythagoras.f.IDimension;
import react.*;

import tripleplay.game.ScreenStack;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.SimpleStyles;
import tripleplay.ui.Style;
import tripleplay.ui.Stylesheet;
import tripleplay.ui.layout.AxisLayout;

/**
 * The base class for all demo screens.
 */
public abstract class DemoScreen extends ScreenStack.UIScreen
{
    public static final Font TITLE_FONT = new Font("Helvetica", 24);

    public Button back;

    public DemoScreen () {
        super(TripleDemo.game);
    }

    @Override public void wasAdded () {
        super.wasAdded();
        final Root root = iface.createRoot(
            AxisLayout.vertical().gap(0).offStretch(), stylesheet(), layer);
        root.addStyles(Style.BACKGROUND.is(background()), Style.VALIGN.top);
        sizeValue().connectNotify(new Slot<IDimension>() {
            public void onEmit (IDimension size) {
                root.setSize(size);
            }
        });
        Background bg = Background.solid(0xFFCC99FF).inset(0, 0, 5, 0);
        root.add(new Group(AxisLayout.horizontal(), Style.HALIGN.left, Style.BACKGROUND.is(bg)).add(
            this.back = new Button("Back"),
            new Label(title()).addStyles(Style.FONT.is(TITLE_FONT), Style.HALIGN.center).
                setConstraint(AxisLayout.stretched())));
        if (subtitle() != null) root.add(new Label(subtitle()));
        Group iface = createIface(root);
        if (iface != null) root.add(iface.setConstraint(AxisLayout.stretched()));
    }

    @Override public void wasRemoved () {
        super.wasRemoved();
        iface.disposeRoots();
        layer.disposeAll();
    }

    /** The label to use on the button that displays this demo. */
    protected abstract String name ();

    /** Returns the title of this demo. */
    protected abstract String title ();

    /** Returns an explanatory subtitle for this demo, or null. */
    protected String subtitle () { return null; }

    /** Override this method and return a group that contains your main UI, or null. Note: {@code
      * root} is provided for reference, the group returned by this call will automatically be
      * added to the root group. */
    protected abstract Group createIface (Root root);

    /** Returns the stylesheet to use for this screen. */
    protected Stylesheet stylesheet () {
        return SimpleStyles.newSheet(game().plat.graphics());
    }

    /** Returns the background to use for this screen. */
    protected Background background () {
        return Background.bordered(0xFFCCCCCC, 0xFFCC99FF, 5).inset(5);
    }

    protected Assets assets () { return game().plat.assets(); }
    protected Graphics graphics () { return game().plat.graphics(); }
    protected Input input () { return game().plat.input(); }
    protected Json json () { return game().plat.json(); }
    protected Log log () { return game().plat.log(); }
}
