//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.gesture;

import react.Signal;
import react.SignalView;
import react.UnitSignal;

/**
 * A base class for Gestures to extend to get some common functionality.
 * {@link #updateState(GestureNode)} is called when the the gesture is either PASSIVE or GREEDY
 * and therefore primed to react to user interaction.
 */
public abstract class GestureBase<T extends GestureBase<T>>
    implements Gesture<T>
{
    /**
     * Greedy gestures will dispatch both a {@link #started()} and {@link #completed()} event, and
     * are considered greedy because once their start conditions have been met, they will prevent
     * other gestures from further consideration for dispatch.
     *
     * @return this gesture for chaining.
     */
    public T greedy (boolean value) {
        _greedy = value;
        return asT();
    }

    public boolean greedy () {
        return _greedy;
    }

    @Override public void start () {
        setState(State.PASSIVE);
    }

    @Override public void cancel () {
        setState(State.UNQUALIFIED);
    }

    @Override public void evaluate (GestureNode node) {
        if (_state == State.PASSIVE || _state == State.GREEDY) updateState(node);
    }

    @Override public State state () {
        return _state;
    }

    @Override public SignalView<Void> started () {
        return _started;
    }

    @Override public SignalView<Boolean> completed () {
        return _completed;
    }

    /**
     * Subclasses must reset their internal state and memory when called, resetting to passive.
     */
    protected abstract void clearMemory ();

    /**
     * Overridden by subclasses to perform necessary state transitions.
     */
    protected abstract void updateState (GestureNode node);

    /**
     * Subclasses should call this method in order to modify _state.
     */
    protected void setState (State state) {
        if (state == _state) return; // NOOP

        if (state == State.GREEDY && _state != State.PASSIVE)
            Log.log.warning("Transitioning to GREEDY from !PASSIVE", "current", _state,
                "gesture", this);

        State previous = _state;
        _state = state;
        // dispatch signals after setting state so that anything checking on our honesty doesn't
        // get a nasty surprise
        if (state == State.GREEDY) _started.emit();
        if ((state == State.UNQUALIFIED || state == State.PASSIVE) && previous == State.GREEDY)
            _completed.emit(false);
        if (state == State.COMPLETE) _completed.emit(true);
        if (state == State.PASSIVE) clearMemory();
    }

    /**
     * Returns <code>this</code> cast to <code>T</code>.
     */
    @SuppressWarnings({"unchecked", "cast"}) protected T asT () {
        return (T)this;
    }

    private State _state = State.PASSIVE; // state should only be modified via setState()

    protected UnitSignal _started = new UnitSignal();
    protected Signal<Boolean> _completed = Signal.create();
    protected boolean _greedy;
}
