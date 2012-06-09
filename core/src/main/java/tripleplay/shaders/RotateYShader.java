//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.shaders;

import playn.core.gl.GLContext;
import playn.core.gl.IndexedTrisShader;

/**
 * Rotates the view through a 3D transform around the Y axis. Can be used to simulate a (rigid)
 * page turn.
 */
public class RotateYShader extends IndexedTrisShader
{
    /** The angle of rotation. */
    public float angle;

    /** The x-coordinate of the eye (as a fraction of the screen width). */
    public final float eyeX;

    /** The y-coordinate of the eye (as a fraction of the screen height). */
    public final float eyeY;

    /** The multiple of the screen width by which to scale z (usually either 1 or 0.5). */
    public final float zScale;

    public RotateYShader (GLContext ctx, float eyeX, float eyeY, float zScale) {
        super(ctx);
        this.eyeX = eyeX;
        this.eyeY = eyeY;
        this.zScale = zScale;
    }

    @Override protected String vertexShader() {
        return "uniform vec2 u_ScreenSize;\n" +
            "uniform float u_Angle;\n" +
            "uniform vec2 u_Eye;\n" +
            "attribute vec4 a_Matrix;\n" +
            "attribute vec2 a_Translation;\n" +
            "attribute vec2 a_Position;\n" +
            "attribute vec2 a_TexCoord;\n" +
            "varying vec2 v_TexCoord;\n" +

            "void main(void) {\n" +
            // Transform the vertex per the normal screen transform
            "  mat4 transform = mat4(\n" +
            "    a_Matrix[0],      a_Matrix[1],      0, 0,\n" +
            "    a_Matrix[2],      a_Matrix[3],      0, 0,\n" +
            "    0,                0,                1, 0,\n" +
            "    a_Translation[0], a_Translation[1], 0, 1);\n" +
            "  vec4 pos = transform * vec4(a_Position, 0, 1);\n" +

            // Rotate the vertex per our 3D rotation
            "  float cosa = cos(u_Angle);\n" +
            "  float sina = sin(u_Angle);\n" +
            "  mat4 rotmat = mat4(\n" +
            "    cosa, 0, sina, 0,\n" +
            "    0,    1, 0,    0,\n" +
            "   -sina, 0, cosa, 0,\n" +
            "    0,    0, 0,    1);\n" +
            "  pos = rotmat * vec4(pos.x - u_Eye.x * u_ScreenSize.x," +
            "                      pos.y - u_Eye.y * u_ScreenSize.y, 0, 1);\n" +

            // Perspective project the vertex back into the plane
            "  mat4 persp = mat4(\n" +
            "    1, 0, 0, 0,\n" +
            "    0, 1, 0, 0,\n" +
            "    0, 0, 1, -1.0/2000.0,\n" +
            "    0, 0, 0, 1);\n" +
            "  pos = persp * pos;\n" +
            "  pos += vec4(u_Eye.x * u_ScreenSize.x, u_Eye.y * u_ScreenSize.y, 0, 0);\n;" +

            // Finally convert the coordinates into OpenGL space
            "  pos.x /= (u_ScreenSize.x / 2.0);\n" +
            "  pos.y /= (u_ScreenSize.y / 2.0);\n" +
            "  pos.z /= (u_ScreenSize.x * " + zScale + ");\n" +
            "  pos.x -= 1.0;\n" +
            "  pos.y = 1.0 - pos.y;\n" +
            // z may already be rotated into negative space so we don't shift it
            "  gl_Position = pos;\n" +

            "  v_TexCoord = a_TexCoord;\n" +
            "}";
    }

    @Override
    protected Core createTextureCore() {
        return new ITCore(this, vertexShader(), textureFragmentShader()) {
            private final Uniform1f uAngle = prog.getUniform1f("u_Angle");
            private final Uniform2f uEye = prog.getUniform2f("u_Eye");

            @Override
            public void prepare(int fbufWidth, int fbufHeight) {
                super.prepare(fbufWidth, fbufHeight);
                uAngle.bind(angle);
                uEye.bind(eyeX, eyeY);
            }
        };
    }

    @Override
    protected Core createColorCore() {
        return new ITCore(this, vertexShader(), colorFragmentShader()) {
            private final Uniform1f uAngle = prog.getUniform1f("u_Angle");
            private final Uniform2f uEye = prog.getUniform2f("u_Eye");

            @Override
            public void prepare(int fbufWidth, int fbufHeight) {
                super.prepare(fbufWidth, fbufHeight);
                uAngle.bind(angle);
                uEye.bind(eyeX, eyeY);
            }
        };
    }
}
