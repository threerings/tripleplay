//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import java.util.Random;

import pythagoras.f.XY;

import playn.core.ImageLayer;
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

    /** Used to cancel animations after they've been started. See {@link #handle}. */
    public interface Handle {
        /** Cancels this animation. It will remove itself from its animator the next frame.
         * @return true if this animation was actually running and was canceled, false if it had
         * already completed. */
        boolean cancel ();
    }

    /** Processes a {@link Flipbook}. */
    public static class Flip extends Animation {
        public Flip (ImageLayer target, Flipbook book) {
            _target = target;
            _book = book;
        }

        @Override
        protected void init (float time) {
            super.init(time);
            setFrame(0);
        }

        protected float apply (float time) {
            float dt = time - _start;
            int newIdx = _curIdx;
            float[] frameEnds = _book.frameEnds;
            float remain = frameEnds[frameEnds.length-1] - dt;
            if (remain < 0) return remain;
            while (frameEnds[newIdx] < dt) newIdx++;
            if (newIdx != _curIdx) setFrame(newIdx);
            return remain;
        }

        protected void setFrame (int idx) {
            _book.frames.apply(_book.frameIndexes[idx], _target);
            _curIdx = idx;
        }

        protected final ImageLayer _target;
        protected final Flipbook _book;
        protected int _curIdx;
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

    /** Animates a single scalar value. */
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

    /** Animates a pair of scalar values (usually a position). */
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

        /** Configures the starting values. Default: the values of the scalar at the time that the
         * animation begins. */
        public Two from (XY pos) {
            return from(pos.x(), pos.y());
        }

        /** Configures the ending values. Default: (0, 0). */
        public Two to (float tox, float toy) {
            _tox = tox;
            _toy = toy;
            return this;
        }

        /** Configures the ending values. Default: (0, 0). */
        public Two to (XY pos) {
            return to(pos.x(), pos.y());
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

    /** Delays a specified number of seconds. */
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

    /** Executes an action and completes immediately. */
    public static class Action extends Animation {
        public Action (Runnable action) {
            _action = action;
        }

        @Override
        protected float apply (float time) {
            _action.run();
            return _start - time;
        }

        protected Runnable _action;
    }

    /** Repeats its underlying animation over and over again (until removed). */
    public static class Repeat extends Animation {
        public Repeat (Layer layer) {
            _layer = layer;
        }

        @Override
        public Animator then () {
            return new Animator() {
                @Override public <T extends Animation> T add (T anim) {
                    // set ourselves as the repeat target of this added animation
                    anim._next = Repeat.this;
                    _next = anim;
                    return anim;
                }
            };
        }

        @Override
        protected float apply (float time) {
            return _start - time; // immediately move to our next animation
        }

        @Override
        protected Animation next () {
            // if our target layer is no longer active, we're done
            return (_layer.parent() == null) ? null : _next;
        }

        protected Layer _layer;
    }

    /** An animation that shakes a layer randomly in the x and y directions. */
    public static class Shake extends Animation.Interped<Shake> {
        public Shake (Layer layer) {
            _layer = layer;
        }

        /** Configures the amount under and over the starting x and y allowed when shaking. The
         * animation will shake the layer in the range {@code x + underX} to {@code x + overX} and
         * similarly for y, thus {@code underX} (and {@code underY}) should be negative. */
        public Shake bounds (float underX, float overX, float underY, float overY) {
            _underX = underX;
            _overX = overX;
            _underY = underY;
            _overY = overY;
            return this;
        }

        /** Configures the shake cycle time in the x and y directions. */
        public Shake cycleTime (float millis) { return cycleTime(millis, millis); }

        /** Configures the shake cycle time in the x and y directions. */
        public Shake cycleTime (float millisX, float millisY) {
            _cycleTimeX = millisX;
            _cycleTimeY = millisY;
            return this;
        }

        @Override
        protected void init (float time) {
            super.init(time);
            _startX = _layer.transform().tx();
            _startY = _layer.transform().ty();

            // start our X/Y shaking randomly in one direction or the other
            _curMinX = _startX;
            if (_overX == 0) _curRangeX = _underX;
            else if (_underX == 0) _curRangeX = _overX;
            else _curRangeX = RANDS.nextBoolean() ?  _overX : _underX;
            _curMinY = _startY;
            if (_overY == 0) _curRangeY = _underY;
            else if (_underY == 0) _curRangeY = _overY;
            else _curRangeY = RANDS.nextBoolean() ? _overY : _underY;

            System.err.println("Start " + _startY);
            System.err.println("Bounds " + _underY + " to " + _overY);
            System.err.println("From " + _curMinY + ", range: " + _curRangeY);
        }

        @Override
        protected float apply (float time) {
            float dt = time-_start, nx, ny;
            if (dt < _duration) {
                float dtx = time-_timeX, dty = time-_timeY;
                if (dtx < _cycleTimeX) nx = _interp.apply(_curMinX, _curRangeX, dtx, _cycleTimeX);
                else {
                    nx = _curMinX + _curRangeX;
                    _curMinX = nx;
                    float rangeX = _startX + (_curRangeX < 0 ?  _overX : _underX) - nx;
                    _curRangeX = rangeX/2 + RANDS.nextFloat() * rangeX/2;
                    _timeX = time;
                }
                if (dty < _cycleTimeY) ny = _interp.apply(_curMinY, _curRangeY, dty, _cycleTimeY);
                else {
                    ny = _curMinY + _curRangeY;
                    _curMinY = ny;
                    float rangeY = _startY + (_curRangeY < 0 ?  _overY : _underY) - ny;
                    _curRangeY = rangeY/2 + RANDS.nextFloat() * rangeY/2;
                    _timeY = time;
                }
            } else {
                nx = _startX;
                ny = _startY;
            }
            _layer.setTranslation(nx, ny);
            return _duration - dt;
        }

        protected final Layer _layer;

        // parameters initialized by setters or in init()
        protected float _underX = -2, _overX = 2, _underY = -2, _overY = 2;
        protected float _cycleTimeX = 100, _cycleTimeY = 100;
        protected float _startX, _startY;

        // parameters used during animation
        protected float _timeX, _timeY;
        protected float _curMinX, _curRangeX, _curMinY, _curRangeY;
    }

    /**
     * Returns an animation factory for constructing an animation that will be queued up for
     * execution when the current animation is completes.
     */
    public Animator then () {
        return new Animator() {
            @Override public <T extends Animation> T add (T anim) {
                // our _next is either null, or it points to the animation to which we should
                // repeat when we reach the end of this chain; so pass the null or the repeat
                // target down to our new next animation
                anim._root = _root;
                anim._next = _next;
                _next = anim;
                return anim;
            }
        };
    }

    /**
     * Returns a handle on this collection of animations which can be used to cancel the animation.
     * This handle references the root animation in this chain of animations, and will cancel all
     * (as yet uncompleted) animations in the chain.
     */
    public Handle handle () {
        return new Handle() {
            @Override public boolean cancel () {
                return _root.cancel();
            }
        };
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

        while (remain <= 0) {
            // if we have no next animation, return our overflow
            _current = _current.next();
            if (_current == null) return remain;

            // otherwise init and apply our next animation (accounting for overflow)
            _current.init(time+remain);
            remain = _current.apply(time);
        }
        return remain;
    }

    protected boolean cancel () {
        if (_current == null) return false;
        _current = null;
        return true;
    }

    protected abstract float apply (float time);

    protected Animation next () {
        return _next;
    }

    @Override public String toString () {
        return getClass().getName() + " start:" + _start;
    }

    protected float _start;
    protected Animation _root = this;
    protected Animation _current = this;
    protected Animation _next;

    protected static final Random RANDS = new Random();
}
