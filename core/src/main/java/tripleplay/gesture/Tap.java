package tripleplay.gesture;

import java.util.HashSet;
import java.util.Set;

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
        _touchIds.clear();
    }

    @Override protected void updateState (GestureNode node) {
        switch (node.type) {
        case PAUSE:
            Log.log.warning("Received pause on a Tap.");
            setState(State.UNQUALIFIED);
            break;

        case MOVE:
            // TODO: this may need some mitigating circumstances such as moves that are very small
            // or waiting to see if they release quickly enough after the initial start.
            // For now: any move immediately disqualifies us.
            setState(State.UNQUALIFIED);
            break;

        case CANCEL:
            setState(State.UNQUALIFIED);
            break;

        case START:
            _touchIds.add(node.touch.id());
            int size = _touchIds.size();
            if (size > _touches) setState(State.UNQUALIFIED);
            else if (size == _touches && _greedy) setState(State.GREEDY);
            break;

        case END:
            // when any touch ends, we either move to unqualified if we didn't have enough touches
            // or complete if we did.
            setState(_touchIds.size() == _touches ? State.COMPLETE : State.UNQUALIFIED);
            break;
        }
    }

    protected final int _touches;

    protected Set<Integer> _touchIds = new HashSet<Integer>();
}
