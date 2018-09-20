//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import pythagoras.f.MathUtil;

/**
 * Abstracts the process of interpolation between two values.
 */
public abstract class Interpolator
{
    /** An interpolator that always returns the starting position. */
    public static Interpolator NOOP = new Interpolator() {
        @Override public String toString () { return "NOOP"; }
        @Override public float apply (float v) {
            return 0;
        }
    };

    /** A linear interpolator. */
    public static Interpolator LINEAR = new Interpolator() {
        @Override public String toString () { return "LINEAR"; }
        @Override public float apply (float v) {
            return v;
        }
    };

    /** An interpolator that starts to change slowly and ramps up to full speed. */
    public static Interpolator EASE_IN = new Interpolator() {
        @Override public String toString () { return "EASE_IN"; }
        @Override public float apply (float v) {
            return v * v * v;
        }
    };

    /** An interpolator that starts to change quickly and eases into the final value. */
    public static Interpolator EASE_OUT = new Interpolator() {
        @Override public String toString () { return "EASE_OUT"; }
        @Override public float apply (float v) {
            float vv = v - 1;
            return (1 + vv * vv * vv);
        }
    };

    /** An interpolator that eases away from the starting value, speeds up, then eases into the
     * final value. */
    public static Interpolator EASE_INOUT = new Interpolator() {
        @Override public String toString () { return "EASE_INOUT"; }
        @Override public float apply (float v) {
            float v2 = 2*v;
            if (v2 < 1) {
                return (v2 * v2 * v2)/2;
            }
            float ov = v2 - 2;
            return (2 + ov * ov * ov)/2;
        }
    };

    /** An interpolator that undershoots the starting value, then speeds up into the final value */
    public static Interpolator EASE_IN_BACK = new Interpolator() {
        @Override public String toString () { return "EASE_IN_BACK"; }
        @Override public float apply (float v) {
            float curvature = 1.70158f;
            return v * v * ((curvature+1) * v - curvature);
        }
    };

    /** An interpolator that eases into the final value and overshoots it before settling on it. */
    public static Interpolator EASE_OUT_BACK = new Interpolator() {
        @Override public String toString () { return "EASE_OUT_BACK"; }
        @Override public float apply (float v) {
            float curvature = 1.70158f, v1 = v - 1;
            return (v1 * v1 * ((curvature+1) * v1 + curvature) + 1);
        }
    };

    public static Interpolator BOUNCE_OUT = new Interpolator() {
        @Override public String toString () { return "BOUNCE_OUT"; }
        @Override public float apply (float v) {
            if (v < (1/2.75f)) {
                return 7.5625f * v * v;
            } else if (v < (2/2.75f)) {
                float vBounce = v - (1.5f/2.75f);
                return 7.5625f * vBounce * vBounce + 0.75f;
            } else if (v < (2.5/2.75)) {
                float vBounce = v - (2.25f/2.75f);
                return 7.5625f * vBounce * vBounce + 0.9375f;
            } else {
                float vBounce = v - (2.625f/2.75f);
                return 7.5625f * vBounce * vBounce + 0.984375f;
            }
        }
    };

    /** An interpolator that eases past the final value then back towards it elastically. */
    public static Interpolator EASE_OUT_ELASTIC = new Interpolator() {
        @Override public String toString () { return "EASE_OUT_ELASTIC"; }
        @Override public float apply (float v) {
            return (float)Math.pow(2, -10 * v) * (float)Math.sin((v - K) * J) + 1;
        }
        private static final float K = 0.3f/4;
        private static final float J = (float)(2*Math.PI/0.3);
    };

    /**
     * Interpolates between zero and one according to this interpolator's function.
     *
     * @param v a value between zero and one (usually {@code elapsed/total} time).
     */
    public abstract float apply (float v);

    /**
     * Interpolates between zero and one according to this interpolator's function.
     *
     * @param dt the amount of time that has elapsed.
     * @param t the total amount of time for the interpolation. If t == 0, the result is undefined.
     */
    public float apply (float dt, float t) {
        return apply(dt/t);
    }

    /**
     * Interpolates between two values, as in {@link #apply(float,float)} except that {@code dt} is
     * clamped to [0..t] to avoid interpolation weirdness if {@code dt} is ever negative or exceeds
     * {@code t}.
     */
    public float applyClamp (float dt, float t) {
        return apply((dt < 0) ? 0 : (dt > 1 ? 1 : dt), t);
    }

    /**
     * Interpolates between two values.
     *
     * @param start the starting value.
     * @param range the difference between the ending value and the starting value.
     * @param dt the amount of time that has elapsed.
     * @param t the total amount of time for the interpolation. If t == 0, start+range will be
     * returned.
     */
    public float apply (float start, float range, float dt, float t) {
        float pos = (t == 0) ? 1 : apply(dt, t);
        return start + range * pos;
    }

    /**
     * Interpolates between two values, as in {@link #apply} except that {@code dt} is clamped to
     * [0..t] to avoid interpolation weirdness if {@code dt} is ever negative or exceeds {@code t}.
     */
    public float applyClamp (float start, float range, float dt, float t) {
        return apply(start, range, MathUtil.clamp(dt, 0, t), t);
    }
}
