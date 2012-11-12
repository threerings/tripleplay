//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.bgs;

import playn.core.GroupLayer;
import playn.core.Layer;
import playn.core.PlayN;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import tripleplay.ui.Background;

/**
 * A background consisting of multiple other backgrounds. Note: callers should not inset this
 * background since it derives all of its insets from the contained backgrounds and relies on the
 * values during instantiation.
 */
public class CompositeBackground extends Background
{
    /**
     * Creates a new composite background with the given constituents. The first background
     * is the outermost, the 2nd one is inside that and so on. The insets of this background
     * are set to the sum of the insets of the constituents.
     */
    public CompositeBackground (Background... constituents) {
        _constituents = constituents;
        for (Background bg : constituents) {
            top += bg.top;
            right += bg.right;
            bottom += bg.bottom;
            left += bg.left;
        }
    }

    /**
     * Reverses the usual depth of the constituent backgrounds' layers. Normally the outermost
     * background's layer is lowest (rendered first). Use this method to render the innermost
     * background's layer first instead.
     */
    public CompositeBackground reverseDepth () {
        _reverseDepth = true;
        return this;
    }

    @Override protected Instance instantiate (final IDimension size) {
        // we use one layer, and add the constituents to that
        GroupLayer layer = PlayN.graphics().createGroupLayer();
        final Instance[] instances = new Instance[_constituents.length];

        float top = 0, left = 0, width = size.width(), height = size.height();
        for (int ii = 0, ll = _constituents.length; ii < ll; ii++) {
            Background bg = _constituents[ii];

            // create and save off the instance so we can destroy it later
            instances[ii] = instantiate(bg, new Dimension(width, height));

            // add to our composite layer and translate the layers added
            instances[ii].addTo(layer, left, top, 0);

            // adjust the bounds
            top += bg.top;
            left += bg.left;
            width -= bg.width();
            height -= bg.height();
        }

        if (_reverseDepth) {
            // simple reversal, if optimization is needed it would be better to simply
            // instantiate the backgrounds in reverse order above
            Layer[] temp = new Layer[layer.size()];
            for (int ii = 0, nn = layer.size(); ii < nn; ii++) {
                temp[ii] = layer.get(ii);
            }
            float depth = 0;
            for (Layer l : temp) {
                l.setDepth(depth);
                depth -= 1;
            }
        }

        return new LayerInstance(size, layer) {
            @Override public void destroy () {
                for (Instance i : instances) {
                    i.destroy();
                }
                super.destroy();
            }
        };
    }

    protected final Background[] _constituents;
    protected boolean _reverseDepth;
}
