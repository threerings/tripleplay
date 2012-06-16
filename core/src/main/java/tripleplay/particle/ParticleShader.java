//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import pythagoras.f.FloatMath;

import playn.core.gl.GL20;
import playn.core.gl.GLBuffer;
import playn.core.gl.GLContext;
import playn.core.gl.GLProgram;
import playn.core.gl.GLShader;

/**
 * A custom shader designed for shading particles.
 */
public class ParticleShader extends GLShader
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
        return core = new ParticleCore(VERTEX_SHADER, TEXTURE_FRAG_SHADER);
    }

    @Override
    protected Core createColorCore () {
        throw new UnsupportedOperationException("Color core should not be used.");
    }

    @Override
    protected Extras createTextureExtras (GLProgram prog) {
        return new ParticleExtras(prog);
    }

    protected static final String VERTEX_SHADER =
        "uniform vec2 u_ScreenSize;\n" +
        "attribute vec4 a_Matrix;\n" +
        "attribute vec2 a_Translation;\n" +
        "attribute vec4 a_Color;\n" +
        "attribute vec2 a_Position;\n" +
        "attribute vec2 a_TexCoord;\n" +
        "varying vec2 v_TexCoord;\n" +
        "varying vec4 v_Color;\n" +

        "void main(void) {\n" +
        // Transform the vertex.
        "  mat3 transform = mat3(\n" +
        "    a_Matrix[0], a_Matrix[1], 0,\n" +
        "    a_Matrix[2], a_Matrix[3], 0,\n" +
        "    a_Translation[0], a_Translation[1], 1);\n" +
        "  gl_Position = vec4(transform * vec3(a_Position, 1.0), 1);\n" +
        // Scale from screen coordinates to [0, 2].
        "  gl_Position.x /= (u_ScreenSize.x / 2.0);\n" +
        "  gl_Position.y /= (u_ScreenSize.y / 2.0);\n" +
        // Offset to [-1, 1] and flip y axis to put origin at top-left.
        "  gl_Position.x -= 1.0;\n" +
        "  gl_Position.y = 1.0 - gl_Position.y;\n" +

        "  v_TexCoord = a_TexCoord;\n" +
        "  v_Color = a_Color;\n" +
        "}";

    protected static final String TEXTURE_FRAG_SHADER =
        "#ifdef GL_ES\n" +
        "precision highp float;\n" +
        "#endif\n" +

        "uniform sampler2D u_Texture;\n" +
        "varying vec2 v_TexCoord;\n" +
        "varying vec4 v_Color;\n" +

        "void main(void) {\n" +
        "  vec4 texcol = texture2D(u_Texture, v_TexCoord);\n" +
        "  vec4 tinted = vec4(texcol.rgb * (v_Color.rgb * texcol.a + (1.0 - texcol.a)), texcol.a);\n" +
        "  gl_FragColor = tinted * v_Color.a;\n" +
        "}";

    protected static final int VERTEX_SIZE = 14; // 14 floats per vertex
    protected static final int START_VERTS = 16*4;
    protected static final int EXPAND_VERTS = 16*4;
    protected static final int START_ELEMS = 6*START_VERTS/4;
    protected static final int EXPAND_ELEMS = 6*EXPAND_VERTS/4;
    protected static final int FLOAT_SIZE_BYTES = 4;
    protected static final int VERTEX_STRIDE = VERTEX_SIZE * FLOAT_SIZE_BYTES;

    protected class ParticleCore extends Core {
        private final Uniform2f uScreenSize;
        private final Attrib aMatrix, aTranslation, aColor, aPosition, aTexCoord;

        private final GLBuffer.Float vertices;
        private final GLBuffer.Short elements;

        public ParticleCore (String vertShader, String fragShader) {
            super(vertShader, fragShader);

            // determine our various shader program locations
            uScreenSize = prog.getUniform2f("u_ScreenSize");
            aMatrix = prog.getAttrib("a_Matrix", 4, GL20.GL_FLOAT);
            aTranslation = prog.getAttrib("a_Translation", 2, GL20.GL_FLOAT);
            aColor = prog.getAttrib("a_Color", 4, GL20.GL_FLOAT);
            aPosition = prog.getAttrib("a_Position", 2, GL20.GL_FLOAT);
            aTexCoord = prog.getAttrib("a_TexCoord", 2, GL20.GL_FLOAT);

            // create our vertex and index buffers
            vertices = ctx.createFloatBuffer(START_VERTS*VERTEX_SIZE);
            elements = ctx.createShortBuffer(START_ELEMS);
        }

        public void ensureCapacity (int maxQuads) {
            beginPrimitive(maxQuads*4, maxQuads*6);
        }

        @Override
        public void prepare (int fbufWidth, int fbufHeight) {
            prog.bind();
            uScreenSize.bind(fbufWidth, fbufHeight);

            vertices.bind(GL20.GL_ARRAY_BUFFER);
            aMatrix.bind(VERTEX_STRIDE, 0);
            aTranslation.bind(VERTEX_STRIDE, 16);
            aColor.bind(VERTEX_STRIDE, 24);
            aPosition.bind(VERTEX_STRIDE, 40);
            aTexCoord.bind(VERTEX_STRIDE, 48);

            elements.bind(GL20.GL_ELEMENT_ARRAY_BUFFER);
        }

        @Override
        public void flush () {
            if (vertices.position() == 0) return;
            vertices.send(GL20.GL_ARRAY_BUFFER, GL20.GL_STREAM_DRAW);
            int elems = elements.send(GL20.GL_ELEMENT_ARRAY_BUFFER, GL20.GL_STREAM_DRAW);
            elements.drawElements(GL20.GL_TRIANGLES, elems);
        }

        @Override
        public void destroy () {
            super.destroy();
            vertices.destroy();
            elements.destroy();
        }

        public void addQuad (float left, float top, float right, float bottom,
                             float[] data, int ppos) {
            int vertIdx = beginPrimitive(4, 6);

            // bulk copy m00,m01,m10,m11,tx,ty,r,g,b,a
            int pstart = ppos + ParticleBuffer.M00;
            vertices.add(data, pstart, 10).add(left, top).add(0, 0);
            vertices.add(data, pstart, 10).add(right, top).add(1, 0);
            vertices.add(data, pstart, 10).add(left, bottom).add(0, 1);
            vertices.add(data, pstart, 10).add(right, bottom).add(1, 1);

            elements.add(vertIdx+0);
            elements.add(vertIdx+1);
            elements.add(vertIdx+2);
            elements.add(vertIdx+1);
            elements.add(vertIdx+3);
            elements.add(vertIdx+2);

            // addQuad(,
            //         left, top, right, bottom, 0, 0, 1, 1);
        }

        @Override
        public void addQuad (float m00, float m01, float m10, float m11, float tx, float ty,
                             float x1, float y1, float sx1, float sy1,
                             float x2, float y2, float sx2, float sy2,
                             float x3, float y3, float sx3, float sy3,
                             float x4, float y4, float sx4, float sy4) {
            throw new RuntimeException("Not used.");
        }

        protected int beginPrimitive (int vertexCount, int elemCount) {
            int vertIdx = vertices.position() / VERTEX_SIZE;
            int verts = vertIdx + vertexCount, elems = elements.position() + elemCount;
            int availVerts = vertices.capacity() / VERTEX_SIZE, availElems = elements.capacity();
            if ((verts > availVerts) || (elems > availElems)) {
                ParticleShader.this.flush();
                if (vertexCount > availVerts)
                    expandVerts(vertexCount);
                if (elemCount > availElems)
                    expandElems(elemCount);
                return 0;
            }
            return vertIdx;
        }

        private void expandVerts (int vertCount) {
            int newVerts = vertices.capacity() / VERTEX_SIZE;
            while (newVerts < vertCount)
                newVerts += EXPAND_VERTS;
            vertices.expand(newVerts*VERTEX_SIZE);
        }

        private void expandElems (int elemCount) {
            int newElems = elements.capacity();
            while (newElems < elemCount)
                newElems += EXPAND_ELEMS;
            elements.expand(newElems);
        }
    }

    protected class ParticleExtras extends Extras {
        private final Uniform1i uTexture;
        private int lastTex;

        public ParticleExtras(GLProgram prog) {
            uTexture = prog.getUniform1i("u_Texture");
        }

        @Override
        public void prepare(int tex, float alpha, boolean justActivated) {
            ctx.checkGLError("textureShader.prepare start");
            boolean stateChanged = (tex != lastTex);
            if (!justActivated && stateChanged)
                flush();
            if (stateChanged) {
                lastTex = tex;
                ctx.checkGLError("textureShader.prepare end");
            }
            if (justActivated) {
                ctx.activeTexture(GL20.GL_TEXTURE0);
                uTexture.bind(0);
            }
        }

        @Override
        public void willFlush () {
            ctx.bindTexture(lastTex);
        }
    }
}
