//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.gesture;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import pythagoras.f.Point;
import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

import react.Closeable;

import playn.core.Platform;
import playn.scene.Layer.HitTester;
import playn.scene.Layer;
import playn.scene.Touch;

import tripleplay.util.Timer;

/**
 * Handles multiple GestureDirectors listening on a single layer. A GestureDirector is only given a
 * touch start event if it originated within that director's region bounds. It is only given one of
 * the other events if that event is for a touch that started within that director's region bounds.
 */
public class GestureRegionDirector extends Touch.Listener
{
    /**
     * Creates an uninitalized GestureRegionDirector. setLayer() must be called separately.
     */
    public GestureRegionDirector (Platform plat, Timer timer) {
        _plat = plat;
        _timer = timer;
    }

    /**
     * Creates a GestureRegionDirector with default options.
     *
     * @param bounds The bounds in which to react to touch events.
     */
    public GestureRegionDirector (Platform plat, Timer timer, Layer layer, IRectangle bounds) {
        this(plat, timer);
        setLayer(layer, bounds);
    }

    public GestureRegionDirector setLayer (Layer layer, IRectangle bounds) {
        remove();
        _bounds = bounds;
        layer.setHitTester(new HitTester() {
            @Override public Layer hitTest (Layer layer, Point p) {
                return _bounds.contains(p.x, p.y) ? layer : null;
            }
        });
        _conn = layer.events().connect(this);
        return this;
    }

    public void remove () {
        _conn = Closeable.close(_conn);
    }

    public Collection<GestureDirector> getRegions () {
        return _regions.values();
    }

    /**
     * Gets a Region instance bounded by the given dimensions. The given bounds will restricted to
     * the given bounds of this GestureRegionDirector, and null may be returned if suitable bounds
     * are not found or a conflicting region was already defined on this GestureRegionDirector.
     */
    public GestureDirector getRegion (float x, float y, float width, float height) {
        return getRegion(new Rectangle(x, y, width, height));
    }

    /**
     * Gets a Region instance bounded by the given dimensions. The given bounds will be restricted
     * to the given bounds of this GestureRegionDirector, and null may be returned if suitable
     * bounds are not found or a conflicting region was already defined on this
     * GestureRegionDirector.
     */
    public GestureDirector getRegion (IRectangle bounds) {
        if (!_bounds.intersects(bounds)) {
            Log.log.warning("Supplied region is not within our defined bounds", "_bounds", _bounds,
                "bounds", bounds);
            return null;
        }

        Rectangle regionBounds = _bounds.intersection(bounds);
        GestureDirector region = _regions.get(regionBounds);
        if (region != null) return region;

        // make sure we don't intersect any other defined regions
        for (Rectangle existingRegionBounds : _regions.keySet()) {
            if (existingRegionBounds.intersects(regionBounds)) {
                Log.log.warning("New region intersects existing region", "existing",
                    existingRegionBounds, "new", regionBounds);
                return null;
            }
        }

        // new region!
        region = new GestureDirector(_plat, regionBounds, _timer);
        _regions.put(regionBounds, region);
        return region;
    }

    /**
     * Gets a region bounded by the given dimensions as defined as percentages of this
     * GestureRegionDirector's defined bounds. Null may be returned if suitable bounds are not found
     * or a conflicting Region was already defined on this GestureRegionDirector.
     */
    public GestureDirector getPercentRegion (
            float perX, float perY, float perWidth,  float perHeight) {
        return getRegion(applyPercentX(perX), applyPercentY(perY),
            applyPercentX(perWidth), applyPercentY(perHeight));
    }

    /**
     * Gets a Region instance bounded by the given dimensions as defined as percentages of this
     * GestureRegionDirector's defined bounds. Null may be returned if suitable bounds are not found
     * or a conflicting Region was already defined on this GestureRegionDirector.
     */
    public GestureDirector getPercentRegion (IRectangle bounds) {
        return getRegion(bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    @Override public void onStart (Touch.Interaction iact) {
        if (_regions.isEmpty()) return;

        for (GestureDirector mgr : _regions.values()) {
            if (mgr.touchInBounds(iact)) {
                _activeTouches.put(iact.event.id, new TrackedTouch(mgr, iact.event));
                mgr.onStart(iact);
                return;
            }
        }
        _ignoredTouches.add(iact.event.id);
    }

    @Override public void onMove (Touch.Interaction iact) {
        if (_regions.isEmpty()) return;

        TrackedTouch touch = _activeTouches.get(iact.event.id);
        if (touch == null && !_ignoredTouches.contains(iact.event.id)) {
            Log.log.warning("No start for move event", "event", iact.event);
        } else if (touch != null) {
            touch.region.onMove(iact);
        }
    }

    @Override public void onEnd (Touch.Interaction iact) {
        if (_regions.isEmpty()) return;

        TrackedTouch touch = _activeTouches.remove(iact.event.id);
        // set access first to ensure removal
        if (!_ignoredTouches.remove(iact.event.id) && touch == null) {
            Log.log.warning("No start for end event", "event", iact.event);
        } else if (touch != null) {
            touch.region.onEnd(iact);
        }
    }

    @Override public void onCancel (Touch.Interaction iact) {
        if (_regions.isEmpty()) return;

        TrackedTouch touch = _activeTouches.remove(iact.event.id);
        // set access first to ensure removal
        if (!_ignoredTouches.remove(iact.event.id) && touch == null) {
            Log.log.warning("No start for cancel event", "event", iact.event);
        } else if (touch != null) {
            touch.region.onCancel(iact);
        }
    }

    protected float applyPercentX (float x) {
        return _bounds.x() + _bounds.width() * x;
    }

    protected float applyPercentY (float y) {
        return _bounds.y() + _bounds.height() * y;
    }

    protected static class TrackedTouch {
        public final GestureDirector region;
        public final Touch.Event startEvent;

        public TrackedTouch (GestureDirector region, Touch.Event startEvent) {
            this.region = region;
            this.startEvent = startEvent;
        }
    }

    protected final Platform _plat;
    protected final Timer _timer;
    protected IRectangle _bounds;
    protected Closeable _conn = Closeable.NOOP;
    protected Map<Integer, TrackedTouch> _activeTouches = new HashMap<Integer, TrackedTouch>();
    protected Set<Integer> _ignoredTouches = new HashSet<Integer>();
    protected Map<Rectangle, GestureDirector> _regions = new HashMap<Rectangle, GestureDirector>();
}
