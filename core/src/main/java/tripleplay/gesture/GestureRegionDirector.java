//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.gesture;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import playn.core.Connection;
import playn.core.Layer;
import playn.core.Layer.HitTester;
import playn.core.Touch;
import playn.core.Touch.Event;

import pythagoras.f.Point;
import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

import tripleplay.util.Timer;

/**
 * Handles multiple GestureDirectors listening on a single layer. A GestureDirector is only given a
 * touch start event if it originated within that director's region bounds. It is only given one of
 * the other events if that event is for a touch that started within that director's region bounds.
 */
public class GestureRegionDirector
    implements Touch.LayerListener
{
    public static interface RegionTranslator {
        public void registerCommands (GestureRegionDirector gestureDir);
    }

    /**
     * Creates an uninitalized GestureRegionDirector. setLayer() must be called separately.
     */
    public GestureRegionDirector (Timer timer) {
        _timer = timer;
    }

    /**
     * Creates a GestureRegionDirector with default options.
     *
     * @param bounds The bounds in which to react to touch events.
     */
    public GestureRegionDirector (Timer timer, Layer layer, IRectangle bounds) {
        this(timer);
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
        _connection = layer.addListener(this);
        return this;
    }

    public void remove () {
        if (_connection != null) _connection.disconnect();
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
        region = new GestureDirector(regionBounds, _timer);
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

    @Override public void onTouchStart (Event event) {
        if (_regions.isEmpty()) return;

        for (GestureDirector mgr : _regions.values()) {
            if (mgr.touchInBounds(event)) {
                _activeTouches.put(event.id(), new TrackedTouch(mgr, event));
                mgr.onTouchStart(event);
                break;
            }
        }
    }

    @Override public void onTouchMove (Event event) {
        if (_regions.isEmpty()) return;

        TrackedTouch touch = _activeTouches.get(event.id());
        if (touch == null) {
            Log.log.warning("No start for move event", "event", event);
            return;
        }
        touch.region.onTouchMove(event);
    }

    @Override public void onTouchEnd (Event event) {
        if (_regions.isEmpty()) return;

        TrackedTouch touch = _activeTouches.remove(event.id());
        if (touch == null) {
            Log.log.warning("No start for end event", "event", event);
            return;
        }
        touch.region.onTouchEnd(event);
    }

    @Override public void onTouchCancel (Event event) {
        if (_regions.isEmpty()) return;

        TrackedTouch touch = _activeTouches.remove(event.id());
        if (touch == null) {
            Log.log.warning("No start for cancel event", "event", event);
            return;
        }
        touch.region.onTouchCancel(event);
    }

    protected float applyPercentX (float x) {
        return _bounds.x() + _bounds.width() * x;
    }

    protected float applyPercentY (float y) {
        return _bounds.y() + _bounds.height() * y;
    }

    protected static class TrackedTouch {
        public final GestureDirector region;
        public final Event startEvent;

        public TrackedTouch (GestureDirector region, Event startEvent) {
            this.region = region;
            this.startEvent = startEvent;
        }
    }

    protected Timer _timer;
    protected IRectangle _bounds;
    protected Connection _connection;
    protected Map<Integer, TrackedTouch> _activeTouches = new HashMap<Integer, TrackedTouch>();
    protected Map<Rectangle, GestureDirector> _regions = new HashMap<Rectangle, GestureDirector>();
}
