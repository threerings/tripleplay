//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import pythagoras.f.IPoint;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;
import playn.core.Surface;
import playn.scene.GroupLayer;
import playn.scene.Layer;
import playn.scene.LayerUtil;
import playn.scene.Pointer;

/**
 * Provides utility functions for dealing with Layers
 */
public class Layers
{
    /** Prevents parent handling for pointer events. This is useful if you have for example a
     * button inside a scrolling container and need to enable event propagation. */
    public static final Pointer.Listener NO_PROPAGATE = new Pointer.Listener() {
        @Override public void onStart (Pointer.Interaction iact) { stop(iact); }
        @Override public void onEnd (Pointer.Interaction iact) { stop(iact); }
        @Override public void onDrag (Pointer.Interaction iact) { stop(iact); }
        @Override public void onCancel (Pointer.Interaction iact) { stop(iact); }
        void stop (Pointer.Interaction iact) {
            // TODO: event.flags().setPropagationStopped(true);
        }
    };

    /**
     * Transforms a point from one Layer's coordinate system to another's.
     */
    public static Point transform (IPoint p, Layer from, Layer to, Point result) {
        LayerUtil.layerToScreen(from, p, result);
        LayerUtil.screenToLayer(to, result, result);
        return result;
    }

    /**
     * Transforms a point from one Layer's coordinate system to another's.
     */
    public static Point transform (IPoint p, Layer from, Layer to) {
        return transform(p, from, to, new Point());
    }

    /**
     * Removes {@code layer} from its current parent and adds it to {@code target}, modifying its
     * transform in the process so that it stays in the same position on the screen.
     */
    public static void reparent (Layer layer, GroupLayer target) {
        Point pos = new Point(layer.tx(), layer.ty());
        LayerUtil.layerToScreen(layer.parent(), pos, pos);
        target.add(layer);
        LayerUtil.screenToLayer(layer.parent(), pos, pos);
        layer.setTranslation(pos.x, pos.y);
    }

    /**
     * Whether a GroupLayer hierarchy contains another layer somewhere in its depths.
     */
    public static boolean contains (GroupLayer group, Layer layer) {
        while (layer != null) {
            layer = layer.parent();
            if (layer == group) return true;
        }
        return false;
    }

    /**
     * Creates a new group with the given children.
     */
    public static GroupLayer group (Layer... children) {
        GroupLayer gl = new GroupLayer();
        for (Layer l : children) gl.add(l);
        return gl;
    }

    /**
     * Adds a child layer to a group and returns the child.
     */
    public static <T extends Layer> T addChild (GroupLayer parent, T child) {
        parent.add(child);
        return child;
    }

    /**
     * Adds a child group to a parent group and returns the child.
     */
    public static GroupLayer addNewGroup (GroupLayer parent) {
        return addChild(parent, new GroupLayer());
    }

    /**
     * Creates a layer that renders a simple rectangle of the given color, width and height.
     */
    public static Layer solid (final int color, final float width, final float height) {
        return new Layer() {
            @Override public float width () { return width; }
            @Override public float height () { return height; }
            @Override protected void paintImpl (Surface surf) {
                surf.setFillColor(color).fillRect(0, 0, width, height);
            }
        };
    }

    /**
     * Computes the total bounds of the layer hierarchy rooted at {@code root}.
     * The returned Rectangle will be in {@code root}'s coordinate system.
     */
    public static Rectangle totalBounds (Layer root) {
        // account for root's origin
        Rectangle r = new Rectangle(root.originX(), root.originY(), 0, 0);
        addBounds(root, root, r, new Point());
        return r;
    }

    /** Helper function for {@link #totalBounds}. */
    protected static void addBounds (Layer root, Layer l, Rectangle bounds, Point scratch) {
        float w = l.width(), h = l.height();
        if (w != 0 || h != 0) {
            // grow bounds
            bounds.add(LayerUtil.layerToParent(l, root, scratch.set(0, 0), scratch));
            bounds.add(LayerUtil.layerToParent(l, root, scratch.set(w, h), scratch));
        }

        if (l instanceof GroupLayer) {
            GroupLayer group = (GroupLayer) l;
            for (int ii = 0, ll = group.children(); ii < ll; ++ii) {
                addBounds(root, group.childAt(ii), bounds, scratch);
            }
        }
    }
}
