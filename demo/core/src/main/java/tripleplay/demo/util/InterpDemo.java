//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.util;

import react.Slot;
import react.UnitSlot;

import playn.core.*;
import playn.scene.ImageLayer;
import playn.scene.Layer;

import tripleplay.demo.DemoScreen;
import tripleplay.ui.*;
import tripleplay.ui.layout.TableLayout;
import tripleplay.util.Interpolator;

public class InterpDemo extends DemoScreen
{
    public InterpDemo () {
        paint.connect(new Slot<Clock>() {
            public void onEmit (Clock clock) {
                for (Driver driver : _drivers) if (driver.elapsed >= 0) driver.paint(clock);
            }
        });
    }

    @Override protected String name () {
        return "Interps";
    }

    @Override protected String title () {
        return "Util: Interpolators";
    }

    @Override protected Group createIface (Root root) {
        Group grid = new Group(new TableLayout(TableLayout.COL.stretch().fixed(),
                                               TableLayout.COL).gaps(10, 10));
        Canvas square = graphics().createCanvas(20, 20);
        square.setFillColor(0xFFFF0000).fillRect(0, 0, 20, 20);
        Texture sqtex = square.toTexture();

        for (int ii = 0; ii < INTERPS.length; ii++) {
            Layer knob = new ImageLayer(sqtex);
            Shim tray = new Shim(300, 20);
            tray.addStyles(Style.BACKGROUND.is(Background.solid(0xFF666666)));
            tray.layer.add(knob);
            final Driver driver = new Driver(INTERPS[ii], knob);
            _drivers[ii] = driver;
            grid.add(new Button(INTERPS[ii].toString()).onClick(new UnitSlot() {
                public void onEmit () { driver.elapsed = 0; }
            }));
            grid.add(tray);
        }
        grid.add(new Button("ALL").onClick(new UnitSlot() { public void onEmit () {
            for (Driver driver : _drivers) driver.elapsed = 0;
        }}));
        return grid.addStyles(Style.BACKGROUND.is(Background.blank().inset(15)));
    }

    protected void demoInterp (Interpolator interp, Layer knob) {
        // TODO
    }

    protected class Driver {
        public final Interpolator interp;
        public final Layer knob;
        public float elapsed = -1;

        public Driver (Interpolator interp, Layer knob) {
            this.interp = interp;
            this.knob = knob;
        }

        public void paint (Clock clock) {
            if (elapsed > 2500) { // spend 500ms at max value
                knob.setTx(0);
                elapsed = -1;
            } else {
                elapsed += clock.dt;
                knob.setTx(interp.applyClamp(0, 300, elapsed, 2000));
            }
        }
    }

    protected final Interpolator[] INTERPS = {
        Interpolator.LINEAR,
        Interpolator.EASE_IN,
        Interpolator.EASE_OUT,
        Interpolator.EASE_INOUT,
        Interpolator.EASE_IN_BACK,
        Interpolator.EASE_OUT_BACK,
        Interpolator.BOUNCE_OUT,
        Interpolator.EASE_OUT_ELASTIC
    };
    protected final Driver[] _drivers = new Driver[INTERPS.length];
}
