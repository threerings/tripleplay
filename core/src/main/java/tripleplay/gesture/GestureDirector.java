package tripleplay.gesture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import playn.core.Touch;
import playn.core.Touch.Event;

import pythagoras.f.IRectangle;

import tripleplay.util.Timer;
import tripleplay.util.Timer.Handle;

/**
 * Resolves user touch input in terms of a set of {@link Gesture}s that are registered with this
 * director. May either be used as a standalone listener on a layer, or along with a set of other
 * regions in a given layer via GestureRegionDirector.
 *
 * Will only consider touches that start within the defined bounds, but if it is given touch events
 * that end outside of the bounds, but started inside the bounds it will react to them.
 */
public class GestureDirector
    implements Touch.LayerListener
{
    public final IRectangle bounds;

    public GestureDirector (IRectangle bounds, Timer timer) {
        this.bounds = bounds;
        _timer = timer;
    }

    /**
     * Adds a gesture to the set considered during each user interaction. Returns this director for
     * chaining.
     */
    public GestureDirector add (Gesture gesture) {
        _gestures.add(gesture);
        return this;
    }

    /**
     * Removes the given gesture from the set considered during each user interaction. Returns false
     * if that gesture was not found.
     */
    public boolean remove (Gesture gesture) {
        return _gestures.remove(gesture);
    }

    public boolean touchInBounds (Event touch) {
        return bounds.contains(touch.localX(), touch.localY());
    }

    public boolean trackingTouch (Event touch) {
        return _currentTouches.containsKey(touch.id());
    }

    @Override public void onTouchStart (Event touch) {
        if (!touchInBounds(touch)) return;

        if (_currentTouches.isEmpty()) {
            // new user interaction!
            for (Gesture gesture : _gestures) gesture.start();
            _greedy = null;
        }
        _currentTouches.put(touch.id(), touch);
        evaluateGestures(new GestureNode(GestureNode.Type.START, touch));
    }

    @Override public void onTouchMove (Event touch) {
        if (!trackingTouch(touch)) return;
        _currentTouches.put(touch.id(), touch);
        evaluateGestures(new GestureNode(GestureNode.Type.MOVE, touch));
    }

    @Override public void onTouchEnd (Event touch) {
        if (!trackingTouch(touch)) return;
        _currentTouches.remove(touch.id());
        evaluateGestures(new GestureNode(GestureNode.Type.END, touch));
    }

    @Override public void onTouchCancel (Event touch) {
        if (!trackingTouch(touch)) return;
        _currentTouches.remove(touch.id());
        evaluateGestures(new GestureNode(GestureNode.Type.CANCEL, touch));
    }

    protected void onTouchPause (Event touch) {
        if (!trackingTouch(touch)) {
            Log.log.warning("Bad state: received pause dispatch for an event we're not tracking",
                "event", touch);
            return;
        }
        // no need to update _currentTouches, it already has this touch registered
        evaluateGestures(new GestureNode(GestureNode.Type.PAUSE, touch));
    }

    protected void evaluateGestures (final GestureNode node) {
        // dispatch a pause event on touches that haven't moved for PAUSE_DELAY.
        Handle handle = _currentMoves.remove(node.touch.id());
        if (handle != null) handle.cancel();
        if (node.type == GestureNode.Type.MOVE) {
            handle = _timer.after(PAUSE_DELAY, new Runnable() {
                @Override public void run () { onTouchPause(node.touch); }
            });
            _currentMoves.put(node.touch.id(), handle);
        }

        if (_greedy != null) {
            _greedy.evaluate(node);
            return;
        }

        List<Gesture> greedy = new ArrayList<Gesture>();
        List<Gesture> complete = new ArrayList<Gesture>();
        for (Gesture gesture : _gestures) {
            if (gesture.state() == Gesture.State.UNQUALIFIED) continue;

            gesture.evaluate(node);
            switch (gesture.state()) {
            case GREEDY: greedy.add(gesture); break;
            case COMPLETE: complete.add(gesture); break;
            }
        }

        int greedyAndComplete = greedy.size() + complete.size();
        if (greedyAndComplete > 1) {
            Log.log.warning(
                "More than one gesture transitioned to GREEDY or COMPLETE on a single node",
                "node", node, "greedy", greedy, "complete", complete);
            // soldier on: the first greedy gesture will have priority
        }
        _greedy = greedy.isEmpty() ? null : greedy.get(0);
        if (greedyAndComplete > 0) {
            // put all but the potential greedy gesture into UNQUALIFIED for the remainder of this
            // interaction.
            for (Gesture gesture : _gestures) if (_greedy != gesture) gesture.cancel();
        }
    }

    protected static final int PAUSE_DELAY = 500; // in ms.

    protected Timer _timer;
    protected Map<Integer, Event> _currentTouches = new HashMap<Integer, Event>();
    protected Map<Integer, Handle> _currentMoves = new HashMap<Integer, Handle>();
    protected Set<Gesture> _gestures = new HashSet<Gesture>();
    protected Gesture _greedy;
}
