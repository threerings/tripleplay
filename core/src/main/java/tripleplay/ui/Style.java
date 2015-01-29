//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Canvas;
import playn.core.Font;
import playn.core.Sound;
import playn.core.TextBlock;

import tripleplay.util.EffectRenderer.Gradient;
import tripleplay.util.EffectRenderer;
import tripleplay.util.TextStyle;

/**
 * Defines style properties for interface elements. Some style properties are inherited, such that
 * a property not specified in a leaf element will be inherited by the nearest parent for which the
 * property is specified. Other style properties are not inherited, and a default value will be
 * used in cases where a leaf element lacks a property. The documentation for each style property
 * indicates whether or not it is inherited.
 */
public abstract class Style<V>
{
    /** Defines element modes which can be used to modify an element's styles. */
    public static enum Mode {
        DEFAULT(true, false),
        DISABLED(false, false),
        SELECTED(true, true),
        DISABLED_SELECTED(false, true);

        /** Whether the element is enabled in this mode. */
        public final boolean enabled;
        /** Whether the element is selected in this mode. */
        public final boolean selected;

        Mode (boolean enabled, boolean selected) {
            this.enabled = enabled;
            this.selected = selected;
        }
    }

    /** Used to configure {@link Styles} instances. See {@link Styles#add}. */
    public static class Binding<V> {
        /** The style being configured. */
        public final Style<V> style;

        /** The value to be bound for the style. */
        public final V value;

        public Binding (Style<V> style, V value) {
            this.style = style;
            this.value = value;
        }
    }

    /** Defines horizontal alignment choices. */
    public static enum HAlign {
        LEFT {
            @Override public float offset (float size, float extent) {
                return 0;
            }
        }, RIGHT {
            @Override public float offset (float size, float extent) {
                return (extent - size);
            }
        }, CENTER {
            @Override public float offset (float size, float extent) {
                return (extent - size)/2;
            }
        };

        public abstract float offset (float size, float extent);
    }

    /** Defines vertical alignment choices. */
    public static enum VAlign {
        TOP {
            @Override public float offset (float size, float extent) {
                return 0;
            }
        }, BOTTOM {
            @Override public float offset (float size, float extent) {
                return (extent - size);
            }
        }, CENTER {
            @Override public float offset (float size, float extent) {
                return (extent - size)/2;
            }
        };

        public abstract float offset (float size, float extent);
    }

    /** Defines icon position choices. */
    public static enum Pos {
        LEFT, ABOVE, RIGHT, BELOW;
        /** Tests if this position is left or right. */
        public boolean horizontal () {
            return this == LEFT || this == RIGHT;
        }
    }

    /** Used to create text effects. */
    public interface EffectFactory {
        /** Creates the effect renderer to be used by this factory. */
        EffectRenderer createEffectRenderer (Element<?> elem);
    }

    /** Defines supported text effects. */
    public static enum TextEffect implements EffectFactory {
        /** Outlines the text in the highlight color. */
        PIXEL_OUTLINE {
            public EffectRenderer createEffectRenderer (Element<?> elem) {
                return new EffectRenderer.PixelOutline(Styles.resolveStyle(elem, Style.HIGHLIGHT));
            }
        },
        /** Outlines the text in the highlight color. */
        VECTOR_OUTLINE {
            public EffectRenderer createEffectRenderer (Element<?> elem) {
                return new EffectRenderer.VectorOutline(
                    Styles.resolveStyle(elem, Style.HIGHLIGHT),
                    Styles.resolveStyle(elem, Style.OUTLINE_WIDTH),
                    Styles.resolveStyle(elem, Style.OUTLINE_CAP),
                    Styles.resolveStyle(elem, Style.OUTLINE_JOIN));
            }
        },
        /** Draws a shadow below and to the right of the text in the shadow color. */
        SHADOW {
            public EffectRenderer createEffectRenderer (Element<?> elem) {
                return new EffectRenderer.Shadow(Styles.resolveStyle(elem, Style.SHADOW),
                                                 Styles.resolveStyle(elem, Style.SHADOW_X),
                                                 Styles.resolveStyle(elem, Style.SHADOW_Y));
            }
        },
        /** Draws a gradient from the font color to the gradient color. */
        GRADIENT {
            public EffectRenderer createEffectRenderer (Element<?> elem) {
                return new Gradient(Styles.resolveStyle(elem, Style.GRADIENT_COLOR),
                                    Styles.resolveStyle(elem, Style.GRADIENT_TYPE));
            }
        },
        /** No text effect. */
        NONE {
            public EffectRenderer createEffectRenderer (Element<?> elem) {
                return EffectRenderer.NONE;
            }
        };
    }

