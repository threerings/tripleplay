//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.FloatMath;

import forplay.core.GroupLayer;
import forplay.core.Layer;

/**
 * Handles creation and management of animations. Animations may involve the tweening of a
 * geometric property of a layer (x, y, rotation, scale, alpha), or simple delays, or performing
 * actions. Animations can also be sequenced to orchestrate complex correlated actions.
 */
public abstract class Animator
{
    /**
     * Creates an instance of an animator. The caller is responsible for calling {@link #update} on
     * said animator to drive the animation process.
     */
    public static Animator create () {
        return new Impl();
    }

    /**
     * Registers an animation with this animator. It will be started on the next frame and continue
     * until cancelled or it reports that it has completed.
     */
    public abstract <T extends Animation> T add (T tween);

    /**
     * Starts a tween on the supplied layer's x/y-translation.
     */
    public Animation.Two tweenTranslation (Layer layer) {
        return tweenXY(layer);
    }

    /**
     * Starts a tween on the supplied layer's x/y-translation.
     */
    public Animation.Two tweenXY (Layer layer) {
        return add(new Animation.Two(onX(layer), onY(layer)));
    }

    /**
     * Starts a tween on the supplied layer's x-translation.
     */
    public Animation.One tweenX (Layer layer) {
        return add(new Animation.One(onX(layer)));
    }

    /**
     * Starts a tween on the supplied layer's y-translation.
     */
    public Animation.One tweenY (Layer layer) {
        return add(new Animation.One(onY(layer)));
    }

    /**
     * Starts a tween on the supplied layer's rotation.
     */
    public Animation.One tweenRotation (final Layer layer) {
        return add(new Animation.One(new Animation.Value() {
            public float get () {
                float m01 = layer.transform().m01();
                float m11 = layer.transform().m11();
                float rot = FloatMath.atan2(m01, m11);
                // avoid returning +PI sometimes and -PI others
                if (rot == FloatMath.PI) rot = -FloatMath.PI;
                return rot;
            }
            public void set (float value) {
                layer.setRotation(value);
            }
        }));
    }

    /**
     * Starts a tween on the supplied layer's x/y-scale.
     */
    public Animation.One tweenScale (final Layer layer) {
        return add(new Animation.One(new Animation.Value() {
            public float get () {
                return layer.transform().m00(); // assume x and y scale are equal
            }
            public void set (float value) {
                layer.setScale(value);
            }
        }));
    }

    /**
     * Starts a tween on the supplied layer's x/y-scale.
     */
    public Animation.Two tweenScaleXY (Layer layer) {
        return add(new Animation.Two(onScaleX(layer), onScaleY(layer)));
    }

    /**
     * Starts a tween on the supplied layer's x-scale.
     */
    public Animation.One tweenScaleX (Layer layer) {
        return add(new Animation.One(onScaleX(layer)));
    }

    /**
     * Starts a tween on the supplied layer's y-scale.
     */
    public Animation.One tweenScaleY (Layer layer) {
        return add(new Animation.One(onScaleY(layer)));
    }

    /**
     * Starts a tween on the supplied layer's transparency.
     */
    public Animation.One tweenAlpha (final Layer layer) {
        return add(new Animation.One(new Animation.Value() {
            public float get () {
                return layer.alpha();
            }
            public void set (float value) {
                layer.setAlpha(value);
            }
        }));
    }

    /**
     * Creates an animation that delays for the specified number of seconds.
     */
    public Animation.Delay delay (float seconds)
    {
        return add(new Animation.Delay(seconds));
    }

    /**
     * Returns an animator which can be used to construct an animation that will be repeated until
     * the supplied layer has been removed from its parent. The layer must be added to a parent
     * before the next frame (if it's not already), or the cancellation will trigger immediately.
     */
    public Animator repeat (Layer layer)
    {
        return add(new Animation.Repeat(layer)).then();
    }

    /**
     * Creates an animation that executes the supplied runnable and immediately completes.
     */
    public Animation.Action action (Runnable action)
    {
        return add(new Animation.Action(action));
    }

    /**
     * Adds the supplied child to the supplied parent. This is generally done as the beginning of a
     * chain of animations, which itself may be delayed or subject to animation barriers.
     */
    public Animation.Action add (final GroupLayer parent, final Layer child) {
        return action(new Runnable() {
            public void run () {
                parent.add(child);
            }
        });
    }

