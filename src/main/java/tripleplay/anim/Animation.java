//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import playn.core.Layer;

import tripleplay.util.Interpolator;

/**
 * Represents a single component of an animation.
 */
public abstract class Animation
{
    /** Used by animations to update a target value. */
    public interface Value {
        /** Returns the initial value. */
        float initial ();

        /** Updates the value. */
        void set (float value);
    }

    /** A base class for animations that interpolate values. */
    public static abstract class Interped<R> extends Animation {
        /** Uses the supplied interpolator for this animation. */
        public R using (Interpolator interp) {
            _interp = interp;
            @SuppressWarnings("unchecked") R tthis = (R)this;
            return tthis;
        }

        /** Uses a linear interpolator for this animation. */
        public R linear () {
            return using(Interpolator.LINEAR);
        }

        /** Uses an ease-in interpolator for this animation. */
        public R easeIn () {
            return using(Interpolator.EASE_IN);
        }

        /** Uses an ease-out interpolator for this animation. */
        public R easeOut () {
            return using(Interpolator.EASE_OUT);
        }

        /** Uses an ease-inout interpolator for this animation. */
        public R easeInOut () {
            return using(Interpolator.EASE_INOUT);
        }

        /** Configures the duration for this animation (in seconds). Default: 1. */
        public R in (float duration) {
            _duration = duration;
            @SuppressWarnings("unchecked") R tthis = (R)this;
            return tthis;
        }

        protected Interpolator _interp = Interpolator.LINEAR;
        protected float _duration = 1;
    }

    /** An animation that animates a single scalar value. */
    public static class One extends Interped<One> {
        public One (Value target) {
            _target = target;
        }

        /** Configures the starting value. Default: the value of the scalar at the time that the
         * animation begins. */
        public One from (float value) {
            _from = value;
            return this;
        }

        /** Configures the ending value. Default: 0. */
        public One to (float value) {
            _to = value;
            return this;
        }

        @Override
        protected void init (float time) {
            super.init(time);
            if (_from == Float.MIN_VALUE) _from = _target.initial();
        }

        @Override
        protected float apply (float time) {
            float dt = time-_start;
            _target.set((dt < _duration) ? _interp.apply(_from, _to-_from, dt, _duration) : _to);
            return _duration - dt;
        }

        @Override public String toString () {
            return getClass().getName() + " start:" + _start + " to " + _to;
        }

        protected final Value _target;
        protected float _from = Float.MIN_VALUE;
        protected float _to;
    }

    /** An animation that animates a pair of scalar values (usually a position). */
    public static class Two extends Interped<Two> {
        public Two (Value x, Value y) {
            _x = x;
            _y = y;
        }

        /** Configures the starting values. Default: the values of the scalar at the time that the
         * animation begins. */
        public Two from (float fromx, float fromy) {
            _fromx = fromx;
            _fromy = fromy;
            return this;
        }

        /** Configures the ending values. Default: (0, 0). */
        public Two to (float tox, float toy) {
            _tox = tox;
            _toy = toy;
            return this;
        }

        @Override
        protected void init (float time) {
            super.init(time);
            if (_fromx == Float.MIN_VALUE) _fromx = _x.initial();
            if (_fromy == Float.MIN_VALUE) _fromy = _y.initial();
        }

        @Override
        protected float apply (float time) {
            float dt = time-_start;
            if (dt < _duration) {
                _x.set(_interp.apply(_fromx, _tox-_fromx, dt, _duration));
                _y.set(_interp.apply(_fromy, _toy-_fromy, dt, _duration));
            } else {
                _x.set(_tox);
                _y.set(_toy);
            }
            return _duration - dt;
        }

        protected final Value _x, _y;
        protected float _fromx = Float.MIN_VALUE, _fromy = Float.MIN_VALUE;
        protected float _tox, _toy;
    }

    /** An animation that simply delays a specified number of seconds. */
    public static class Delay extends Animation {
        public Delay (float duration) {
            _duration = duration;
        }

        @Override
        protected float apply (float time) {
            return _start + _duration - time;
        }

        protected final float _duration;
    }

    /** An animation that executes an action and completes immediately. */
    public static class Action extends Animation {
        public Action (Runnable action) {
            _action = action;
        }

        @Override
        protected float apply (float time) {
            _action.run();
            return 0;
        }

        protected Runnable _action;
    }

    /** An animation that repeats its underlying animation over and over again (until removed). */
    public static class Repeat extends Animation {
        public Repeat (Layer layer) {
            _layer = layer;
        }

        @Override
        protected void init (float time) {
            // a normal animation will have _current initialized to itself; we want to skip
            // ourselves and go right to our to-be-repeated animation, and initialize it
            // immediately
            _current = _next;
            _current.init(time);
        }

        @Override
        protected float apply (float time) {
            return 0; // not used
        }

        @Override
        protected float apply (Animator animator, float time) {
            // if our current chain of animations is still running, keep going
            float remain = super.apply(animator, time);
            if (remain > 0) return remain;

            // if our target layer is no longer active, we're done
            if (_layer.parent() == null) return 0;

            // otherwise, reset to the head of the chain and keep going
            _current = _next;
            _current.init(time+remain);
            return 1; // return > zero remaining time to indicate that we're not done
        }

        protected Layer _layer;
    }

    /**
     * Returns an animation factory for constructing an animation that will be queued up for
     * execution when the current animation is completes.
     */
    public Animator then () {
        if (_next != null) {
            throw new IllegalStateException("This animation already has a 'then' animation.");
        }
        return new Animator() {
            @Override public <T extends Animation> T add (T anim) {
                _next = anim;
                return anim;
            }
        };
    }

    /**
     * Cancels this animation. It will remove itself from its animator the next frame.
     * @return true if this animation was actually running and was canceled, false if it had
     * already completed.
     */
    public boolean cancel () {
        if (_current != null) {
            _current = null;
            return true;
        } else {
            return false;
        }
    }

    protected Animation () {
    }

    protected void init (float time) {
        _start = time;
    }

    protected float apply (Animator animator, float time) {
        // if we're cancelled, abandon ship now
        if (_current == null) return 0;

        // if the current animation has completed, move the next one in our chain
        float remain = _current.apply(time);
        if (remain > 0) return remain;

        // if we have no next animation, return our overflow
        _current = _current._next;
        if (_current == null) return remain;

        // otherwise init our next animation (accounting for overflow)
        _current.init(time+remain);
        return 1; // return > zero to indicate that we're not done
    }

    protected abstract float apply (float time);

    @Override public String toString () {
        return getClass().getName() + " start:" + _start;
    }

    protected float _start;
    protected Animation _current = this;
    protected Animation _next;
}
