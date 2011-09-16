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

    /** Encapsulates hit testing and reactor expiry. */
    public static abstract class Reactor {
        /** The pointer listener that receives events for this reactor. */
        public final Pointer.Listener listener;

        /** Returns true if this listener is no longer relevant and should be removed. */
        public abstract boolean hasExpired ();

        /** Returns true if the (screen-coordinates) point triggers this listener. */
        public abstract boolean hitTest (IPoint p);

        public Reactor (Pointer.Listener listener) {
            this.listener = listener;
        }
    }

    /** A reactor that responds to clicks anywhere on the entire screen. */
    public static class ScreenReactor extends Reactor {
        public ScreenReactor (Pointer.Listener listener) {
            super(listener);
        }

        @Override public boolean hasExpired () {
            return false;
        }
        @Override public boolean hitTest (IPoint p) {
            return true;
        }
    }

    /** A reactor that responds to clicks in the supplied (screen) bounds. */
    public static class BoundsReactor extends ScreenReactor {
        public BoundsReactor (IRectangle bounds, Pointer.Listener listener) {
            super(listener);
            _bounds = bounds;
        }

        @Override public boolean hitTest (IPoint p) {
            return _bounds.contains(p);
        }

        protected IRectangle _bounds;
    }

    /** A reactor that responds to clicks in the supplied bounds, as transformed by the supplied
     * layer's transform. While the layer in question is not visible, the reactor will not be
     * notified. If this reactor is considered for processing and its layer has been removed from
     * the view hierarchy, it will automatically be canceled. */
    public static class LayerReactor extends Reactor {
        public LayerReactor (Layer layer, IRectangle bounds, Pointer.Listener listener) {
            super(listener);
            _layer = layer;
            _bounds = bounds;
        }

        @Override public boolean hasExpired () {
            return _layer.parent() == null;
        }
        @Override public boolean hitTest (IPoint p) {
            if (!_layer.visible()) return false;
            // convert the screen coordinates into layer-relative coordinates and check that the
            // point falls within the (layer-transform-relative) bounds
            return _bounds.contains(Layer.Util.screenToLayer(_layer, p, new Point()));
        }

        protected Layer _layer;
        protected IRectangle _bounds;
    }

    /** A reactor that responds to clicks in the supplied layer's (transformed) bounds. While the
     * layer in question is not visible, the reactor will not be notified. If this reactor is
     * considered for processing and its layer has been removed from the view hierarchy, it will
     * automatically be canceled. */
    public static class SizedLayerReactor extends Reactor {
        public SizedLayerReactor (Layer.HasSize layer, Pointer.Listener listener) {
            super(listener);
            _layer = layer;
        }

        @Override public boolean hasExpired () {
            return _layer.parent() == null;
        }
        @Override public boolean hitTest (IPoint p) {
            if (!_layer.visible()) return false;
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
            // take a snapshot of the reactors list to avoid concurrent modification if reactors
            // are added or removed during processing
            List<Reactor> snapshot = new ArrayList<Reactor>(_reactors);
            Point p = new Point(event.x(), event.y());
            for (int ii = snapshot.size() - 1; ii >= 0; ii--) {
                Reactor r = snapshot.get(ii);
                if (r.hasExpired()) {
                    _reactors.remove(r);
                } else if (r.hitTest(p)) {
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

        protected Reactor _active;
    };

    /**
     * Configures a reactor to be notified of pointer activity. On pointer start, reactors will be
     * scanned from most-recently-registered to least-recently-registered and hit-tested. Thus more
     * recently registered reactors that overlap previously registered reactors will take
     * precedence. TODO: use layer depth information to hit test based on depth.
     *
     * <p> Subsequent pointer drag and end events will be dispatched to the reactor that
     * successfully hit-tested the pointer start. </p>
     *
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (final Reactor reactor) {
        _reactors.add(reactor);
        return new Registration() {
            @Override public void cancel () {
                _reactors.remove(reactor);
            }
        };
    }

    /**
     * Registers a @{link ScreenReactor}.
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (Pointer.Listener listener) {
        return register(new ScreenReactor(listener));
    }

    /**
     * Registers a @{link BoundsReactor}.
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (IRectangle bounds, Pointer.Listener listener) {
        return register(new BoundsReactor(bounds, listener));
    }

    /**
     * Registers a {@link LayerReactor}.
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (Layer layer, IRectangle bounds, Pointer.Listener listener) {
        return register(new LayerReactor(layer, bounds, listener));
    }

    /**
     * Registers a {@link SizedLayerReactor}.
     * @return a handle that can be used to clear this registration.
     */
    public Registration register (Layer.HasSize layer, Pointer.Listener listener) {
        return register(new SizedLayerReactor(layer, listener));
    }

    /** A list of all registered reactors. */
    protected List<Reactor> _reactors = new ArrayList<Reactor>();
}
