//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle.effect;

import tripleplay.particle.Effector;
import static tripleplay.particle.ParticleBuffer.*;

/**
 * Applies friction (drag) to particles.
 */
public class Drag extends Effector
{
    /**
     * Creates a drag effector with uniform x and y drag.
     *
     * @param drag the fraction of the velocity to preserve on each frame (0 to 1).
     */
    public Drag (float drag) {
        this(drag, drag);
    }

    /**
     * Creates a drag effector with the specified x and y drag.
     *
     * @param dragX the fraction of the x velocity to preserve on each frame (0 to 1).
     * @param dragY the fraction of the y velocity to preserve on each frame (0 to 1).
     */
    public Drag (float dragX, float dragY) {
        _dragX = dragX;
        _dragY = dragY;
    }

    @Override public void apply (int index, float[] data, int start, float now, float dt) {
        data[start + VEL_X] *= _dragX;
        data[start + VEL_Y] *= _dragY;
    }

    protected final float _dragX, _dragY;
}
