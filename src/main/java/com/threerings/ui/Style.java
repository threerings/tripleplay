//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import forplay.core.Font;
import forplay.core.ForPlay;
import forplay.core.TextFormat;

/**
 * Defines style properties for interface elements. Some style properties are inherited, such that
 * a property not specified in a leaf element will be inherited by the nearest parent for which the
 * property is specified. Other style properties are not inherited, and a default value will be
 * used in cases where a leaf element lacks a property. The documentation for each style property
 * indicates whether or not it is inherited.
 */
public abstract class Style<V>
{
    /** Defines horizontal alignment choices. */
    public static enum HAlign { LEFT, RIGHT, CENTER };

    /** Defines vertical alignment choices. */
    public static enum VAlign { TOP, BOTTOM, CENTER };

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
        true, ForPlay.graphics().createFont("Helvetica", Font.Style.PLAIN, 16));

    /** The effect to use when rendering text, if any. Inherited. */
    public static final Style<TextEffect> TEXT_EFFECT = newStyle(true, TextEffect.NONE);

    /** Indicates whether or not this style property is inherited. */
    public final boolean inherited;

    /**
     * Creates a text format based on the supplied element's stylings.
     */
    public static TextFormat createTextFormat (Element elem, Element.State state) {
        TextFormat format = new TextFormat().
            withFont(elem.getStyle(Style.FONT, state)).
            withTextColor(elem.getStyle(Style.COLOR, state)).
            withAlignment(toAlignment(elem.getStyle(Style.HALIGN, state)));
        switch (elem.getStyle(Style.TEXT_EFFECT, state)) {
        case OUTLINE:
            format = format.withEffect(
                TextFormat.Effect.outline(elem.getStyle(Style.HIGHLIGHT, state)));
            break;
        case SHADOW:
            format = format.withEffect(
                TextFormat.Effect.shadow(elem.getStyle(Style.SHADOW, state), SHADOW_X, SHADOW_Y));
            break;
        }
        return format;
    }

    /**
     * Returns the default value for this style in the given state.
     */
    public abstract V getDefault (Element.State state);

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
