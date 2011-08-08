//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Font;
import playn.core.PlayN;
import playn.core.TextFormat;

import tripleplay.ui.bgs.NullBackground;

/**
 * Defines style properties for interface elements. Some style properties are inherited, such that
 * a property not specified in a leaf element will be inherited by the nearest parent for which the
 * property is specified. Other style properties are not inherited, and a default value will be
 * used in cases where a leaf element lacks a property. The documentation for each style property
 * indicates whether or not it is inherited.
 */
public abstract class Style<V>
{
    /** Used to configure {@link Styles} instances. See {@link Styles#set}. */
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
            public float getOffset (float size, float extent) {
                return 0;
            }
        }, RIGHT {
            public float getOffset (float size, float extent) {
                return (extent - size);
            }
        }, CENTER {
            public float getOffset (float size, float extent) {
                return (extent - size)/2;
            }
        };

        public abstract float getOffset (float size, float extent);
    };

    /** Defines vertical alignment choices. */
    public static enum VAlign {
        TOP {
            public float getOffset (float size, float extent) {
                return 0;
            }
        }, BOTTOM {
            public float getOffset (float size, float extent) {
                return (extent - size);
            }
        }, CENTER {
            public float getOffset (float size, float extent) {
                return (extent - size)/2;
            }
        };

        public abstract float getOffset (float size, float extent);
    };

    /** Defines icon position choices. */
    public static enum Pos {
        LEFT, ABOVE, RIGHT, BELOW;
    }

    /** Defines supported text effects. */
    public static enum TextEffect {
        /** Outlines the text in the highlight color. */
        OUTLINE,
        /** Draws a shadow below and to the right of the text in the shadow color. */
        SHADOW,
        /** No text effect. */
        NONE };

    /** The foreground color for an element. Inherited. */
    public static final Style<Integer> COLOR = new Style<Integer>(true) {
        public Integer getDefault (Element.State state) {
            switch (state) {
            case DISABLED: return 0xCC000000;
            default:       return 0xFF000000;
            }
        }
    };

    /** The highlight color for an element. Inherited. */
    public static final Style<Integer> HIGHLIGHT = new Style<Integer>(true) {
        public Integer getDefault (Element.State state) {
            switch (state) {
            case DISABLED: return 0xAACCCCCC;
            default:       return 0xAAFFFFFF;
            }
        }
    };

    /** The shadow color for an element. Inherited. */
    public static final Style<Integer> SHADOW = newStyle(true, 0x55000000);

    /** The horizontal alignment of an element. Not inherited. */
    public static final Style<HAlign> HALIGN = newStyle(false, HAlign.LEFT);

    /** The vertical alignment of an element. Not inherited. */
    public static final Style<VAlign> VALIGN = newStyle(false, VAlign.TOP);

    /** The font used to render text. Inherited. */
    public static final Style<Font> FONT = newStyle(
        true, PlayN.graphics().createFont("Helvetica", Font.Style.PLAIN, 16));

    /** The effect to use when rendering text, if any. Inherited. */
    public static final Style<TextEffect> TEXT_EFFECT = newStyle(true, TextEffect.NONE);

    /** The background for an element. Not inherited. */
    public static final Style<Background> BACKGROUND = newStyle(
        false, (Background)new NullBackground());

    /** The position relative to the text to render an icon for labels, buttons, etc. */
    public static final Style<Pos> ICON_POS = newStyle(false, Pos.LEFT);

    /** The gap between the icon and text in labels, buttons, etc. */
    public static final Style<Integer> ICON_GAP = newStyle(false, 2);

    /** Indicates whether or not this style property is inherited. */
    public final boolean inherited;

    /**
     * Creates a text format based on the supplied element's stylings.
     */
    public static TextFormat createTextFormat (Element elem, Element.State state) {
        TextFormat format = new TextFormat().
            withFont(Styles.resolveStyle(elem, state, Style.FONT)).
            withTextColor(Styles.resolveStyle(elem, state, Style.COLOR)).
            withAlignment(toAlignment(Styles.resolveStyle(elem, state, Style.HALIGN)));
        switch (Styles.resolveStyle(elem, state, Style.TEXT_EFFECT)) {
        case OUTLINE:
            format = format.withEffect(
                TextFormat.Effect.outline(Styles.resolveStyle(elem, state, Style.HIGHLIGHT)));
            break;
        case SHADOW:
            format = format.withEffect(
                TextFormat.Effect.shadow(
                    Styles.resolveStyle(elem, state, Style.SHADOW), SHADOW_X, SHADOW_Y));
            break;
        }
        return format;
    }

    /**
     * Returns the default value for this style in the given state.
     */
    public abstract V getDefault (Element.State state);

    /**
     * Returns a {@link Binding} with this style bound to the specified value.
     */
    public Binding<V> is (V value) {
        return new Binding<V>(this, value);
    }

    protected Style (boolean inherited) {
        this.inherited = inherited;
    }

    protected static <V> Style<V> newStyle (boolean inherited, final V defaultValue) {
        return new Style<V>(inherited) {
            public V getDefault (Element.State state) {
                return defaultValue;
            }
        };
    }

    protected static TextFormat.Alignment toAlignment (HAlign align) {
        switch (align) {
        default:
        case LEFT: return TextFormat.Alignment.LEFT;
        case RIGHT: return TextFormat.Alignment.RIGHT;
        case CENTER: return TextFormat.Alignment.CENTER;
        }
    }

    // TODO: make these configurable somehow/where
    protected static final int SHADOW_X = 2, SHADOW_Y = 2;
}