    /** Used to provide concise HAlign style declarations. */
    public static class HAlignStyle extends Style<HAlign> {
        public final Binding<HAlign> left = is(HAlign.LEFT);
        public final Binding<HAlign> right = is(HAlign.RIGHT);
        public final Binding<HAlign> center = is(HAlign.CENTER);
        @Override public HAlign getDefault (Element<?> elem) { return HAlign.CENTER; }
        HAlignStyle() { super(false); }
    }

    /** Used to provide concise VAlign style declarations. */
    public static class VAlignStyle extends Style<VAlign> {
        public final Binding<VAlign> top = is(VAlign.TOP);
        public final Binding<VAlign> bottom = is(VAlign.BOTTOM);
        public final Binding<VAlign> center = is(VAlign.CENTER);
        @Override public VAlign getDefault (Element<?> elem) { return VAlign.CENTER; }
        VAlignStyle() { super(false); }
    }

    /** Used to provide concise Pos style declarations. */
    public static class PosStyle extends Style<Pos> {
        public final Binding<Pos> left = is(Pos.LEFT);
        public final Binding<Pos> above = is(Pos.ABOVE);
        public final Binding<Pos> right = is(Pos.RIGHT);
        public final Binding<Pos> below = is(Pos.BELOW);
        @Override public Pos getDefault (Element<?> elem) { return Pos.LEFT; }
        PosStyle() { super(false); }
    }

    /** Used to provide concise TextEffect style declarations. */
    public static class TextEffectStyle extends Style<EffectFactory> {
        public final Binding<EffectFactory> pixelOutline = is(TextEffect.PIXEL_OUTLINE);
        public final Binding<EffectFactory> vectorOutline = is(TextEffect.VECTOR_OUTLINE);
        public final Binding<EffectFactory> shadow = is(TextEffect.SHADOW);
        public final Binding<EffectFactory> gradient = is(TextEffect.GRADIENT);
        public final Binding<EffectFactory> none = is(TextEffect.NONE);
        public final Binding<EffectFactory> is (final EffectRenderer renderer) {
            return is(new EffectFactory() {
                public EffectRenderer createEffectRenderer (Element<?> elem) {
                    return renderer;
                }
            });
        }
        @Override public EffectFactory getDefault (Element<?> elem) { return TextEffect.NONE; }
        TextEffectStyle() { super(true); }
    }

    public static class GradientTypeStyle extends Style<Gradient.Type> {
        public final Binding<Gradient.Type> bottom = is(Gradient.Type.BOTTOM);
        public final Binding<Gradient.Type> top    = is(Gradient.Type.TOP);
        public final Binding<Gradient.Type> center = is(Gradient.Type.CENTER);
        @Override public Gradient.Type getDefault (Element<?> elem) { return Gradient.Type.BOTTOM; }
        GradientTypeStyle() { super(true); }
    }

    /** The foreground color for an element. Inherited. */
    public static final Style<Integer> COLOR = new Style<Integer>(true) {
        @Override public Integer getDefault (Element<?> elem) {
            return elem.isEnabled() ? 0xFF000000 : 0xFF666666;
        }
    };

    /** The highlight color for an element. Inherited. */
    public static final Style<Integer> HIGHLIGHT = new Style<Integer>(true) {
        @Override public Integer getDefault (Element<?> elem) {
            return elem.isEnabled() ? 0xAAFFFFFF : 0xAACCCCCC;
        }
    };

    /** A Boolean style, with convenient members for on & off bindings. */
    public static class Flag extends Style<Boolean> {
        public final Binding<Boolean> off = is(false);
        public final Binding<Boolean> on = is(true);
        private Flag (boolean inherited, boolean defaultVal) {
            super(inherited);
            _default = defaultVal;
        }
        @Override public Boolean getDefault (Element<?> mode) { return _default; }
        protected final Boolean _default;
    }

    /** The shadow color for an element. Inherited. */
    public static final Style<Integer> SHADOW = newStyle(true, 0x55000000);

    /** The shadow offset in pixels. Inherited. */
    public static final Style<Float> SHADOW_X = newStyle(true, 2f);

    /** The shadow offset in pixels. Inherited. */
    public static final Style<Float> SHADOW_Y = newStyle(true, 2f);

    /** The color of the gradient. Inherited. */
    public static final Style<Integer> GRADIENT_COLOR = newStyle(true, 0xFFC70000);

