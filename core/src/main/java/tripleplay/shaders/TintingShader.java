//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.shaders;

import playn.core.gl.GLContext;
import playn.core.gl.IndexedTrisShader;

/**
 * A shader that tints a texture by multiplying it with a color.
 */
public class TintingShader extends IndexedTrisShader
{
    public TintingShader (GLContext ctx, int color) {
        this(ctx,
             ((color >> 24) & 0xFF) / 255f,
             ((color >> 16) & 0xFF) / 255f,
             ((color >>  8) & 0xFF) / 255f,
             ((color      ) & 0xFF) / 255f);
    }

    public TintingShader (GLContext ctx, float alpha, float red, float green, float blue) {
        super(ctx);
        _alpha = alpha;
        _red = red;
        _green = green;
        _blue = blue;
    }

    @Override protected String textureFragmentShader() {
        return "#ifdef GL_ES\n" +
            "precision highp float;\n" +
            "#endif\n" +

            "uniform sampler2D u_Texture;\n" +
            "varying vec2 v_TexCoord;\n" +
            "uniform float u_Alpha;\n" +
            "const vec4 tintColor = vec4(" + ShaderUtil.format(_red) + ", " +
                                             ShaderUtil.format(_green) + ", " +
                                             ShaderUtil.format(_blue) + ", " +
                                             ShaderUtil.format(_alpha) + ");\n" +
            "void main(void) {\n" +
            "  vec4 textureColor = texture2D(u_Texture, v_TexCoord);\n" +
            "  gl_FragColor = textureColor * tintColor * u_Alpha;\n" +
            "}";
    }

    protected final float _alpha, _red, _green, _blue;
}
