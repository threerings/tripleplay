//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.anim;

import java.util.ArrayList;
import java.util.List;

import forplay.core.Layer;

import com.threerings.util.FloatMath;

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
            _nanims.add(anim);
            return anim;
        }

        @Override public void update (float time) {
            if (!_nanims.isEmpty()) {
                for (int ii = 0, ll = _nanims.size(); ii < ll; ii++) {
                    _nanims.get(ii).init(time);
                }
                _anims.addAll(_nanims);
                _nanims.clear();
            }
            for (int ii = 0, ll = _anims.size(); ii < ll; ii++) {
                if (_anims.get(ii).apply(this, time)) {
                    _anims.remove(ii--);
                    ll -= 1;
                }
            }
        }

        protected List<Animation> _anims = new ArrayList<Animation>();
        protected List<Animation> _nanims = new ArrayList<Animation>();
    }
}
