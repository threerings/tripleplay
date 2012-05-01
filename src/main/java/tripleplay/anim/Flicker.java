//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import pythagoras.f.MathUtil;

import react.Signal;

import playn.core.Game;
import playn.core.Pointer;

/**
 * Implements click, and scroll/flick gestures for a single variable (y position by default). When
 * the pointer is pressed and dragged, the scroll position is updated to track the pointer. If the
 * last two pointer events describe a motion of sufficiently high velocity, the scroll position is
 * "flicked" and undergoes (friction decelerated) motion. If the pointer is pressed and released
 * without scrolling or flicking, the {@link #clicked} signal is emitted.
 *
 * <p>To use: create a flicker, configure it as a pointer listener on a layer that represents your
 * touchable area, then use {@link Flicker#position} to position your flickable layer (or to offset
 * your hand-drawn flickable elements) on every frame tick. You must also call {@link
 * Flicker#update} on every frame tick (to process changes due to acceleration and velocity).</p>
 *
 * <p>Various flick parameters can be customized by overriding the appropriate method: {@link
 * #friction}, {@link #maxFlickVel}, etc.</p>
 *
 * <p><em>Note:</em> the flicker does not currently implement the iOS-style "bounce" that happens
 * when an entity is flicked hard against its upper or lower bounds. The flicker simply stops when
 * it reaches its bounds.</p>
 */
public class Flicker extends Pointer.Adapter
{
    /** The current position value. */
    public float position;

    /** A signal that is emitted (with the pointer end event) on click. */
    public Signal<Pointer.Event> clicked = Signal.create();

    /**
     * Creates a flicker with the specified initial, minimum and maximum values.
     */
    public Flicker (float initial, float min, float max) {
        this.position = initial;
        _min = min;
        _max = max;
    }

    /** This must be called every frame with {@link Game#update}'s delta. */
    public void update (float delta) {
        if (_vel != 0) {
            float prev = position;
            position = MathUtil.clamp(position + _vel * delta, _min, _max);
            if (position == prev) _vel = 0; // for now stop when we hit the edge
            float prevVel = _vel;
            _vel += _accel * delta;
            // if we decelerate past zero velocity, stop
            if (Math.signum(prevVel) != Math.signum(_vel)) _vel = 0;
        }
    }

    @Override public void onPointerStart (Pointer.Event event) {
        _vel = 0;
        _maxDelta = 0;
        _origPos = position;
        _start = _prev = _cur = getPosition(event);
        _prevStamp = 0;
        _curStamp = event.time();
    }

    @Override public void onPointerDrag (Pointer.Event event) {
        _prev = _cur;
        _prevStamp = _curStamp;
        _cur = getPosition(event);
        _curStamp = event.time();
        float delta = _cur - _start;
        position = MathUtil.clamp(_origPos + delta, _min, _max);
        _maxDelta = Math.max(Math.abs(delta), _maxDelta);
    }

    @Override public void onPointerEnd (Pointer.Event event) {
        // check whether we should call onClick
        if (_maxDelta < maxClickDelta()) clicked.emit(event);
        // if not, determine whether we should impart velocity to the tower
        else {
            float dragTime = (float)(_curStamp - _prevStamp);
            float delta = _cur - _prev;
            float signum = Math.signum(delta);
            float dragVel = Math.abs(delta) / dragTime;
            if (dragVel > flickVelThresh() && delta > minFlickDelta()) {
                _vel = signum * Math.min(maxFlickVel(), dragVel * flickXfer());
                _accel = -signum * friction();
            }
        }
    }

    /**
     * Extracts the desired position from the pointer event. The default is to use the y-position.
     */
    protected float getPosition (Pointer.Event event) {
        return event.localY();
    }

    /**
     * Returns the deceleration (in pixels per ms per ms) applied to non-zero velocity.
     */
    protected float friction () {
        return 0.003f;
    }

    /**
     * Returns the minimum (positive) velocity (in pixels per millisecond) at time of touch release
     * required to initiate a flick (i.e. transfer the flick velocity to the entity).
     */
    protected float flickVelThresh () {
        return 0.5f;
    }

    /**
     * Returns the fraction of flick velocity that is transfered to the entity (a value between 0
     * and 1).
     */
    protected float flickXfer () {
        return 0.7f;
    }

    /**
     * Returns the maximum flick velocity that will be transfered to the entity; limits the actual
     * flick velocity at time of release. This value is not adjusted by {@link #flickXfer}.
     */
    protected float maxFlickVel () {
        return 1.2f; // pixels/ms
    }

    /**
     * Returns the maximum distance (in pixels) the pointer is allowed to travel while pressed and
     * still register as a click.
     */
    protected float maxClickDelta () {
        return 5;
    }

    /**
     * Returns the minimum distance (in pixels) the pointer must have moved to register as a flick.
     */
    protected float minFlickDelta () {
        return 10;
    }

    protected final float _min, _max;

    protected float _vel, _accel;
    protected float _origPos, _start, _cur, _prev;
    protected double _curStamp, _prevStamp;
    protected float _maxDelta;
}