    /** The type of gradient. Inherited. */
    public static final GradientTypeStyle GRADIENT_TYPE = new GradientTypeStyle();

    /** The stroke width of the outline, when using a vector outline. */
    public static final Style<Float> OUTLINE_WIDTH = newStyle(true, 1f);

    /** The line cap for the outline, when using a vector outline. */
    public static final Style<Canvas.LineCap> OUTLINE_CAP = newStyle(true, Canvas.LineCap.ROUND);

    /** The line join for the outline, when using a vector outline. */
    public static final Style<Canvas.LineJoin> OUTLINE_JOIN = newStyle(true, Canvas.LineJoin.ROUND);

    /** The horizontal alignment of an element. Not inherited. */
    public static final HAlignStyle HALIGN = new HAlignStyle();

    /** The vertical alignment of an element. Not inherited. */
    public static final VAlignStyle VALIGN = new VAlignStyle();

    /** The font used to render text. Inherited. */
    public static final Style<Font> FONT = newStyle(true, new Font("Helvetica", 16));

    /** Whether or not to allow text to wrap. When text cannot wrap and does not fit into the
     * allowed space, it is truncated. Not inherited. */
    public static final Flag TEXT_WRAP = newFlag(false, false);

    /** The effect to use when rendering text, if any. Inherited. */
    public static final TextEffectStyle TEXT_EFFECT = new TextEffectStyle();

    /** Whether or not to underline text. Inherited. */
    public static final Flag UNDERLINE = newFlag(true, false);

    /** Whether or not to automatically shrink a text widget's font size until it fits into the
     * horizontal space it has been allotted. Cannot be used with {@link #TEXT_WRAP}. Not
     * inherited. */
    public static final Flag AUTO_SHRINK = newFlag(false, false);

    /** The background for an element. Not inherited. */
    public static final Style<Background> BACKGROUND = newStyle(false, Background.blank());

    /** The position relative to the text to render an icon for labels, buttons, etc. */
    public static final PosStyle ICON_POS = new PosStyle();

    /** The gap between the icon and text in labels, buttons, etc. */
    public static final Style<Integer> ICON_GAP = newStyle(false, 2);

    /** If true, the icon is cuddled to the text, with extra space between icon and border, if
     * false, the icon is placed next to the border with extra space between icon and label. */
    public static final Flag ICON_CUDDLE = newFlag(false, false);

    /** The effect to apply to the icon. */
    public static final Style<IconEffect> ICON_EFFECT = newStyle(false, IconEffect.NONE);

    /** The sound to be played when this element's action is triggered. */
    public static final Style<Sound> ACTION_SOUND = newStyle(false, (Sound)null);

    /** Indicates whether or not this style property is inherited. */
    public final boolean inherited;

    /**
     * Creates a text style instance based on the supplied element's stylings.
     */
    public static TextStyle createTextStyle (Element<?> elem) {
        return new TextStyle(
            Styles.resolveStyle(elem, Style.FONT),
            Styles.resolveStyle(elem, Style.TEXT_EFFECT) != TextEffect.PIXEL_OUTLINE,
            Styles.resolveStyle(elem, Style.COLOR),
            Styles.resolveStyle(elem, Style.TEXT_EFFECT).createEffectRenderer(elem),
            Styles.resolveStyle(elem, Style.UNDERLINE));
    }

    /**
     * Returns the default value for this style for the supplied element.
     */
    public abstract V getDefault (Element<?> mode);

    /**
     * Returns a {@link Binding} with this style bound to the specified value.
     */
    public Binding<V> is (V value) {
        return new Binding<V>(this, value);
    }

    /**
     * Creates a style identifier with the supplied properties.
     */
    public static <V> Style<V> newStyle (boolean inherited, final V defaultValue) {
        return new Style<V>(inherited) {
            @Override public V getDefault (Element<?> elem) {
                return defaultValue;
            }
        };
    }

    /**
     * Creates a boolean style identifier with the supplied properties.
     */
    public static Flag newFlag (boolean inherited, final boolean defaultValue) {
        return new Flag(inherited, defaultValue);
    }

    protected Style (boolean inherited) {
        this.inherited = inherited;
    }

    protected static TextBlock.Align toAlignment (HAlign align) {
        switch (align) {
        default:
        case   LEFT: return TextBlock.Align.LEFT;
        case  RIGHT: return TextBlock.Align.RIGHT;
        case CENTER: return TextBlock.Align.CENTER;
        }
    }
}
