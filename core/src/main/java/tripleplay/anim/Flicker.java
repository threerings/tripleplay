//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import pythagoras.f.MathUtil;

import react.Signal;
import react.Slot;
import react.Value;

import playn.core.Clock;
import playn.scene.Pointer;

import tripleplay.util.Interpolator;

/**
 * Implements click, and scroll/flick gestures for a single variable (y position by default). When
 * the pointer is pressed and dragged, the scroll position is updated to track the pointer. If the
 * last two pointer events describe a motion of sufficiently high velocity, the scroll position is
 * "flicked" and undergoes (friction decelerated) motion. If the pointer is pressed and released
 * without scrolling or flicking, the {@link #clicked} signal is emitted.
 *
 * <p>To use: create a flicker, configure it as a pointer listener on a layer that represents your
 * touchable area, then use {@link Flicker#position} to position your flickable layer (or to offset
 * your hand-drawn flickable elements) on every frame tick. You must also connect {@link
 * Flicker#onPaint} to a paint signal (to process changes due to acceleration and velocity).</p>
 *
 * <p>Various flick parameters can be customized by overriding the appropriate method:
 * {@link #friction}, {@link #maxFlickVel}, etc.</p>
 */
public class Flicker extends Pointer.Listener
{
    /** This flicker's bounds. */
    public float min, max;

    /** The current position value. */
    public float position;

    /** A signal emitted when this flicker's position has changed. */
    public Signal<Flicker> changed = Signal.create();

    /** A signal that is emitted (with the pointer end event) on click. */
    public Signal<Pointer.Interaction> clicked = Signal.create();

    /** Whether or not this flicker is enabled (responding to pointer events). Disabling a flicker
     * does not stop any existing physical behavior, it just prevents the user from introducing any
     * further behavior by flicking or tapping.
     *
     * <p>Note that if a pointer interaction has already started when the flicker is disabled, that
     * interaction will be allowed to complete. Otherwise the flicker would be left in an
     * unpredictable state.</p> */
    public Value<Boolean> enabled = Value.create(true);

    /** This must be connected to a paint signal. */
    public Slot<Clock> onPaint = new Slot<Clock>() {
        public void onEmit (Clock clock) { onPaint(clock); }
    };

    /**
     * Creates a flicker with the specified initial, minimum and maximum values.
     */
    public Flicker (float initial, float min, float max) {
        this.position = initial;
        this.min = min;
        this.max = max;
    }

    /** Connects this flicker to the {@code paint} signal. */
    public Flicker connect (Signal<Clock> paint) {
        paint.connect(onPaint);
        return this;
    }

    /** Returns the position of this flicker as an animation value. */
    public Animation.Value posValue () {
        return new Animation.Value() {
            public float initial () { return position; }
            public void set (float value) { setPosition(value); }
        };
    }

    /** Stops any active movement of this flicker. The position is immediately clamped back into
      * {@code min/max} which may be jarring if the flicker was in the middle of a rebound and
      * outside its bounds. */
    public void stop () {
        setState(STOPPED);
    }

    /** Aborts any active movement of this flicker. Zeroes out velocity and resets state to stopped.
      * This differs from {@link #stop} in that it does not clamp the flicker's position back into
      * {@code min/max}, so it should only be used if you plan to subsequently manually adjust the
      * position to reflect valid values. Otherwise you may freeze the flicker while it's in a
      * rebound state and not inside its normal bounds. */
    public void freeze () {
        // don't use setState as we don't want STOPPED to clamp our position into min/max
        _vel = 0;
        _state = STOPPED;
    }

    @Override public void onStart (Pointer.Interaction iact) {
        if (!enabled.get()) return;

        _vel = 0;
        _maxDelta = 0;
        _minFlickExceeded = false;
        _origPos = position;
        _start = _prev = _cur = getPosition(iact.event);
        _prevStamp = 0;
        _curStamp = iact.event.time;
        setState(DRAGGING);
    }

    @Override public void onDrag (Pointer.Interaction iact) {
        // check whether we are processing this interaction
        if (_state != DRAGGING) return;

        _prev = _cur;
        _prevStamp = _curStamp;
        _cur = getPosition(iact.event);
        _curStamp = iact.event.time;

        // update our position based on the drag delta
        float delta = _cur - _start;
        setPosition(_origPos + delta);

        // if we're not allowed to rebound, clamp the position to our bounds
        if (!allowRebound()) setPosition(MathUtil.clamp(position, min, max));
        // otherwise if we're exceeding min/max then only use a fraction of the delta
        else if (position < min) position += (min-position)*overFraction();
        else if (position > max) position -= (position-max)*overFraction();

        float absDelta = Math.abs(delta);
        if (!_minFlickExceeded && absDelta > minFlickDelta()) {
            _minFlickExceeded = true;
            minFlickExceeded(iact);
        }
        _maxDelta = Math.max(absDelta, _maxDelta);
    }

    @Override public void onEnd (Pointer.Interaction iact) {
        // check whether we are processing this interaction
        if (_state != DRAGGING) return;

        // check whether we should call onClick
        if (_maxDelta < maxClickDelta()) {
            clicked.emit(iact);
            setState(STOPPED);
        }
        // if not, determine whether we should impart velocity to the tower
        else {
            float dragTime = (float)(_curStamp - _prevStamp);
            float delta = _cur - _prev;
            float signum = Math.signum(delta);
            float dragVel = Math.abs(delta) / dragTime;
            // if we're outside our bounds, go immediately into easeback mode
            if (position < min || position > max) setState(EASEBACK);
            // otherwise potentially initiate a flick
            else if (dragVel > flickVelThresh() && _minFlickExceeded) {
                _vel = signum * Math.min(maxFlickVel(), dragVel * flickXfer());
                _accel = -signum * friction();
                setState(SCROLLING);
            }
            else setState(STOPPED);
        }
    }

