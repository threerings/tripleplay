//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.anim;

import pythagoras.f.Point;

import playn.core.CanvasImage;
import playn.core.Font;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.util.Clock;
import static playn.core.PlayN.*;

import tripleplay.anim.Flicker;
import tripleplay.demo.DemoScreen;
import tripleplay.ui.Group;
import tripleplay.util.StyledText;
import tripleplay.util.TextStyle;

/**
 * Demonstrates the flicker.
 */
public class FlickerDemo extends DemoScreen
{
    @Override protected String name () {
        return "Flicker";
    }
    @Override protected String title () {
        return "Flicker Demo";
    }

    @Override protected Group createIface () {
        final int width = 400;
        _group.setHitTester(new Layer.HitTester() {
            public Layer hitTest (Layer layer, Point p) {
                return (p.x < width) ? layer : null;
            }
        });
        _group.addListener(_flicker);
        layer.addAt(_group, (width()-width)/2, 0);

        // add a bunch of image layers to our root layer
        float y = 0;
        for (int ii = 0; ii < IMG_COUNT; ii++) {
            CanvasImage image = graphics().createImage(width, IMG_HEIGHT);
            StringBuffer text = new StringBuffer();
            for (int tt = 0; tt < 25; tt++) text.append(ii+1);
            StyledText.span(text.toString(), TEXT).render(image.canvas(), 0, 0);
            ImageLayer layer = graphics().createImageLayer(image);
            _group.addAt(layer, 0, y);
            y += layer.scaledHeight();
        }

        return null;
    }

    @Override public void paint (Clock clock) {
        super.paint(clock);
        _flicker.paint(clock);
        _group.setTy(_flicker.position);
    }

    protected GroupLayer _group = graphics().createGroupLayer();
    protected Flicker _flicker = new Flicker(0, height()-IMG_HEIGHT*IMG_COUNT, 0) {
        @Override protected float friction () { return 0.001f; }
    };

    protected static final float IMG_HEIGHT = 100;
    protected static final int IMG_COUNT = 20;
    protected static final TextStyle TEXT = new TextStyle().
        withFont(graphics().createFont("Helvetiva", Font.Style.PLAIN, 72));
}
