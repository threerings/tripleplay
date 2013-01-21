//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.layout;

import java.util.Arrays;

import playn.core.Asserts;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

import tripleplay.ui.Element;
import tripleplay.ui.Elements;
import tripleplay.ui.Layout;
import tripleplay.ui.Style;

/**
 * Lays out elements in a simple tabular form, where each row has uniform height.
 * Frills are kept to a minimum.
 */
public class TableLayout extends Layout
{
    /** The default column configuration. */
    public static final Column COL = new Column(Style.HAlign.CENTER, false, false, 0);

    /** A configurator for a table column. */
    public static class Column {
        protected Column (Style.HAlign halign, boolean fixed, boolean stretch, float minWidth) {
            _halign = halign;
            _fixed = fixed;
            _stretch = stretch;
            _minWidth = minWidth;
        }

        /** Configures this column as left-aligned. */
        public Column alignLeft () {
            return new Column(Style.HAlign.LEFT, _fixed, _stretch, _minWidth);
        }

        /** Configures this column as right-aligned. */
        public Column alignRight () {
            return new Column(Style.HAlign.RIGHT, _fixed, _stretch, _minWidth);
        }

        /** Configures this column's width as fixed to the width of its widest element. By default
         * columns are 'free' and may be configured as wider than their default to accommodate
         * excess width available to the table. */
        public Column fixed () {
            return new Column(_halign, true, _stretch, _minWidth);
        }

        /** Configures this column to stretch the width of its elements to the column width. By
         * default elements are configured to their preferred width. */
        public Column stretch () {
            return new Column(_halign, _fixed, true, _minWidth);
        }

        /** Configures the minimum width of this column. This column will not be allowed to shrink
         * below its minimum width unless the total table width is insufficient to satisfy the
         * minimum width requirements of all of its columns. */
        public Column minWidth (float minWidth) {
            return new Column(_halign, _fixed, _stretch, minWidth);
        }

        /** Returns {@code count} copies of this column. */
        public Column[] copy (int count) {
            Column[] cols = new Column[count];
            Arrays.fill(cols, this);
            return cols;
        }

        protected final Style.HAlign _halign;
        protected final boolean _fixed, _stretch;
        protected final float _minWidth;
    }

    /** Defines a colspan constraint. */
    public static class Colspan extends Layout.Constraint {
        /** The number of columns spanned by this element. */
        public final int colspan;

        public Colspan (int colspan) {
            Asserts.checkArgument(colspan >= 1, "Colspan must be >= 1");
            this.colspan = colspan;
        }
    }

    /**
     * Creates an array of {@code columns} columns, each with default configuration.
     */
    public static Column[] columns (int count) {
        return COL.copy(count);
    }

    /**
     * Configures a colspan constraint on {@code elem}.
     */
    public static <T extends Element<?>> T colspan (T elem, int colspan) {
        elem.setConstraint(new Colspan(colspan));
        return elem;
    }

    /**
     * Creates a table layout with the specified number of columns, each with the default
     * configuration.
     */
    public TableLayout (int columns) {
        this(columns(columns));
    }

    /**
     * Creates a table layout with the specified columns.
     */
    public TableLayout (Column... columns) {
        _columns = columns;
    }

    /**
     * Configures the gap between successive rows and successive columns. The default gap is zero.
     */
    public TableLayout gaps (int rowgap, int colgap) {
        _rowgap = rowgap;
        _colgap = colgap;
        return this;
    }

    /**
     * Configures the vertical alignment of cells to the top of their row.
     */
    public TableLayout alignTop () {
        _rowVAlign = Style.VAlign.TOP;
        return this;
    }

    /**
     * Configures the vertical alignment of cells to the bottom of their row.
     */
    public TableLayout alignBottom () {
        _rowVAlign = Style.VAlign.BOTTOM;
        return this;
    }

    /**
     * Configures cells to be stretched vertically to take up the entire height of their row.
     */
    public TableLayout fillHeight () {
        _vstretch = true;
        return this;
    }

    /**
     * Returns the number of columns configured for this table.
     */
    public int columns () {
        return _columns.length;
    }

    @Override public Dimension computeSize (Elements<?> elems, float hintX, float hintY) {
        Metrics m = computeMetrics(elems, hintX, hintY);
        return new Dimension(m.totalWidth(_colgap), m.totalHeight(_rowgap));
    }

