//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.ArrayList;
import java.util.List;

import playn.core.Layer;
import playn.core.Pointer;

import pythagoras.f.IPoint;
import pythagoras.f.IRectangle;
import pythagoras.f.Point;

/**
 * Dispatches user input to the appropriate entity.
 */
public class Input
{
    /** Provides a handle on a listener or action registration. */
    public interface Registration {
        /** Unregisters the action associated with this handle. */
        void cancel ();
    }

    /** A listener that will call {@link Action#onTrigger} when the pointer is pressed. */
    public static abstract class Action extends Pointer.Adapter {
        /** Called when the user triggers a reactor. */
        public abstract void onTrigger ();

        @Override public void onPointerStart (Pointer.Event event) {
            onTrigger();
        }
    }

    /** Encapsulates enabledness, expiry, and hit testing. */
    public static abstract class Region {
        /** Returns true if this region can be triggered, false if it's currently invisible. */
        public boolean canTrigger () {
            return true;
        }

        /** Returns true if this region is no longer relevant and should be removed. */
        public boolean hasExpired () {
            return false;
        }

        /** Returns true if the (screen-coordinates) point triggers falls in this region. */
        public abstract boolean hitTest (IPoint p);
    }

    /** A region that encompasses the entire screen. */
    public static class ScreenRegion extends Region {
        @Override public boolean hitTest (IPoint p) {
            return true;
        }
    }

    /** A region that encompasses the supplied (screen) bounds. */
    public static class BoundsRegion extends Region {
        public BoundsRegion (IRectangle bounds) {
            _bounds = bounds;
        }

        @Override public boolean hitTest (IPoint p) {
            return _bounds.contains(p);
        }

        protected IRectangle _bounds;
    }

    /** A region that encompasses supplied bounds, as transformed by the supplied layer's
     * transform. While the layer in question is not visible, the region will match clicks. If a
     * reaction using this region is considered for processing and its layer has been removed from
     * the view hierarchy, it will automatically be canceled. */
    public static class LayerRegion extends Region {
        public LayerRegion (Layer layer, IRectangle bounds) {
            _layer = layer;
            _bounds = bounds;
        }

        @Override public boolean canTrigger () {
            return _layer.visible();
        }
        @Override public boolean hasExpired () {
            return _layer.parent() == null;
        }
        @Override public boolean hitTest (IPoint p) {
            // convert the screen coordinates into layer-relative coordinates and check that the
            // point falls within the (layer-transform-relative) bounds
            return _bounds.contains(Layer.Util.screenToLayer(_layer, p, new Point()));
        }

        protected Layer _layer;
        protected IRectangle _bounds;
    }

    /** A region that encompasses the supplied layer's (transformed) bounds. While the layer in
     * question is not visible, the region will not be match clicks. If a reaction using this
     * region is considered for processing and its layer has been removed from the view hierarchy,
     * it will automatically be canceled. */
    public static class SizedLayerRegion extends Region {
        public SizedLayerRegion (Layer.HasSize layer) {
            _layer = layer;
        }

        @Override public boolean canTrigger () {
            return _layer.visible();
        }
        @Override public boolean hasExpired () {
            return _layer.parent() == null;
        }
        @Override public boolean hitTest (IPoint p) {
            // convert the screen coordinates into layer-relative coordinates
            Point lp = Layer.Util.screenToLayer(_layer, p, new Point());
            float x = lp.x, y = lp.y;
            return (x > 0 && y > 0 && x < _layer.scaledWidth() && y < _layer.scaledHeight());
        }

        protected Layer.HasSize _layer;
    }

    /** Receives input from the PlayN Pointer service. */
    public final Pointer.Listener plistener = new Pointer.Listener() {
        @Override public void onPointerStart (Pointer.Event event) {
            // take a snapshot of the regions list to avoid concurrent modification if reactions
            // are added or removed during processing
            List<Reaction> snapshot = new ArrayList<Reaction>(_reactions);
            Point p = new Point(event.x(), event.y());
            for (int ii = snapshot.size() - 1; ii >= 0; ii--) {
                Reaction r = snapshot.get(ii);
                if (r.region.hasExpired()) {
                    _reactions.remove(r);
                } else if (r.region.canTrigger() && r.region.hitTest(p)) {
                    _active = r;
                    r.listener.onPointerStart(event);
                    break;
                }
            }
        }

        @Override public void onPointerDrag (Pointer.Event event) {
            if (_active != null) {
                _active.listener.onPointerDrag(event);
            }
        }

        @Override public void onPointerEnd (Pointer.Event event) {
            if (_active != null) {
                _active.listener.onPointerEnd(event);
                _active = null;
            }
        }

        protected Reaction _active;
    };

    /**
     * Configures a reaction to be notified of pointer activity. On pointer start, reactions will be
     * scanned from most-recently-registered to least-recently-registered and hit-tested. Thus more
     * recently registered reactions that overlap previously registered reactions will take
     * precedence. TODO: use layer depth information to hit test based on depth.
     *
     * <p> Subsequent pointer drag and end events will be dispatched to the reaction that
     * successfully hit-tested the pointer start. </p>
     *
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (Region region, Pointer.Listener listener) {
        final Reaction reaction = new Reaction(region, listener);
        _reactions.add(reaction);
        return new Registration() {
            @Override public void cancel () {
                _reactions.remove(reaction);
            }
        };
    }

    /**
     * Registers a reaction using {@link ScreenRegion} and the supplied listener.
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (Pointer.Listener listener) {
        return register(new ScreenRegion(), listener);
    }

    /**
     * Registers a reaction using {@link BoundsRegion} and the supplied listener.
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (IRectangle bounds, Pointer.Listener listener) {
        return register(new BoundsRegion(bounds), listener);
    }

    /**
     * Registers a reaction using {@link LayerRegion} and the supplied listener.
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (Layer layer, IRectangle bounds, Pointer.Listener listener) {
        return register(new LayerRegion(layer, bounds), listener);
    }

    /**
     * Registers a reaction using {@link SizedLayerRegion} and the supplied listener.
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (Layer.HasSize layer, Pointer.Listener listener) {
        return register(new SizedLayerRegion(layer), listener);
    }

    protected final class Reaction {
        public final Region region;
        public final Pointer.Listener listener;

        public Reaction (Region region, Pointer.Listener listener) {
            this.region = region;
            this.listener = listener;
        }
    }

    /** A list of all registered reactions. */
    protected List<Reaction> _reactions = new ArrayList<Reaction>();
}
