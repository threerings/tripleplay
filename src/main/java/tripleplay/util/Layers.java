//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.GroupLayer;
import playn.core.Layer;
import pythagoras.f.IPoint;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;

/**
 * Provides utility functions for dealing with Layers
 */
public class Layers
{
    /**
     * Transforms a point from one Layer's coordinate system to another's
     */
    public static Point transform (IPoint p, Layer from, Layer to, Point result)
    {
        Layer.Util.layerToScreen(from, p, result);
        Layer.Util.screenToLayer(to, result, result);
        return result;
    }

    /**
     * Transforms a point from one Layer's coordinate system to another's
     */
    public static Point transform (IPoint p, Layer from, Layer to)
    {
        return transform(p, from, to, new Point());
    }

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
        if (l == root) {
            // initialize bounds
            float x = l.originX();
            float y = l.originY();
            // avoid weird "-0, -0" rectangle origin
            bounds.setBounds((x != 0 ? -x : 0), (y != 0 ? -y : 0), 0, 0);
        }

        if (l instanceof Layer.HasSize) {
            Layer.HasSize lhs = (Layer.HasSize) l;
            float w = lhs.width();
            float h = lhs.height();

            // grow bounds
            if (w != 0 || h != 0) {
                bounds.add(Layer.Util.layerToParent(l, root, 0, 0));
                bounds.add(Layer.Util.layerToParent(l, root, w, h));
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
