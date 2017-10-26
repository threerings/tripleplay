//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2017, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Color;
import playn.core.Surface;
import playn.scene.Layer;
import playn.scene.Mouse;
import playn.scene.Pointer;
import playn.scene.Touch;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

/**
 * A layer that fills a region with shadow and absorbs all pointer, mouse and touch interactions
 * that land on it. This is primarily intended to be displayed below modal interfaces which must
 * prevent interaction with any interfaces over which they are displayed.
 */
public class ModalShadow extends Layer {
    private final IDimension size;
    private int color;

    public ModalShadow (IDimension size) {
        this.size = size;
        this.setColor(0x000000, 0.5f);
        this.events().connect(new Pointer.Listener() {
            public void onStart (Pointer.Interaction iact) {
                iact.capture();
            }
        });
        this.events().connect(new Mouse.Listener() {
            public void onButton (Mouse.ButtonEvent event, Mouse.Interaction iact) {
                iact.capture();
            }
        });
        this.events().connect(new Touch.Listener() {
            public void onStart (Touch.Interaction iact) {
                iact.capture();
            }
        });
    }

    public ModalShadow (float width, float height) {
        this(new Dimension(width, height));
    }

    public float width () { return size.width(); }
    public float height () { return size.height(); }

    public void setColor(int color, float alpha) {
        this.color = Color.withAlpha(color, Math.round(alpha * 255));
    }

    protected void paintImpl (Surface surf) {
        surf.setFillColor(color).fillRect(0, 0, width(), height());
    }
}
