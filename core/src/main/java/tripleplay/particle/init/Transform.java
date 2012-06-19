//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle.init;

import playn.core.InternalTransform;
import playn.core.Layer;
import static playn.core.PlayN.graphics;

import pythagoras.f.FloatMath;
import pythagoras.f.Point;

import tripleplay.particle.Initializer;
import tripleplay.particle.ParticleBuffer;
import tripleplay.util.Randoms;

/**
 * Initializers for a particle's transform (scale, rotation and position).
 */
public class Transform
{
    /**
     * Returns an initialize that configures a particle to the identity transform. This is
     * generally registered prior to an initializer that updates some specific other part of the
     * transform.
     */
    public static Initializer identity () {
        return constant(0, 0);
    }

    /**
     * Returns an initializer that configures a particle with a constant translation, and no
     * scaling or rotation.
     */
    public static Initializer constant (float tx, float ty) {
        return constant(1, 0, tx, ty);
    }

    /**
     * Returns an initializer that configures a particle with a constant transform.
     */
    public static Initializer constant (float scale, float rot, final float tx, final float ty) {
        float sina = FloatMath.sin(rot), cosa = FloatMath.cos(rot);
        final float m00 = cosa * scale, m01 = sina * scale, m10 = -sina * scale, m11 = cosa * scale;
        return new Initializer() {
            @Override public void init (int index, float[] data, int start) {
                data[start + ParticleBuffer.M00] = m00;
                data[start + ParticleBuffer.M01] = m01;
                data[start + ParticleBuffer.M10] = m10;
                data[start + ParticleBuffer.M11] = m11;
                data[start + ParticleBuffer.TX] = tx;
                data[start + ParticleBuffer.TY] = ty;
            }
        };
    }

    /**
     * Returns an initializer that configures a particle with the same transform (scale, rotation,
     * position) as the supplied layer. This will the the fully computed transform, not the layer's
     * local transform.
     */
    public static Initializer layer (final Layer layer) {
        return new Initializer() {
            @Override public void willInit (int count) {
                // concatenate the transform of all layers above our target layer
                xform.setTransform(1, 0, 0, 1, 0, 0);
                Layer xlayer = layer;
                while (xlayer != null) {
                    xform.preConcatenate((InternalTransform)xlayer.transform());
                    xlayer = xlayer.parent();
                }
                // finally pre-concatenate the root transform as we're bypassing normal rendering
                xform.preConcatenate(graphics().ctx().rootTransform());
                xform.get(_matrix);
            }
            @Override public void init (int index, float[] data, int start) {
                System.arraycopy(_matrix, 0, data, start + ParticleBuffer.M00, 6);
            }
            protected final InternalTransform xform = graphics().ctx().createTransform();
            protected final Point _pos = new Point();
            protected final float[] _matrix = new float[6];
        };
    }

    /**
     * Returns an initializer that configures a particle's position in a random region. The scale
     * and rotation are not initialized, so this should be used with {@link #identity}.
     */
    public static Initializer randomPos (final Randoms rando, final float x, final float y,
                                         final float width, final float height) {
        return new Initializer() {
            @Override public void init (int index, float[] data, int start) {
                data[start + ParticleBuffer.TX] = x + rando.getFloat(width);
                data[start + ParticleBuffer.TY] = y + rando.getFloat(height);
            }
            protected final Point _pos = new Point();
        };
    }
}
