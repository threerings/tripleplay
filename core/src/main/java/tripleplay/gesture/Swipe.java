//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.gesture;

import java.util.HashMap;
import java.util.Map;

import pythagoras.f.Point;

/**
 * A simple swipe gesture in a given cardinal direction. Supports 1 to 4 fingers. If greedy, will
 * indicate a continuous swipe. Completes if: one of the fingers stops moving or is removed from the
 * display. Cancels if an additional finger is added to the display or one of the fingers switches
 * directions.
 */
public class Swipe extends GestureBase<Swipe>
{
    public Swipe (Direction direction) {
        this(1, direction);
    }

    public Swipe (int touches, Direction direction) {
        if (touches < 1 || touches > 4) {
            Log.log.warning("How many fingers do you think people have?", "tapTouches", 4);
            touches = Math.max(1, Math.min(4, touches));
        }
        if (direction == null) {
            Log.log.warning("Swipe cannot operate with a null direction, assuming RIGHT");
            direction = Direction.RIGHT;
        }
        _touches = touches;
        setDirection(direction);
    }

    /**
     * If true (the default) a pause counts the same as a finger lifted from the display.
     */
    public Swipe cancelOnPause (boolean value) {
        _cancelOnPause = value;
        return this;
    }

    /**
     * A swipe is not qualified if the user moves outside of a region defined by lines that are
     * parallel to the line that goes through the start touch point along the direction axis.
     * offAxisTolerance is the distance away from that defining line that the parallel boundaries
     * sit. The default is 10 pixels. If you want the user to have more freedom in how finely
     * defined their swipes are, make the tolerance large.
     */
    public Swipe offAxisTolerance (int pixels) {
        _offAxisTolerance = pixels;
        return this;
    }

    /**
     * The distance on the Swipe's Direction axis that a touch must move to qualify the Swipe for
     * completion.
     */
    public Swipe onAxisThreshold (int pixels) {
        _onAxisThreshold = pixels;
        return this;
    }

    /**
     * An axis Swipe will consider any movement along the axis of its configured direction to be
     * valid, rather than only movement in the direction itself.
     */
    public Swipe axisSwipe (boolean value) {
        _axisSwipe = value;
        return this;
    }

    @Override protected void clearMemory () {
        _movedEnough = false;
        _startNodes.clear();
        _lastNodes.clear();
    }

    @Override protected void updateState (GestureNode node) {
        switch (node.type) {
        case START:
            _startNodes.put(node.touch.id(), node);
            break;

        case MOVE:
            // always grounds for immediate dismissal
            if (_startNodes.size() != _touches) setState(State.UNQUALIFIED);
            evaluateMove(node);
            break;

        case PAUSE:
            if (_cancelOnPause) setState(getEndState());
            break;

        case END:
            setState(getEndState());
            break;

        case CANCEL:
            setState(State.UNQUALIFIED);
            break;
        }
    }

    protected State getEndState () {
        return _movedEnough && _startNodes.size() == _touches ? State.COMPLETE : State.UNQUALIFIED;
    }

    // TODO: any gesture that cares about swiping in a cardinal direction could make use of this
    protected void evaluateMove (GestureNode node) {
        GestureNode start = _startNodes.get(node.touch.id());
        if (start == null) {
            Log.log.warning("No start point for a path check, invalid state",
                "touchId", node.touch.id());
            return;
        }

        GestureNode lastNode = _lastNodes.get(node.touch.id());
        Point current = node.location();
        _lastNodes.put(node.touch.id(), node);
        // we haven't moved far enough yet, no further evaluation needed.
        Point startLoc = start.location();
        if (current.distance(startLoc) < _onAxisThreshold) return;

        float offAxisDistance; // distance from our start position in the perpendicular axis
        float lastAxisDistance = axisDistance(
            lastNode == null ? null : lastNode.location(), current);
        if (_direction == Direction.UP || _direction == Direction.DOWN)
            offAxisDistance = Math.abs(current.x() - startLoc.x());
        else
            offAxisDistance = Math.abs(current.y() - startLoc.y());

        // if we've strayed outside of the safe zone, or we've backtracked from our last position,
        // disqualify
        if (offAxisDistance > _offAxisTolerance) setState(State.UNQUALIFIED);
        else if (lastAxisDistance < 0) backtracked(node, -lastAxisDistance);

        // Figure out if we've moved enough to meet minimum requirements with all touches
        if (!_movedEnough) {
            boolean allMovedEnough = true;
            for (Map.Entry<Integer, GestureNode> touchStart : _startNodes.entrySet()) {
                GestureNode touchLast = _lastNodes.get(touchStart.getKey());
                if (axisDistance(touchStart.getValue().location(),
                    touchLast == null ? null : touchLast.location()) <=
                    _onAxisThreshold) {
                    allMovedEnough = false;
                    break;
                }
            }
            if (allMovedEnough) {
                _movedEnough = true;
                if (_greedy) setState(State.GREEDY);
            }
        }
    }

    protected float axisDistance (Point start, Point end) {
        if (start == null || end == null) return 0;

        float value;
        if (_direction == Direction.UP || _direction == Direction.DOWN)
            value = end.y() - start.y();
        else
            value = end.x() - start.x();
        return _axisSwipe ? Math.abs(value) : value * _directionModifier;
    }

    protected void backtracked (GestureNode node, float distance) {
        if (!_axisSwipe) {
            setState(State.UNQUALIFIED);
        }
    }

    protected void setDirection (Direction direction) {
        _direction = direction;
        _directionModifier = _direction == Direction.UP || _direction == Direction.LEFT ? -1 : 1;
    }

    protected int _touches;
    protected Direction _direction;
    protected int _directionModifier;

    protected boolean _movedEnough = false;
    protected Map<Integer, GestureNode> _startNodes = new HashMap<Integer, GestureNode>();
    protected Map<Integer, GestureNode> _lastNodes = new HashMap<Integer, GestureNode>();
    protected boolean _cancelOnPause = true;
    protected int _offAxisTolerance = 10;
    protected int _onAxisThreshold = 10;
    protected boolean _axisSwipe = false;
}
