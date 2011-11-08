//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.GroupLayer;
import playn.core.Layer;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;
import pythagoras.f.Transform;

/**
 * Provides utility functions for dealing with Layers
 */
public class Layers
{
    /**
     * Computes the total bounds of the layer hierarchy rooted at <code>root</code>.
     * The returned Rectangle will be in <code>root</code>'s coordinate system.
     */
    public static Rectangle totalBounds (Layer root)
    {
        Rectangle r = new Rectangle();
        addBounds(root, root, r);
        return r;
    }

    /**
     * Helper function for totalBounds()
     */
    protected static void addBounds (Layer root, Layer l, Rectangle bounds)
    {
        float w = 0, h = 0;
        if (l instanceof Layer.HasSize) {
            Layer.HasSize lhs = (Layer.HasSize) l;
            w = lhs.width();
            h = lhs.height();
        }

        if (l == root) {
            // initialize bounds
            bounds.setBounds(-l.originX(), -l.originY(), w, h);
        } else {
            // grow bounds
            Transform t = l.transform();
            Point loc = Layer.Util.layerToParent(l, root, t.tx(), t.ty());
            bounds.add(loc);
            if (w != 0 || h != 0) {
                bounds.add(Layer.Util.layerToParent(l, root, t.tx() + w, t.ty() + h));
            }
        }

        if (l instanceof GroupLayer) {
            GroupLayer group = (GroupLayer) l;
            for (int ii = 0, ll = group.size(); ii < ll; ++ii) {
                addBounds(root, group.get(ii), bounds);
            }
        }
    }
}
