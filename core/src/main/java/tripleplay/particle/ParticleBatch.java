//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import playn.core.GL20;
import playn.core.Tile;
import playn.core.TriangleBatch;

/**
 * A custom batch designed for shading particles.
 */
public class ParticleBatch extends TriangleBatch
{
    public ParticleBatch (GL20 gl) {
        super(gl);
    }

    public ParticleBatch prepare (Tile tile, int maxQuads) {
        setTexture(tile.texture());
        _sx = tile.sx(); _sy = tile.sy();
        _tx = tile.tx(); _ty = tile.ty();
        // this doesn't actually change anything, it just makes sure we have space for a primitive
        // of the specified size and does some idempotent math
        beginPrimitive(maxQuads*4, maxQuads*6);
        return this;
    }

    /** Adds a particle quad to this batch. */
    public void addParticle (float l, float t, float r, float b, float[] data, int ppos) {
        int vertIdx = beginPrimitive(4, 6), pstart = ppos + ParticleBuffer.M00;
        float[] verts = vertices; int offset = vertPos;
        float sx = _sx, sy = _sy, tx = _tx, ty = _ty;
        // bulk copy m00,m01,m10,m11,tx,ty,ar,gb then add quad info
        offset = add(verts, add(verts, offset, data, pstart, 8), l, t, sx, sy);
        offset = add(verts, add(verts, offset, data, pstart, 8), r, t, tx, sy);
        offset = add(verts, add(verts, offset, data, pstart, 8), l, b, sx, ty);
        offset = add(verts, add(verts, offset, data, pstart, 8), r, b, tx, ty);
        vertPos = offset;
        addElems(vertIdx, QUAD_INDICES, 0, QUAD_INDICES.length, 0);
    }

    protected float _sx, _sy, _tx, _ty;
}
