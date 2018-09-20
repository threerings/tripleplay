//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Graphics;
import playn.core.TextLayout;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

import tripleplay.util.TextStyle;

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
         * @param into the constrained size will be written into this instance.
         * @param lsize the size of the currently laid out text, may be null. */
        public abstract void addTextSize (Dimension into, IDimension lsize);
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width to the specified value.
     */
    public static Layout.Constraint fixedWidth (final float width) {
        return new Layout.Constraint() {
            @Override public float adjustHintX (float hintX) {
                return Math.min(width, hintX);
            }
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
            @Override public float adjustHintY (float hintY) {
                return Math.min(height, hintY);
            }
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
            @Override public float adjustHintX (float hintX) {
                return Math.min(width, hintX);
            }
            @Override public float adjustHintY (float hintY) {
                return Math.min(height, hintY);
            }
            @Override public void adjustPreferredSize (Dimension psize, float hintX, float hintY) {
                psize.width = width;
                psize.height = height;
            }
        };
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width to be no more than the
     * specified value.
     */
    public static Layout.Constraint maxWidth (final float width) {
        return new Layout.Constraint() {
            @Override public float adjustHintX (float hintX) {
                return Math.min(width, hintX);
            }
            @Override public void adjustPreferredSize (Dimension psize, float hintX, float hintY) {
                psize.width = Math.min(psize.width, width);
            }
        };
    }

    /**
     * Returns a layout constraint that forces the widget's preferred height to be no more than the
     * specified value.
     */
    public static Layout.Constraint maxHeight (final float height) {
        return new Layout.Constraint() {
            @Override public float adjustHintY (float hintY) {
                return Math.min(height, hintY);
            }
            @Override public void adjustPreferredSize (Dimension psize, float hintX, float hintY) {
                psize.height = Math.min(psize.height, height);
            }
        };
    }

    /**
     * Returns a layout constraint that forces the widget's preferred width height to be no more
     * than the specified values.
     */
    public static Layout.Constraint maxSize (final float width, final float height) {
        return new Layout.Constraint() {
            @Override public float adjustHintX (float hintX) {
                return Math.min(width, hintX);
            }
            @Override public float adjustHintY (float hintY) {
                return Math.min(height, hintY);
            }
            @Override public void adjustPreferredSize (Dimension psize, float hintX, float hintY) {
                psize.width = Math.min(psize.width, width);
                psize.height = Math.min(psize.height, height);
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
    public static Layout.Constraint minSize (Graphics gfx, String text) {
        return new TemplateTextConstraint(gfx, text) {
            @Override protected void addTextSize (
                Dimension into, IDimension lsize, TextLayout tmplLayout) {
                float lwidth = (lsize == null) ? 0 : lsize.width();
                float lheight = (lsize == null) ? 0 : lsize.height();
                into.width += Math.max(lwidth, tmplLayout.size.width());
                into.height += Math.max(lheight, tmplLayout.size.height());
            }
        };
    }

    /**
     * Returns a layout constriant that forces the widget's preferred size to be precisely what is
     * needed to accommodate the supplied template text string. This is useful for configuring the
     * size of text widgets to be that of a largest-possible value.
     */
    public static Layout.Constraint fixedSize (Graphics gfx, String text) {
        return new TemplateTextConstraint(gfx, text) {
            @Override protected void addTextSize (
                Dimension into, IDimension lsize, TextLayout tmplLayout) {
                into.width += tmplLayout.size.width();
                into.height += tmplLayout.size.height();
            }
        };
    }

    protected static abstract class TemplateTextConstraint extends TextConstraint {
        public TemplateTextConstraint (Graphics gfx, String tmpl) {
            _gfx = gfx;
            _tmpl = tmpl;
        }

        @Override public void setElement (Element<?> elem) {
            _elem = elem;
        }

        @Override public void addTextSize (Dimension into, IDimension lsize) {
            TextStyle style = Style.createTextStyle(_elem);
            addTextSize(into, lsize, _gfx.layoutText(_tmpl, style));
        }

        protected abstract void addTextSize (
            Dimension into, IDimension lsize, TextLayout tmplLayout);

        protected final Graphics _gfx;
        protected final String _tmpl;
        protected Element<?> _elem;
    }
}
