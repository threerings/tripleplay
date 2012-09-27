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
        _direction = direction;
        _directionModifier = _direction == Direction.UP || _direction == Direction.LEFT ? -1 : 1;
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

    @Override protected void clearMemory () {
        _movedEnough = false;
        _startPoints.clear();
        _lastPoints.clear();
    }

    @Override protected void updateState (GestureNode node) {
        switch (node.type) {
        case START:
            _startPoints.put(node.touch.id(), node.location());
            break;

        case MOVE:
            // always grounds for immediate dismissal
            if (_startPoints.size() != _touches) setState(State.UNQUALIFIED);
            evaluateMove(node);
            break;

        case PAUSE:
            if (!_cancelOnPause) break; // fall through if we do cancel on pause

        case END:
            setState(_movedEnough && _startPoints.size() == _touches ?
                State.COMPLETE : State.UNQUALIFIED);
            break;

        case CANCEL:
            setState(State.UNQUALIFIED);
            break;
        }
    }

    // TODO: any gesture that cares about swiping in a cardinal direction could make use of this
    protected void evaluateMove (GestureNode node) {
        Point start = _startPoints.get(node.touch.id());
        if (start == null) {
            Log.log.warning("No start point for a path check, invalid state",
                "touchId", node.touch.id());
            return;
        }

        Point last = _lastPoints.get(node.touch.id());
        Point current = node.location();
        _lastPoints.put(node.touch.id(), current);
        // we haven't moved far enough yet, no further evaluation needed.
        if (current.distance(start) < DIRECTION_THRESHOLD) return;

        float offAxisDistance; // distance from our start position in the perpendicular axis
        float lastAxisDistance = axisDistance(last, current);
        if (_direction == Direction.UP || _direction == Direction.DOWN)
            offAxisDistance = Math.abs(current.x() - start.x());
        else
            offAxisDistance = Math.abs(current.y() - start.y());

        // if we've strayed outside of the safe zone, or we've backtracked from our last position,
        // disqualify
        if (offAxisDistance > _offAxisTolerance || lastAxisDistance < 0)
            setState(State.UNQUALIFIED);

        // Figure out if we've moved enough to meet minimum requirements with all touches
        if (!_movedEnough) {
            boolean allMovedEnough = true;
            for (Map.Entry<Integer, Point> touchStart : _startPoints.entrySet()) {
                Point touchLast = _lastPoints.get(touchStart.getKey());
                if (axisDistance(touchStart.getValue(), touchLast) <= DIRECTION_THRESHOLD) {
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

        if (_direction == Direction.UP || _direction == Direction.DOWN)
            return (end.y() - start.y()) * _directionModifier;
        else
            return (end.x() - start.x()) * _directionModifier;
    }

    // the furthest in a perpendicular direction a finger can go before being considered to not be
    // moving the in the right direction. Also the distance that a finger must move to be considered
    // a swipe.
    protected static final int DIRECTION_THRESHOLD = 10;

    protected final int _touches;
    protected final Direction _direction;
    protected final int _directionModifier;

    protected boolean _movedEnough = false;
    protected Map<Integer, Point> _startPoints = new HashMap<Integer, Point>();
    protected Map<Integer, Point> _lastPoints = new HashMap<Integer, Point>();
    protected boolean _cancelOnPause = true;
    protected int _offAxisTolerance = 10;
}
