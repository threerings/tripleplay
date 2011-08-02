//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

/**
 * Abstracts the process of interpolation between two values.
 */
public abstract class Interpolator
{
    /** An interpolator that always returns the starting position. */
    public static Interpolator NOOP = new Interpolator() {
        @Override public float apply (float start, float range, float dt, float t) {
            return start;
        }
    };

    /** A linear interpolator. */
    public static Interpolator LINEAR = new Interpolator() {
        @Override public float apply (float start, float range, float dt, float t) {
            return start + range * dt / t;
        }
    };

    /** An interpolator that starts to change slowly and ramps up to full speed. */
    public static Interpolator EASE_IN = new Interpolator() {
        @Override public float apply (float start, float range, float dt, float t) {
            float dtt = dt / t;
            return start + range * dtt * dtt * dtt;
        }
    };

    /** An interpolator that starts to change quickly and eases into the final value. */
    public static Interpolator EASE_OUT = new Interpolator() {
        @Override public float apply (float start, float range, float dt, float t) {
            float dtt = dt / t - 1;
            return start + range * (1 + dtt * dtt * dtt);
        }
    };

    /** An interpolator that eases away from the starting value, speeds up, then eases into the
     * final value. */
    public static Interpolator EASE_INOUT = new Interpolator() {
        @Override public float apply (float start, float range, float dt, float t) {
            float hdtt = dt / (t/2);
            if (hdtt < 1) {
                return start + range/2 * hdtt * hdtt * hdtt;
            } else {
                float nhdtt = hdtt - 2;
                return start + range/2 * (2 + nhdtt * nhdtt * nhdtt);
            }
        }
    };

    /**
     * Interpolates between two values.
     *
     * @param start the starting value.
     * @param range the difference between the ending value and the starting value.
     * @param dt the amount of time that has elapsed.
     * @param t the total amount of time for the interpolation.
     */
    public abstract float apply (float start, float range, float dt, float t);
}
