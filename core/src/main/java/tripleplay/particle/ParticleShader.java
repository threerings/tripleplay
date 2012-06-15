//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import playn.core.gl.GLContext;
import playn.core.gl.IndexedTrisShader;

/**
 * A custom shader designed for shading particles.
 */
public class ParticleShader extends IndexedTrisShader
{
    public ParticleShader (GLContext ctx) {
        super(ctx);
    }

    public ParticleShader prepareTexture(int tex, float alpha, int maxQuads) {
        prepareTexture(tex, alpha);
        ((ParticleCore)texCore).ensureCapacity(maxQuads);
        return this;
    }

    @Override
    protected Core createTextureCore() {
        return new ParticleCore(this, vertexShader(), textureFragmentShader());
    }

    protected class ParticleCore extends ITCore {
        public ParticleCore (ParticleShader shader, String vertShader, String fragShader) {
            super(shader, vertShader, fragShader);
        }

        public void ensureCapacity (int maxQuads) {
            beginPrimitive(maxQuads*4, maxQuads*6);
        }
    }

    // TODO: support tinting the texture
}
