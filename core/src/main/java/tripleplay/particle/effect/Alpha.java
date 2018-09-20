//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle.effect;

import playn.core.Color;

import tripleplay.particle.Effector;
import tripleplay.util.Interpolator;
import static tripleplay.particle.ParticleBuffer.*;

/**
 * Adjust the alpha value of a particle.
 */
public class Alpha
{
    /**
     * Returns an effector that updates the particle's alpha based on its age, as adjusted by the
     * supplied interpolator. The particle will be faded from alpha of one to zero.
     */
    public static Effector byAge (Interpolator interp) {
        return byAge(interp, 1, 0);
    }

    /**
     * Returns an effector that updates the particle's alpha based on its age, as adjusted by the
     * supplied interpolator. In general you'd use {@code startAlpha} of 1 and {@code endAlpha} of
     * 0, but if you are doing uncommon things, you might use different values.
     */
    public static Effector byAge (final Interpolator interp,
                                  final float startAlpha, float endAlpha) {
        final float rangeAlpha = endAlpha - startAlpha;
        return new Effector() {
            @Override public void apply (int index, float[] data, int start, float now, float dt) {
                float alpha = interp.apply(
                    startAlpha, rangeAlpha, now - data[start + BIRTH], data[start + LIFESPAN]);
                float red = Color.decodeLower(data[start + ALPHA_RED]);
                data[start + ALPHA_RED] = Color.encode(alpha, red);
            }
        };
    }
}
