//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;

import playn.core.Pointer;
import pythagoras.f.IPoint;
import pythagoras.f.MathUtil;
import pythagoras.f.Point;
import react.Signal;

/**
 * Translates pointer input on a layer into an x, y offset. With a sufficiently large drag delta,
 * calculates a velocity, applies it to the position over time and diminishes its value by
 * friction. For smaller drag deltas, dispatches the pointer end event on the {@link #clicked}
 * signal.
 *
 * <p><b>NOTE:</b>Clients of this class must call {@link #update(float)}, so that friction and
 * other calculations can be applied. This is normally done within the client's own update method
 * and followed by some usage of the {@link #position()} method. For example:
 *
 * <pre>{@code
 *    XYFlicker flicker = new XYFlicker();
 *    Layer layer = ...;
 *    { layer.addListener(flicker); }
 *    void update (int delta) {
 *        flicker.update(delta);
 *        layer.setTranslation(flicker.position().x(), flicker.position().y());
 *    }
 * }</pre></p>
 *
 * TODO: figure out how to implement with two Flickers. could require some changes therein since
 * you probably don't want them to have differing states, plus 2x clicked signals is wasteful
 */
public class XYFlicker implements Pointer.Listener
{
    /** Signal dispatched when a pointer usage did not end up being a flick. */
    public Signal<Pointer.Event> clicked = Signal.create();

    /**
     * Gets the current position.
     */
    public IPoint position () {
        return _position;
    }

    @Override public void onPointerStart (Pointer.Event event) {
        _vel.set(0, 0);
        _maxDeltaSq = 0;
        _origPos.set(_position);
        getPosition(event, _start);
        _prev.set(_start);
        _cur.set(_start);
        _prevStamp = 0;
        _curStamp = event.time();
    }

    @Override public void onPointerDrag (Pointer.Event event) {
        _prev.set(_cur);
        _prevStamp = _curStamp;
        getPosition(event, _cur);
        _curStamp = event.time();
        float dx = _cur.x - _start.x, dy = _cur.y - _start.y;
        setPosition(_origPos.x + dx, _origPos.y + dy);
        _maxDeltaSq = Math.max(dx * dx + dy * dy, _maxDeltaSq);

        // for the purposes of capturing the event stream, dx and dy are capped by their ranges
        dx = _position.x - _origPos.x;
        dy = _position.y - _origPos.y;
        if (dx * dx + dy * dy >= maxClickDeltaSq()) {
            event.capture();
        }
    }

    @Override public void onPointerEnd (Pointer.Event event) {
        // just dispatch a click if the pointer didn't move very far
        if (_maxDeltaSq < maxClickDeltaSq()) {
            clicked.emit(event);
            return;
        }
        // if not, maybe impart some velocity
        float dragTime = (float)(_curStamp - _prevStamp);
        Point delta = new Point(_cur.x - _prev.x, _cur.y - _prev.y);
        Point dragVel = delta.mult(1 / dragTime);
        float dragSpeed = dragVel.distance(0, 0);
        if (dragSpeed > flickSpeedThresh() && delta.distance(0, 0) > minFlickDelta()) {
            if (dragSpeed > maxFlickSpeed()) {
                dragVel.multLocal(maxFlickSpeed() / dragSpeed);
                dragSpeed = maxFlickSpeed();
            }
            _vel.set(dragVel);
            _vel.multLocal(flickXfer());
            float sx = Math.signum(_vel.x), sy = Math.signum(_vel.y);
            _accel.x = -sx * friction();
            _accel.y = -sy * friction();
        }
    }

    @Override public void onPointerCancel (Pointer.Event event) {
        _vel.set(0, 0);
        _accel.set(0, 0);
    }

    public void update (float delta) {
        if (_vel.x == 0 && _vel.y == 0) return;

        _prev.set(_position);

        // apply x and y velocity
        float x = MathUtil.clamp(_position.x + _vel.x * delta, _min.x, _max.x);
        float y = MathUtil.clamp(_position.y + _vel.y * delta, _min.y, _max.y);

        // stop when we hit the edges
        if (x == _position.x) _vel.x = 0;
        if (y == _position.y) _vel.y = 0;
        _position.set(x, y);

        // apply x and y acceleration
        _vel.x = applyAccelertion(_vel.x, _accel.x, delta);
        _vel.y = applyAccelertion(_vel.y, _accel.y, delta);
    }

    /**
     * Resets the flicker to the given maximum values.
     */
    public void reset (float maxX, float maxY) {
        _max.set(maxX, maxY);

        // reclamp the position
        setPosition(_position.x, _position.y);
    }

    /**
     * Sets the flicker position, in the case of a programmatic change.
     */
    public void positionChanged (float x, float y) {
        setPosition(x, y);
    }

    /** Translates a pointer event into a position. */
    protected void getPosition (Pointer.Event event, Point dest) {
        dest.set(-event.x(), -event.y());
    }

    /** Sets the current position, clamping the values between min and max. */
    protected void setPosition (float x, float y) {
        _position.set(MathUtil.clamp(x, _min.x, _max.x), MathUtil.clamp(y, _min.y, _max.y));
    }

    /** Returns the minimum distance (in pixels) the pointer must have moved to register as a
     * flick. */
    protected float minFlickDelta () {
        return 10;
    }

    /** Returns the deceleration (in pixels per ms per ms) applied to non-zero velocity. */
    protected float friction () {
        return 0.0015f;
    }

    /** Returns the minimum (positive) speed (in pixels per millisecond) at time of touch release
     * required to initiate a flick (i.e. transfer the flick velocity to the entity). */
    protected float flickSpeedThresh () {
        return 0.5f;
    }

    /** Returns the fraction of flick speed that is transfered to the entity (a value between 0
     * and 1). */
    protected float flickXfer () {
        return 0.95f;
    }

    /** Returns the maximum flick speed that will be transfered to the entity; limits the actual
     * flick speed at time of release. This value is not adjusted by {@link #flickXfer}. */
    protected float maxFlickSpeed () {
        return 1.4f; // pixels/ms
    }

    /** Returns the square of the maximum distance (in pixels) the pointer is allowed to travel
     * while pressed and still register as a click. */
    protected float maxClickDeltaSq () {
        return 225;
    }

    protected static float applyAccelertion (float v, float a, float dt) {
        float prev = v;
        v += a * dt;
        // if we decelerate past zero velocity, stop
        return Math.signum(prev) == Math.signum(v) ? v : 0;
    }

    protected float _maxDeltaSq;
    protected final Point _position = new Point();
    protected final Point _vel = new Point(), _accel = new Point(), _origPos = new Point();
    protected final Point _start = new Point(), _cur = new Point(), _prev = new Point();
    protected final Point _max = new Point(), _min = new Point();
    protected double _prevStamp, _curStamp;
}