    protected void onPaint (Clock clock) {
        int dt = clock.dt;
        // update our position based on our velocity
        if (_vel != 0) setPosition(position + _vel * dt);
        // let our state handle additional updates
        _state.paint(dt);
    }

    protected final void setPosition (float position) {
        if (position != this.position) {
            this.position = position;
            changed.emit(this);
        }
    }

    /**
     * Extracts the desired position from the pointer event. The default is to use the y-position.
     */
    protected float getPosition (Pointer.Event event) {
        return event.y();
    }

    /**
     * Returns the deceleration (in pixels per ms per ms) applied to non-zero velocity.
     */
    protected float friction () {
        return 0.0015f;
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
        return 0.9f;
    }

    /**
     * Returns the maximum flick velocity that will be transfered to the entity; limits the actual
     * flick velocity at time of release. This value is not adjusted by {@link #flickXfer}.
     */
    protected float maxFlickVel () {
        return 1.4f; // pixels/ms
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

    /**
     * A method called as soon as the minimum flick distance is exceeded.
     * @param iact the pointer interaction being processed at the time we detected this state.
     */
    protected void minFlickExceeded (Pointer.Interaction iact) {
        iact.capture(); // capture the interaction by default
    }

    /**
     * Determines whether or not the flicker is allowed to scroll past its limits and rebound in a
     * bouncily physical manner (ala iOS). If this is enabled, the flicker position may be
     * temporarily less than {@link #min} or greater than {@link #max} while it is rebounding. The
     * user will also be allowed to drag the flicker past the edge up to {@link #overFraction}
     * times the height of the screen.
     */
    protected boolean allowRebound () {
        return true;
    }

    /**
     * The fraction of the drag distance to use when we've dragged beyond our minimum or maximum
     * value. The default value is {@code 0.5} which seems to be what most inertial scrolling code
     * uses, but you can use an even smaller fraction if you don't want the user to expose so much
     * of your "off-screen" area. If rebounding is disabled, this value is not used and dragging
     * beyond the edges is disallowed.
     */
    protected float overFraction () {
        return 0.5f;
    }

    /**
     * The duration (in milliseconds) over which to animate our ease back to the edge.
     */
    protected float easebackTime () {
        return 500;
    }

    /**
     * Controls the tightness of the deceleration when we're decelerating after scrolling beyond
     * our minimum or maximum value. Default is 5, smaller values result in tighter snapback.
     */
    protected float decelerateSnap () {
        return 5;
    }

    protected void setState (State state) {
        _state = state;
        state.becameActive();
    }

    protected abstract class State {
        public void becameActive () {}
        public void paint (float dt) {}
    }

    protected final State DRAGGING = new State() {
        // nada
    };

    protected final State SCROLLING = new State() {
        @Override public void paint (float dt) {
            // update our velocity based on the current (friction) acceleration
            float prevVel = _vel;
            _vel += _accel * dt;

            // if we decelerate to (or rather slightly through) zero velocity, stop
            if (Math.signum(prevVel) != Math.signum(_vel)) setState(STOPPED);
            // otherwise, if we move past the edge of our bounds, either stop if rebound is
            // disallowed or go into decelerate mode if rebound is allowed
            else if (position < min || position > max) setState(
                allowRebound() ? DECELERATE : STOPPED);
        }

        @Override public String toString () { return "SCROLLING"; }
    };

    protected final State DECELERATE = new State() {
        @Override public void paint (float dt) {
            // update our acceleration based on the pixel distance back to the edge
            float retpix = (position < min) ? (min - position) : (max - position);
            _accel = retpix / (1000 * decelerateSnap());

            // now update our velocity based on this one
            float prevVel = _vel;
            _vel += _accel * dt;

            // once we decelerate to zero, switch to snapback mode
            if (Math.signum(prevVel) != Math.signum(_vel)) setState(SNAPBACK);
        }

        @Override public String toString () { return "DECELERATE"; }
    };

    protected final State SNAPBACK = new State() {
        @Override public void becameActive () {
            _vel = 0;
            _snapdist = (position < min) ? (min - position) : (max - position);
        }

        @Override public void paint (float dt) {
            // if we're in the first 30% of the snapback, accelerate, otherwise switch to easeback
            float retpix = (position < min) ? (min - position) : (max - position);
            float retpct = retpix / _snapdist;
            if (retpct > 0.7f) _vel += _accel * dt;
            else setState(EASEBACK);
        }

        @Override public String toString () { return "SNAPBACK"; }

        protected float _snapdist;
    };

    protected final State EASEBACK = new State() {
        @Override public void becameActive () {
            _vel = 0; // we animate based on timestamps now
            _spos = position;
            _delta = 0;
            _time = easebackTime();
        }

        @Override public void paint (float dt) {
            // from here we just interpolate to our final position
            _delta += dt;
            float target = (position <= min) ? min : max;
            if (_delta > _time) {
                setPosition(target);
                setState(STOPPED);
            } else {
                setPosition(Interpolator.EASE_OUT.apply(_spos, target-_spos, _delta, _time));
            }
        }

        @Override public String toString () { return "EASEBACK"; }

        protected float _time, _spos, _delta;
    };

    protected final State STOPPED = new State() {
        @Override public void becameActive () {
            setPosition(MathUtil.clamp(position, min, max));
            _vel = 0;
        }

        @Override public String toString () { return "STOPPED"; }
    };

    protected State _state = STOPPED;
    protected float _vel, _accel;
    protected float _origPos, _start, _cur, _prev;
    protected double _curStamp, _prevStamp;
    protected float _maxDelta;
    protected boolean _minFlickExceeded;
}