    /**
     * Destroys the specified layer. This is generally done as the end of a chain of animations,
     * which culminate in the removal (destruction) of the target layer.
     */
    public Animation.Action destroy (final Layer layer) {
        return action(new Runnable() {
            public void run () {
                layer.destroy();
            }
        });
    }

    /**
     * Causes this animator to delay the start of any subsequently registered animations until all
     * currently registered animations are complete.
     */
    public void addBarrier () {
        addBarrier(0);
    }

    /**
     * Causes this animator to delay the start of any subsequently registered animations until the
     * specified delay has elapsed <em>after this barrier becomes active</em>. Any previously
     * registered barriers must first expire and this barrier must move to the head of the list
     * before its delay timer will be started. This is probably what you want.
     */
    public void addBarrier (float delay) {
        throw new UnsupportedOperationException(
            "Barriers are only supported on the top-level animator.");
    }

    /**
     * Performs per-frame animation processing.
     */
    public void update (float time) {
        // nada by default
    }

    protected static Animation.Value onX (final Layer layer) {
        return new Animation.Value() {
            public float get () {
                return layer.transform().tx();
            }
            public void set (float value) {
                layer.transform().setTx(value);
            }
        };
    }

    protected static Animation.Value onY (final Layer layer) {
        return new Animation.Value() {
            public float get () {
                return layer.transform().ty();
            }
            public void set (float value) {
                layer.transform().setTy(value);
            }
        };
    }

    protected static Animation.Value onScaleX (final Layer layer) {
        return new Animation.Value() {
            public float get () {
                float m00 = layer.transform().m00();
                float m01 = layer.transform().m01();
                return FloatMath.sqrt(m00*m00 + m01*m01);
            }
            public void set (float value) {
                layer.transform().setM00(value);
            }
        };
    }

    protected static Animation.Value onScaleY (final Layer layer) {
        return new Animation.Value() {
            public float get () {
                float m10 = layer.transform().m10();
                float m11 = layer.transform().m11();
                return FloatMath.sqrt(m10*m10 + m11*m11);
            }
            public void set (float value) {
                layer.transform().setM11(value);
            }
        };
    }

    /** Implementation details, avert your eyes. */
    protected static class Impl extends Animator {
        @Override public <T extends Animation> T add (T anim) {
            _accum.add(anim);
            return anim;
        }

        @Override public void addBarrier (float delay) {
            Barrier barrier = new Barrier(delay);
            _barriers.add(barrier);
            // pushing a barrier causes subsequent animations to be accumulated separately
            _accum = barrier.accum;
        }

        @Override public void update (float time) {
            // if we have any animations queued up to be added, add those now
            if (!_nanims.isEmpty()) {
                for (int ii = 0, ll = _nanims.size(); ii < ll; ii++) {
                    _nanims.get(ii).init(time);
                }
                _anims.addAll(_nanims);
                _nanims.clear();
            }

            // now process all of our registered animations
            for (int ii = 0, ll = _anims.size(); ii < ll; ii++) {
                if (_anims.get(ii).apply(this, time)) {
                    _anims.remove(ii--);
                    ll -= 1;
                }
            }

            // if we have no active animations, or a timed barrier has expired, unblock a barrier
            boolean noActiveAnims = _anims.isEmpty() && _nanims.isEmpty();
            if (!_barriers.isEmpty() && (noActiveAnims || _barriers.get(0).expired(time))) {
                Barrier barrier = _barriers.remove(0);
                _nanims.addAll(barrier.accum);
                // if we just unblocked the last barrier, start accumulating back on _nanims
                if (_barriers.isEmpty()) {
                    _accum = _nanims;
                }
            }
        }

        protected List<Animation> _anims = new ArrayList<Animation>();
        protected List<Animation> _nanims = new ArrayList<Animation>();
        protected List<Animation> _accum = _nanims;
        protected List<Barrier> _barriers = new ArrayList<Barrier>();
    }

    /** Implementation details, avert your eyes. */
    protected static class Barrier {
        public List<Animation> accum = new ArrayList<Animation>();
        public float expireDelay;
        public float absoluteExpireTime;

        public Barrier (float expireDelay) {
            this.expireDelay = expireDelay;
        }

        public boolean expired (float time) {
            if (expireDelay == 0) return false;
            if (absoluteExpireTime == 0) absoluteExpireTime = time + expireDelay;
            return time > absoluteExpireTime;
        }
    }
}
