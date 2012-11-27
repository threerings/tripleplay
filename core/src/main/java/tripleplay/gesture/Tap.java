//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.gesture;

import java.util.HashMap;
import java.util.Map;

import pythagoras.f.Point;

/**
 * A simple touch gesture. May support 1 to 4 fingers. If greedy, will indicate a held touch, and
 * will cancel if the fingers start to move.
 */
public class Tap extends GestureBase<Tap>
{
    public Tap () {
        this(1);
    }

    public Tap (int touches) {
        if (touches < 1 || touches > 4) {
            Log.log.warning("How many fingers do you think people have?", "tapTouches", 4);
            touches = Math.max(1, Math.min(4, touches));
        }
        _touches = touches;
    }

    @Override protected void clearMemory () {
        _startPoints.clear();
    }

    @Override protected void updateState (GestureNode node) {
        switch (node.type) {
        case PAUSE:
            setState(State.UNQUALIFIED);
            break;

        case MOVE:
            Point start = _startPoints.get(node.touch.id());
            if (start == null) Log.log.warning("No start for a moved touch", "id", node.touch.id());
            else if (start.distance(node.location()) > MOVE_THRESHOLD) setState(State.UNQUALIFIED);
            break;

        case CANCEL:
            setState(State.UNQUALIFIED);
            break;

        case START:
            _startPoints.put(node.touch.id(), node.location());
            int size = _startPoints.size();
            if (size > _touches) setState(State.UNQUALIFIED);
            else if (size == _touches && _greedy) setState(State.GREEDY);
            break;

        case END:
            // when any touch ends, we either move to unqualified if we didn't have enough touches
            // or complete if we did.
            setState(_startPoints.size() == _touches ? State.COMPLETE : State.UNQUALIFIED);
            break;
        }
    }

    // They must move this far to be disqualified for a tap
    protected static final int MOVE_THRESHOLD = 10;

    protected final int _touches;

    protected Map<Integer, Point> _startPoints = new HashMap<Integer, Point>();
}
