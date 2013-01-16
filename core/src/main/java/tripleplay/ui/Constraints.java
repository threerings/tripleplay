//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.PlayN;
import playn.core.TextFormat;
import playn.core.TextLayout;

import pythagoras.f.Dimension;

/**
 * Provides various user interface constraints.
 */
public class Constraints
{
    /** A special layout constraint used by {@link TextWidget}s which adjusts only the text size of
     * the widget, leaving the remaining dimensions (icon, insets, etc.) unmodified. This is an
     * implementation detail that can be safely ignored unless you are implementing your own custom
     * text constraints. */
    public static abstract class TextConstraint extends Layout.Constraint {
        /** Adds the appropriate text dimensions to the supplied size.
         * @param layout the text widget's currently laid out text, may be null. */
        public abstract void addTextSize (Dimension tsize, TextLayout layout);
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width to the specified value.
     */
    public static Layout.Constraint fixedWidth (final float width) {
        return new Layout.Constraint() {
            @Override public void adjustPreferredSize (Dimension psize, float hintX, float hintY) {
                psize.width = width;
            }
        };
    }

    /**
     * Returns a layout constraint that forces the widget's preferred height to the specified value.
     */
    public static Layout.Constraint fixedHeight (final float height) {
        return new Layout.Constraint() {
            @Override public void adjustPreferredSize (Dimension psize, float hintX, float hintY) {
                psize.height = height;
            }
        };
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width height to the specified
     * values.
     */
    public static Layout.Constraint fixedSize (final float width, final float height) {
        return new Layout.Constraint() {
            @Override public void adjustPreferredSize (Dimension psize, float hintX, float hintY) {
                psize.width = width;
                psize.height = height;
            }
        };
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width to be no less than the
     * specified value.
     */
    public static Layout.Constraint minWidth (final float width) {
        return new Layout.Constraint() {
            @Override public void adjustPreferredSize (Dimension psize, float hintX, float hintY) {
                psize.width = Math.max(psize.width, width);
            }
        };
    }

    /**
     * Returns a layout constraint that forces the widget's preferred height to be no less than the
     * specified value.
     */
    public static Layout.Constraint minHeight (final float height) {
        return new Layout.Constraint() {
            @Override public void adjustPreferredSize (Dimension psize, float hintX, float hintY) {
                psize.height = Math.max(psize.height, height);
            }
        };
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width height to be no less
     * than the specified values.
     */
    public static Layout.Constraint minSize (final float width, final float height) {
        return new Layout.Constraint() {
            @Override public void adjustPreferredSize (Dimension psize, float hintX, float hintY) {
                psize.width = Math.max(psize.width, width);
                psize.height = Math.max(psize.height, height);
            }
        };
    }

    /**
     * Returns a layout constriant that forces the widget's preferred size to be no less than what
     * is needed to accommodate the supplied template text string. This is useful for configuring
     * the size of text widgets to be that of a largest-possible value.
     */
    public static Layout.Constraint minSize (String text) {
        return new TemplateTextConstraint(text) {
            @Override protected void addTextSize (
                Dimension tsize, TextLayout layout, TextLayout tmplLayout) {
                float lwidth = (layout == null) ? 0 : layout.width();
                float lheight = (layout == null) ? 0 : layout.height();
                tsize.width += Math.max(lwidth, tmplLayout.width());
                tsize.height += Math.max(lheight, tmplLayout.height());
            }
        };
    }

    /**
     * Returns a layout constriant that forces the widget's preferred size to be precisely what is
     * needed to accommodate the supplied template text string. This is useful for configuring the
     * size of text widgets to be that of a largest-possible value.
     */
    public static Layout.Constraint fixedSize (String text) {
        return new TemplateTextConstraint(text) {
            @Override protected void addTextSize (
                Dimension tsize, TextLayout layout, TextLayout tmplLayout) {
                tsize.width += tmplLayout.width();
                tsize.height += tmplLayout.height();
            }
        };
    }

    protected static abstract class TemplateTextConstraint extends TextConstraint {
        public TemplateTextConstraint (String tmpl) {
            _tmpl = tmpl;
        }

        @Override public void setElement (Element<?> elem) {
            _elem = elem;
        }

        @Override public void addTextSize (Dimension tsize, TextLayout layout) {
            TextFormat format = Style.createTextFormat(_elem);
            addTextSize(tsize, layout, PlayN.graphics().layoutText(_tmpl, format));
        }

        protected abstract void addTextSize (
            Dimension tsize, TextLayout layout, TextLayout tmplLayout);

        protected final String _tmpl;
        protected Element<?> _elem;
    }
}
