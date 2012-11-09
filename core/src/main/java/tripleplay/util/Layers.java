//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Canvas;
import playn.core.CanvasImage;
import playn.core.CanvasSurface;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.ImmediateLayer;
import playn.core.Layer;
import playn.core.PlayN;
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

    /**
     * Renders the given layer to the given canvas. Group, image and immediate layers are
     * supported. Applications should not need to do this very much, but sometimes can be
     * very handy.
     * TODO: clipping
     * TODO: surfaceLayer
     */
    public static void capture (Layer layer, Canvas canvas) {
        capture(layer, canvas, new AffineTransform());
    }

    /**
     * Renders the given layer to a canvas image of the given width and height and returns the
     * image.
     * @see #capture(Layer, Canvas)
     */
    public static CanvasImage capture (Layer layer, float width, float height) {
        CanvasImage image = PlayN.graphics().createImage(width, height);
        capture(layer, image.canvas(), new AffineTransform());
        return image;
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
        return new AffineTransform(t.scaleX(), t.scaleY(), t.rotation(), t.tx(), t.ty());
    }

    /** Utility method for capture. */
    protected static void setTransform (Canvas canvas, AffineTransform at) {
        canvas.setTransform(at.m00, at.m01, at.m10, at.m11, at.tx, at.ty);
    }

    /** Utility method for capture. */
    protected static void setTransform (Canvas canvas, Transform t) {
        setTransform(canvas, toAffine(t));
    }

    /** Recursive entry point for capturing a layer. */
    protected static void capture (Layer layer, Canvas canvas, Transform transform) {
        if (!layer.visible()) return;
        canvas.save();

        Transform originTransform = new AffineTransform(1, 0, -layer.originX(), -layer.originY());
        transform = transform.concatenate(layer.transform()).concatenate(originTransform);
        setTransform(canvas, transform);

        if (layer instanceof GroupLayer) {
            GroupLayer gl = (GroupLayer)layer;
            for (int ii = 0, ll = gl.size(); ii < ll; ii++) {
                capture(gl.get(ii), canvas, transform);
            }

        } else if (layer instanceof ImageLayer) {
            ImageLayer il = (ImageLayer)layer;
            canvas.drawImage(il.image(), 0, 0);
        } else if (layer instanceof ImmediateLayer) {
            ImmediateLayer il = (ImmediateLayer)layer;
            il.renderer().render(new CanvasSurface(canvas));
        }

        canvas.restore();
    }
}
