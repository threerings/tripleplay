//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.Connection;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.Surface;
import playn.core.canvas.CanvasSurface;
import pythagoras.f.AffineTransform;
import pythagoras.f.IPoint;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;
import pythagoras.f.Transform;

/**
 * Provides utility functions for dealing with Layers
 */
public class Layers
{
    /**
     * No-op connection, for improving nullity assumptions.
     */
    public static final Connection NOT_LISTENING = new Connection() {
        @Override public void disconnect () {}
    };

    /** Prevents parent handling for pointer events. This is useful if you have for example a
     * button inside a scrolling container and need to enable event propagation. */
    public static final Pointer.Listener NO_PROPAGATE = new Pointer.Listener() {
        @Override public void onPointerStart (Pointer.Event event) { stop(event); }
        @Override public void onPointerEnd (Pointer.Event event) { stop(event); }
        @Override public void onPointerDrag (Pointer.Event event) { stop(event); }
        @Override public void onPointerCancel (Pointer.Event event) { stop(event); }
        void stop (Pointer.Event event) { event.flags().setPropagationStopped(true); }
    };

    /**
     * Transforms a point from one Layer's coordinate system to another's.
     */
    public static Point transform (IPoint p, Layer from, Layer to, Point result) {
        Layer.Util.layerToScreen(from, p, result);
        Layer.Util.screenToLayer(to, result, result);
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
        Layer.Util.layerToScreen(layer.parent(), pos, pos);
        target.add(layer);
        Layer.Util.screenToLayer(layer.parent(), pos, pos);
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
        GroupLayer gl = PlayN.graphics().createGroupLayer();
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
        return addChild(parent, PlayN.graphics().createGroupLayer());
    }

    /**
     * Creates an immediate layer that renders a simple rectangle of the given color,
     * width and height.
     */
    public static Layer solid (final int color, final float width, final float height) {
        return PlayN.graphics().createImmediateLayer(new ImmediateLayer.Renderer() {
            public void render (Surface surf) {
                surf.setFillColor(color).fillRect(0, 0, width, height);
            }
        });
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

    /**
     * Renders the given layer to the given canvas. Group, image and immediate layers are
     * supported. Applications should not need to do this very much, but sometimes can be
     * very handy.
     * TODO: clipping
     * TODO: surfaceLayer
     */
    public static void capture (Layer layer, Canvas canvas) {
        if (!layer.visible()) return;
        canvas.save();

        concatTransform(canvas, layer.transform());
        canvas.translate(-layer.originX(), -layer.originY());

        if (layer instanceof GroupLayer) {
            GroupLayer gl = (GroupLayer)layer;
            for (int ii = 0, ll = gl.size(); ii < ll; ii++) {
                capture(gl.get(ii), canvas);
            }

        } else if (layer instanceof ImageLayer) {
            ImageLayer il = (ImageLayer)layer;
            canvas.setAlpha(il.alpha());
            canvas.drawImage(il.image(), 0, 0);
        } else if (layer instanceof ImmediateLayer) {
            ImmediateLayer il = (ImmediateLayer)layer;
            il.renderer().render(new CanvasSurface(canvas).setAlpha(il.alpha()));
        }

        canvas.restore();
    }

    /**
     * Renders the given layer to a canvas image of the given width and height and returns the
     * image.
     * @see #capture(Layer, Canvas)
     */
    public static CanvasImage capture (Layer layer, float width, float height) {
        CanvasImage image = PlayN.graphics().createImage(width, height);
        capture(layer, image.canvas());
        return image;
    }

    /**
     * Creates a connection that will disconnect multiple other connections. NOTE: for best
     * retention practices, once the resulting connection is disconnected, the given ones
     * will no longer be referenced and hence will only have their {@code disconnect} method
     * called once (via the returned object).
     */
    public static Connection join (final Connection... connections) {
        return new Connection() {
            @Override public void disconnect () {
                if (_conns == null) return;
                for (Connection conn : _conns) conn.disconnect();
                _conns = null;
            }
            protected Connection[] _conns = connections;
        };
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

    /** Utility method for capture. */
    protected static AffineTransform toAffine (Transform t) {
        if (t instanceof AffineTransform) return (AffineTransform)t;
        else return new AffineTransform(t.scaleX(), t.scaleY(), t.rotation(), t.tx(), t.ty());
    }

    /** Utility method for capture. */
    protected static void concatTransform (Canvas canvas, AffineTransform at) {
        canvas.transform(at.m00, at.m01, at.m10, at.m11, at.tx, at.ty);
    }

    /** Utility method for capture. */
    protected static void concatTransform (Canvas canvas, Transform t) {
        concatTransform(canvas, toAffine(t));
    }
}
