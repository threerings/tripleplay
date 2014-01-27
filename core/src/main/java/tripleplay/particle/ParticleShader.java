//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import playn.core.gl.GLContext;
import playn.core.gl.IndexedTrisShader;

/**
 * A custom shader designed for shading particles.
 */
public class ParticleShader extends IndexedTrisShader
{
    public ParticleCore core;

    public ParticleShader (GLContext ctx) {
        super(ctx);
    }

    public ParticleShader prepare (int tex, int maxQuads) {
        prepareTexture(tex, 1);
        ((ParticleCore)texCore).ensureCapacity(maxQuads);
        return this;
    }

    @Override
    protected Core createTextureCore () {
        return core = new ParticleCore(vertexShader(), textureFragmentShader());
    }

    protected class ParticleCore extends ITCore {
        public ParticleCore (String vertexShader, String fragShader) {
            super(vertexShader, fragShader);
        }

        public void ensureCapacity (int maxQuads) {
            // this doesn't actually change anything, it just makes sure we have space for a
            // primitive of the specified size and does some idempotent math
            beginPrimitive(maxQuads*4, maxQuads*6);
        }

        public void addQuad (float left, float top, float right, float bot, float[] data, int ppos) {
            int vertIdx = beginPrimitive(4, 6);

            // bulk copy m00,m01,m10,m11,tx,ty then add quad info, then copy ar,gb
            int pstart = ppos + ParticleBuffer.M00;
            vertices.add(data, pstart, 8).add(left,  top).add(0, 0);
            vertices.add(data, pstart, 8).add(right, top).add(1, 0);
            vertices.add(data, pstart, 8).add(left,  bot).add(0, 1);
            vertices.add(data, pstart, 8).add(right, bot).add(1, 1);

            elements.add(vertIdx+0);
            elements.add(vertIdx+1);
            elements.add(vertIdx+2);
            elements.add(vertIdx+1);
            elements.add(vertIdx+3);
            elements.add(vertIdx+2);
        }
    }
}
