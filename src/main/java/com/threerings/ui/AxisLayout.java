//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import java.util.List;
import java.util.Map;

import pythagoras.f.Dimension;

/**
 * Lays out elements in a horizontal or vertical group. Separate policies are enforced for on-axis
 * and off-axis sizing.
 *
 * <p> On-axis, the available space is divided up as follows: non-stretched elements are given
 * their preferred size, and remaining space is divided up among the stretched elements
 * proportional to their configured weight (which defaults to one). If no stretched elements exist,
 * elements are aligned as specified. </p>
 *
 * <p> Off-axis sizing can be configured to either size elements to their preferred size, stretch
 * them all to a uniform size (equal to the preferred size of the largest element), or to stretch
 * them all to the size allotted to the container. When elements are not stretched to fill the size
 * allotted to the container, they may be aligned as desired. </p>
 */
public class AxisLayout extends Layout
{
    /** Specifies alignments of widgets, start is left/top and end is right/bottom. */
    public static enum Align { START, CENTER, END };

    /** Defines axis layout constraints. */
    public static class Constraint extends Layout.Constraint {
        /** Whether this element is stretched to consume extra space. */
        public final boolean stretch;

        /** The weight used when dividing up extra space among stretched elements. */
        public final float weight;

        public Constraint (boolean stretch, float weight) {
            this.stretch = stretch;
            this.weight = weight;
        }
    }

    /** A vertical axis layout. */
    public static class Vertical extends AxisLayout {
        /** Configures this layout's on-axis alignment to top. */
        public Vertical alignTop () {
            alignOn(Align.START);
            return this;
        }

        /** Configures this layout's on-axis alignment to bottom. */
        public Vertical alignBottom () {
            alignOn(Align.END);
            return this;
        }

        /** Configures this layout's off-axis alignment to left. */
        public Vertical alignLeft () {
            alignOff(Align.START);
            return this;
        }

        /** Configures this layout's off-axis alignment to right. */
        public Vertical alignRight () {
            alignOff(Align.END);
            return this;
        }
    }

    /** A horizontal axis layout. */
    public static class Horizontal extends AxisLayout {
        /** Configures this layout's on-axis alignment to left. */
        public Horizontal alignLeft () {
            alignOn(Align.START);
            return this;
        }

        /** Configures this layout's on-axis alignment to right. */
        public Horizontal alignRight () {
            alignOn(Align.END);
            return this;
        }

        /** Configures this layout's off-axis alignment to top. */
        public Horizontal alignTop () {
            alignOff(Align.START);
            return this;
        }

        /** Configures this layout's off-axis alignment to bottom. */
        public Horizontal alignBottom () {
            alignOff(Align.END);
            return this;
        }
    }

    /**
     * Creates a vertical axis layout with default alignments (center, center) and gap (5).
     */
    public static Vertical vertical () {
        return new Vertical();
    }

    /**
     * Creates a horizontal axis layout with default alignments (center, center) and gap (5).
     */
    public static Horizontal horizontal () {
        return new Horizontal();
    }

    /**
     * Returns a layout constraint indicating that the associated element should be stretched to
     * consume extra space, with weight 1.
     */
    public static Constraint stretched () {
        return STRETCHED_SINGLE;
    }

    /**
     * Returns a layout constraint indicating that the associated element should be stretched to
     * consume extra space, with the specified weight.
     */
    public static Constraint stretched (float weight) {
        return new Constraint(true, weight);
    }

    /**
     * Configures the on-axis alignment of this layout.
     */
    public AxisLayout alignOn (Align align) {
        _alignOn = align;
        return this;
    }

    /**
     * Configures the off-axis alignment of this layout.
     */
    public AxisLayout alignOff (Align align) {
        _alignOff = align;
        return this;
    }

    /**
     * Configures the inter-element gap, in pixels.
     */
    public AxisLayout gap (int gap) {
        _gap = gap;
        return this;
    }

    @Override public void computeSize (
        List<Element> elems, Map<Element, Layout.Constraint> constraints,
        float hintX, float hintY, Dimension into) {
        // TODO
    }

    @Override public void layout (
        List<Element> elems, Map<Element, Layout.Constraint> constraints,
        float width, float height) {
        // TODO
    }

    protected Align _alignOn = Align.CENTER, _alignOff = Align.CENTER;
    protected int _gap = 5;

    protected static final Constraint STRETCHED_SINGLE = new Constraint(true, 1);
}
