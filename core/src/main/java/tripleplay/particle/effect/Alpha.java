//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle.effect;

import tripleplay.particle.Effector;
import tripleplay.util.Interpolator;
import static tripleplay.particle.ParticleBuffer.*;

/**
 * Adjust the alpha value of a particle.
 */
public class Alpha
{
    public static Effector fade (Interpolator interp, float duration) {
        return null; // TODO
    }

    public static Effector byAge (final Interpolator interp, final float startAlpha, float endAlpha) {
        final float rangeAlpha = endAlpha - startAlpha;
        return new Effector() {
            @Override public void apply (int index, float[] data, int start, float now, float dt) {
                data[start + ALPHA] = interp.apply(
                    startAlpha, rangeAlpha, now - data[start + BIRTH], data[start + LIFESPAN]);
            }
        };
    }
}
