//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Canvas;
import playn.core.Font;
import playn.core.PlayN;
import playn.core.Sound;
import playn.core.TextFormat;
import tripleplay.util.EffectRenderer;

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
    public static enum Mode { DEFAULT, DISABLED, SELECTED, DISABLED_SELECTED }

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
            public float offset (float size, float extent) {
                return 0;
            }
        }, RIGHT {
            public float offset (float size, float extent) {
                return (extent - size);
            }
        }, CENTER {
            public float offset (float size, float extent) {
                return (extent - size)/2;
            }
        };

        public abstract float offset (float size, float extent);
    };

    /** Defines vertical alignment choices. */
    public static enum VAlign {
        TOP {
            public float offset (float size, float extent) {
                return 0;
            }
        }, BOTTOM {
            public float offset (float size, float extent) {
                return (extent - size);
            }
        }, CENTER {
            public float offset (float size, float extent) {
                return (extent - size)/2;
            }
        };

        public abstract float offset (float size, float extent);
    };

    /** Defines icon position choices. */
    public static enum Pos {
        LEFT, ABOVE, RIGHT, BELOW;
    }

    /** Defines supported text effects. */
    public static enum TextEffect {
        /** Outlines the text in the highlight color. */
        PIXEL_OUTLINE,
        /** Outlines the text in the highlight color. */
        VECTOR_OUTLINE,
        /** Draws a shadow below and to the right of the text in the shadow color. */
        SHADOW,
        /** No text effect. */
        NONE };

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
    public static class TextEffectStyle extends Style<TextEffect> {
        /** @deprecated Use {@link #pixelOutline}. */
        @Deprecated public final Binding<TextEffect> outline = is(TextEffect.PIXEL_OUTLINE);
        public final Binding<TextEffect> pixelOutline = is(TextEffect.PIXEL_OUTLINE);
        public final Binding<TextEffect> vectorOutline = is(TextEffect.VECTOR_OUTLINE);
        public final Binding<TextEffect> shadow = is(TextEffect.SHADOW);
        public final Binding<TextEffect> none = is(TextEffect.NONE);
        @Override public TextEffect getDefault (Element<?> elem) { return TextEffect.NONE; }
        TextEffectStyle() { super(true); }
    }

    /** The foreground color for an element. Inherited. */
    public static final Style<Integer> COLOR = new Style<Integer>(true) {
        public Integer getDefault (Element<?> elem) {
            return elem.isEnabled() ? 0xFF000000 : 0xFF666666;
        }
    };

    /** The highlight color for an element. Inherited. */
    public static final Style<Integer> HIGHLIGHT = new Style<Integer>(true) {
        public Integer getDefault (Element<?> elem) {
            return elem.isEnabled() ? 0xAAFFFFFF : 0xAACCCCCC;
        }
    };

    /** The shadow color for an element. Inherited. */
    public static final Style<Integer> SHADOW = newStyle(true, 0x55000000);

    /** The shadow offset in pixels. Inherited. */
    public static final Style<Float> SHADOW_X = newStyle(true, 2f);

    /** The shadow offset in pixels. Inherited. */
    public static final Style<Float> SHADOW_Y = newStyle(true, 2f);

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
    public static final Style<Font> FONT = newStyle(
        true, PlayN.graphics().createFont("Helvetica", Font.Style.PLAIN, 16));

    /** Whether or not to allow text to wrap. When text cannot wrap and does not fit into the
     * allowed space, it is truncated. Not inherited. */
    public static final Style<Boolean> TEXT_WRAP = newStyle(false, false);

    /** The effect to use when rendering text, if any. Inherited. */
    public static final TextEffectStyle TEXT_EFFECT = new TextEffectStyle();

    /** The background for an element. Not inherited. */
    public static final Style<Background> BACKGROUND = newStyle(false, Background.blank());

    /** The position relative to the text to render an icon for labels, buttons, etc. */
    public static final PosStyle ICON_POS = new PosStyle();

    /** The gap between the icon and text in labels, buttons, etc. */
    public static final Style<Integer> ICON_GAP = newStyle(false, 2);

    /** If true, the icon is cuddled to the text, with extra space between icon and border, if
     * false, the icon is placed next to the border with extra space between icon and label. */
    public static final Style<Boolean> ICON_CUDDLE = newStyle(false, false);

    /** The sound to be played when this element's action is triggered. */
    public static final Style<Sound> ACTION_SOUND = newStyle(false, (Sound)null);

    /** Indicates whether or not this style property is inherited. */
    public final boolean inherited;

    /**
     * Creates a text format based on the supplied element's stylings.
     */
    public static TextFormat createTextFormat (Element<?> elem) {
        TextFormat format = new TextFormat().
            withFont(Styles.resolveStyle(elem, Style.FONT)).
            withAlignment(toAlignment(Styles.resolveStyle(elem, Style.HALIGN)));
        return format;
    }

    /**
     * Creates an effect renderer based on the supplied element's stylings.
     */
    public static EffectRenderer createEffectRenderer (Element<?> elem) {
        switch (Styles.resolveStyle(elem, Style.TEXT_EFFECT)) {
        case PIXEL_OUTLINE:
            return new EffectRenderer.PixelOutline(Styles.resolveStyle(elem, Style.HIGHLIGHT));
        case VECTOR_OUTLINE:
            return new EffectRenderer.VectorOutline(Styles.resolveStyle(elem, Style.HIGHLIGHT),
                                                    Styles.resolveStyle(elem, Style.OUTLINE_WIDTH),
                                                    Styles.resolveStyle(elem, Style.OUTLINE_CAP),
                                                    Styles.resolveStyle(elem, Style.OUTLINE_JOIN));
        case SHADOW:
            return new EffectRenderer.Shadow(Styles.resolveStyle(elem, Style.SHADOW),
                                             Styles.resolveStyle(elem, Style.SHADOW_X),
                                             Styles.resolveStyle(elem, Style.SHADOW_Y));
        default:
            return EffectRenderer.NONE;
        }
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
            public V getDefault (Element<?> elem) {
                return defaultValue;
            }
        };
    }

    protected Style (boolean inherited) {
        this.inherited = inherited;
    }

    protected static TextFormat.Alignment toAlignment (HAlign align) {
        switch (align) {
        default:
        case LEFT: return TextFormat.Alignment.LEFT;
        case RIGHT: return TextFormat.Alignment.RIGHT;
        case CENTER: return TextFormat.Alignment.CENTER;
        }
    }
}