    @Override public void layout (Elements<?> elems,
                                  float left, float top, float width, float height) {
        Metrics m = computeMetrics(elems, width, height);
        int columns = m.columns(), row = 0, col = 0;

        float naturalWidth = m.totalWidth(_colgap);
        int freeColumns = freeColumns();
        float freeExtra = (width - naturalWidth) / freeColumns;
        // freeExtra may end up negative; if our natural width is too wide

        Style.HAlign halign = resolveStyle(elems, Style.HALIGN);
        float startX = left + ((freeColumns == 0) ? halign.offset(naturalWidth, width) : 0);
        float x = startX;

        Style.VAlign valign = resolveStyle(elems, Style.VALIGN);
        float y = top + valign.offset(m.totalHeight(_rowgap), height);

        for (Element<?> elem : elems) {
            int colspan = colspan(elem);
            Asserts.checkState(col + colspan <= columns);

            float colWidth = 0;
            for (int ii = 0; ii < colspan; ii++) {
                colWidth += Math.max(0,
                    m.columnWidths[col + ii] + (_columns[col + ii]._fixed ? 0 : freeExtra));
            }

            Column ccfg = _columns[col];
            float rowHeight = m.rowHeights[row];
            if (colWidth > 0 && elem.isVisible()) {
                IDimension psize = preferredSize(elem, 0, 0); // will be cached, hints ignored
                float elemWidth = (colspan > 1 || ccfg._stretch) ? colWidth :
                    Math.min(psize.width(), colWidth);
                float elemHeight = _vstretch ? rowHeight : Math.min(psize.height(), rowHeight);
                setBounds(elem, x + ccfg._halign.offset(elemWidth, colWidth),
                          y + _rowVAlign.offset(elemHeight, rowHeight), elemWidth, elemHeight);
            }
            x += (colWidth + _colgap);
            if ((col += colspan) == columns) {
                col = 0;
                x = startX;
                y += (rowHeight + _rowgap);
                row++;
            }
        }
    }

    protected int freeColumns () {
        int freeColumns = 0;
        for (int ii = 0; ii < _columns.length; ii++) {
            if (!_columns[ii]._fixed) freeColumns++;
        }
        return freeColumns;
    }

    protected Metrics computeMetrics (Elements<?> elems, float hintX, float hintY) {
        int columns = _columns.length;
        int cells = 0;
        for (Element<?> elem : elems) cells += colspan(elem);
        int rows = cells / columns;
        if (cells % columns != 0) rows++;

        Metrics metrics = new Metrics();
        metrics.columnWidths = new float[columns];
        metrics.rowHeights = new float[rows];

        // note the minimum width constraints
        for (int cc = 0; cc < columns; cc++) metrics.columnWidths[cc] = _columns[cc]._minWidth;

        // compute the preferred size of the fixed columns
        int ii = 0;
        for (Element<?> elem : elems) {
            int col = ii % columns, row = ii / columns;
            if (elem.isVisible() && _columns[col]._fixed) {
                IDimension psize = preferredSize(elem, hintX, hintY);
                metrics.rowHeights[row] = Math.max(metrics.rowHeights[row], psize.height());
                metrics.columnWidths[col] = Math.max(metrics.columnWidths[col], psize.width());
            }
            ii += colspan(elem);
        }

        // determine the total width needed by the fixed columns, then compute the hint given to
        // free columns based on the remaining space
        float fixedWidth = _colgap*(columns-1); // start with gaps, add fixed col widths
        for (int cc = 0; cc < columns; cc++) fixedWidth += metrics.columnWidths[cc];
        float freeHintX = (hintX - fixedWidth) / freeColumns();

        ii = 0;
        for (Element<?> elem : elems) {
            int col = ii % columns, row = ii / columns;
            if (elem.isVisible() && !_columns[col]._fixed) {
                // TODO: supply sane y hint?
                IDimension psize = preferredSize(elem, freeHintX, hintY);
                metrics.rowHeights[row] = Math.max(metrics.rowHeights[row], psize.height());
                metrics.columnWidths[col] = Math.max(metrics.columnWidths[col], psize.width());
            }
            ii += colspan(elem);
        }

        return metrics;
    }

    protected static class Metrics {
        public float[] columnWidths;
        public float[] rowHeights;

        public int columns () {
            return columnWidths.length;
        }
        public int rows () {
            return rowHeights.length;
        }

        public float totalWidth (float gap) {
            return sum(columnWidths) + gap*(columns()-1);
        }
        public float totalHeight (float gap) {
            return sum(rowHeights) + gap*(rows()-1);
        }
    }

    protected static int colspan (Element<?> elem) {
        Layout.Constraint constraint = elem.constraint();
        return (constraint instanceof Colspan) ? ((Colspan)constraint).colspan : 1;
    }

    protected static float sum (float[] values) {
        float total = 0;
        for (float value : values) {
            total += value;
        }
        return total;
    }

    protected final Column[] _columns;
    protected int _rowgap, _colgap;
    protected boolean _vstretch;
    protected Style.VAlign _rowVAlign = Style.VAlign.CENTER;
}
