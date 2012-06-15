//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import java.util.List;

import pythagoras.f.FloatMath;

import playn.core.Image;
import playn.core.Surface;
import playn.core.gl.GLShader;

/**
 * Contains the basic metadata for an array of particles: position, velocity, scale, rotation,
 * birth time, lifespan.
 */
public class ParticleBuffer
{
    /** The offset of the x coordinate of the position in the particle buffer. */
    public static final int POS_X = 0;
    /** The offset of the y coordinate of the position in the particle buffer. */
    public static final int POS_Y = 1;

    /** The offset of the x coordinate of the velocity in the particle buffer. */
    public static final int VEL_X = 2;
    /** The offset of the y coordinate of the velocity in the particle buffer. */
    public static final int VEL_Y = 3;

    /** The offset of the (uniform) scale in the particle buffer. */
    public static final int SCALE = 4;
    /** The offset of the rotation in the particle buffer. */
    public static final int ROT = 5;

    /** The offset of the birth time in the particle buffer. */
    public static final int BIRTH = 6;
    /** The offset of the lifespan in the particle buffer. */
    public static final int LIFESPAN = 7;

    /** The total number of fields per particle. */
    public static final int NUM_FIELDS = LIFESPAN+1;

    /** The particle data. */
    public final float[] data;

    /** A liveness flag for each particle. */
    public final int[] alive;

    /** Creates a particle buffer that can hold up to {@code maxParticles} particles. */
    public ParticleBuffer (int maxParticles) {
        _maxParticles = maxParticles;
        data = new float[maxParticles * NUM_FIELDS];
        alive = new int[maxParticles/32+1];
    }

    /** Returns true if the specified particle is alive. */
    public boolean isAlive (int partidx) {
        return (alive[partidx/32] & (1 << partidx % 32)) != 0;
    }

    /** Sets the particle in question to alive or not. */
    public void setAlive (int partidx, boolean isAlive) {
        if (isAlive) {
            alive[partidx/32] |= (1 << partidx % 32);
        } else {
            alive[partidx/32] &= ~(1 << partidx % 32);
        }
    }

    /** Adds {@code count} particles to this buffer, and initializes them with {@code initters}. */
    public void add (int count, float now, List<? extends Initializer> initters) {
        // optimization when we're full
        if (_live >= _maxParticles) return;
        // TODO: keep track of a last added position and start from there
        int pp = 0, ppos = 0, icount = initters.size(), initted = 0;
        for (int aa = 0; aa < alive.length && initted < count; aa++) {
            int live = alive[aa], mask = 1;
            if (live == 0xFFFFFFFF) {
                pp += 32;
                ppos += 32*NUM_FIELDS;
                continue; // all full
            }
            for (int end = Math.min(pp+32, _maxParticles); pp < end && initted < count;
                 pp++, ppos += NUM_FIELDS, mask <<= 1) {
                if ((live & mask) != 0) continue;
                live |= mask;
                data[ppos+BIRTH] = now;
                for (int ii = 0; ii < icount; ii++) {
                    initters.get(ii).init(pp, data, ppos);
                }
                initted++;
            }
            alive[aa] = live;
        }
    }

    /**
     * Applies the supplied effectors to all (live) particles in this buffer.
     *
     * @return the number of live particles to which the effectors were applied.
     */
    public int apply (List<? extends Effector> effectors, float now, float dt) {
        int pp = 0, ppos = 0, ecount = effectors.size(), death = 0, living = 0;
        for (int aa = 0; aa < alive.length; aa++) {
            int live = alive[aa], mask = 1, died = 0;
            for (int end = pp+32; pp < end; pp++, ppos += NUM_FIELDS, mask <<= 1) {
                // if this particle is not alive, skip it
                if ((live & mask) == 0) continue;

                // if this particle has died, mark it as such
                if (now - data[ppos+BIRTH] > data[ppos+LIFESPAN]) {
                    live &= ~mask;
                    died++;
                    continue;
                }

                // the particle lives, apply the effectors
                for (int ee = 0; ee < ecount; ee++)
                    effectors.get(ee).apply(pp, data, ppos, dt);
                living++;
            }

            // if we killed off any particles, update the liveness array
            if (died > 0) {
                alive[aa] = live;
                death += died;
            }
        }
        return living;
    }

    /** Renders the particles to the supplied surface. */
    public void render (GLShader shader, float width, float height) {
        float ql = -width/2, qt = -height/2, qr = width/2, qb = height/2;
        int pp = 0, ppos = 0, rendered = 0;
        for (int aa = 0; aa < alive.length; aa++) {
            int live = alive[aa], mask = 1;
            for (int end = pp+32; pp < end; pp++, ppos += NUM_FIELDS, mask <<= 1) {
                if ((live & mask) == 0) continue;
                float scale = data[ppos + SCALE], angle = data[ppos + ROT];
                float sina = 0, cosa = 1;
                if (angle != 0) {
                    sina = FloatMath.sin(angle);
                    cosa = FloatMath.cos(angle);
                }
                float x = data[ppos + POS_X], y = data[ppos + POS_Y];
                shader.addQuad(cosa * scale, sina * scale, -sina * scale, cosa * scale, x, y,
                               ql, qt, qr, qb, 0, 0, 1, 1);
                rendered++;
            }
        }
        _live = rendered;
    }

    protected final int _maxParticles;
    protected int _live;
}
