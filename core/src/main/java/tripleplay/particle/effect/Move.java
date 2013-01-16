//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle.effect;

import tripleplay.particle.Effector;
import static tripleplay.particle.ParticleBuffer.*;

/**
 * Moves particles based on their velocity.
 */
public class Move extends Effector
{
    @Override public void apply (int index, float[] data, int start, float now, float dt) {
        data[start + TX] += data[start + VEL_X] * dt;
        data[start + TY] += data[start + VEL_Y] * dt;
    }
}
