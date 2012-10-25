//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
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
    public static Point transform (IPoint p, Layer from, Layer to, Point result) {
        Layer.Util.layerToScreen(from, p, result);
        Layer.Util.screenToLayer(to, result, result);
        return result;
    }

    /**
     * Transforms a point from one Layer's coordinate system to another's
     */
    public static Point transform (IPoint p, Layer from, Layer to) {
        return transform(p, from, to, new Point());
    }

    /**
     * Removes {@code layer} from its current parent and adds it to {@code target}, modifying its
     * transform in the process so that it stays in the same position on the screen.
     */
    public static void reparent (Layer layer, GroupLayer target) {
        Point pos = new Point(layer.transform().tx(), layer.transform().ty());
        Layer.Util.layerToScreen(layer.parent(), pos, pos);
        target.add(layer);
        Layer.Util.screenToLayer(layer.parent(), pos, pos);
        layer.setTranslation(pos.x, pos.y);
    }

    /**
     * Computes the total bounds of the layer hierarchy rooted at <code>root</code>.
     * The returned Rectangle will be in <code>root</code>'s coordinate system.
     */
    public static Rectangle totalBounds (Layer root) {
        // account for root's origin (we use 0-x rather than just -x to avoid weird -0 values)
        Rectangle r = new Rectangle(0-root.originX(), 0-root.originY(), 0, 0);
        addBounds(root, root, r, new Point());
        return r;
    }

    /** Helper function for {@link #totalBounds}. */
    protected static void addBounds (Layer root, Layer l, Rectangle bounds, Point scratch) {
        if (l instanceof Layer.HasSize) {
            Layer.HasSize lhs = (Layer.HasSize) l;
            float w = lhs.width(), h = lhs.height();
            if (w != 0 || h != 0) {
                // grow bounds
                bounds.add(Layer.Util.layerToParent(l, root, scratch.set(0, 0), scratch));
                bounds.add(Layer.Util.layerToParent(l, root, scratch.set(w, h), scratch));
            }
        }

        if (l instanceof GroupLayer) {
            GroupLayer group = (GroupLayer) l;
            for (int ii = 0, ll = group.size(); ii < ll; ++ii) {
                addBounds(root, group.get(ii), bounds, scratch);
            }
        }
    }
}
