//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

/**
 * Lays out elements in a simple tabular form, where each column has uniform width and each row
 * uniform height. Frills are kept to a minimum.
 */
public class TableLayout extends Layout
{
    /** A configurator for a table column. */
    public static class Column {
        /** Configures this column as left-aligned. */
        public Column alignLeft () {
            _halign = Style.HAlign.LEFT;
            return this;
        }

        /** Configures this column as right-aligned. */
        public Column alignRight () {
            _halign = Style.HAlign.RIGHT;
            return this;
        }

        /** Configures this column's width as fixed to the width of its widest element. By default
         * columns are 'free' and may be configured as wider than their default to accommodate
         * excess width available to the table. */
        public Column fixed () {
            _fixed = true;
            return this;
        }

        /** Configures this column to stretch the width of its elements to the column width. By
         * default elements are configured to their preferred width. */
        public Column stretch () {
            _stretch = true;
            return this;
        }

        protected Style.HAlign _halign = Style.HAlign.CENTER;
        protected boolean _fixed, _stretch;
    }

    /**
     * Creates a table layout with the specified number of columns.
     */
    public TableLayout (int columns) {
        _columns = new Column[columns];
    }

    /**
     * Returns an object that can be used to configure the behavior of the specified column.
     */
    public Column column (int column) {
        if (_columns[column] == null) {
            _columns[column] = new Column();
        }
        return _columns[column];
    }

    /**
     * Configures the gap between successive rows and successive columns. The default gap is zero.
     */
    public TableLayout gaps (int rowgap, int colgap) {
        _rowgap = rowgap;
        _colgap = colgap;
        return this;
    }

    @Override public Dimension computeSize (Elements<?> elems, float hintX, float hintY) {
        Metrics m = computeMetrics(elems, hintX, hintY, true);
        return new Dimension(m.totalWidth(_colgap), m.totalHeight(_rowgap));
    }

    @Override public void layout (Elements<?> elems,
                                  float left, float top, float width, float height) {
        Metrics m = computeMetrics(elems, width, height, false);
        int columns = m.columns(), row = 0, col = 0;

        float naturalWidth = m.totalWidth(_colgap);
        int freeColumns = 0;
        for (int ii = 0; ii < columns; ii++) {
            if (!colcfg(ii)._fixed) freeColumns++;
        }
        float freeExtra = (width - naturalWidth) / freeColumns;
        // freeExtra may end up negative; if our natural width is too wide

        Style.HAlign halign = elems.resolveStyle(Style.HALIGN);
        float startX = left + ((freeColumns == 0) ? halign.offset(naturalWidth, width) : 0);
        float x = startX;

        Style.VAlign valign = elems.resolveStyle(Style.VALIGN);
        float y = top + valign.offset(m.totalHeight(_rowgap), height);

        Style.VAlign cellVAlign = Style.VAlign.CENTER; // TODO
        for (Element<?> elem : elems) {
            Column ccfg = colcfg(col);
            float colWidth = Math.max(0, m.columnWidths[col] + (ccfg._fixed ? 0 : freeExtra));
            float rowHeight = m.rowHeights[row];
            if (colWidth > 0 && elem.isVisible()) {
                IDimension psize = elem.preferredSize(0, 0); // will be cached, hints ignored
                float elemWidth = ccfg._stretch ? colWidth : Math.min(psize.width(), colWidth);
                float elemHeight = Math.min(psize.height(), rowHeight);
                elem.setSize(elemWidth, elemHeight);
                elem.setLocation(x + ccfg._halign.offset(elemWidth, colWidth),
                                 y + cellVAlign.offset(elemHeight, rowHeight));
            }
            x += (colWidth + _colgap);
            if (++col == columns) {
                col = 0;
                x = startX;
                y += (rowHeight + _rowgap);
                row++;
            }
        }
    }

    protected Metrics computeMetrics (Elements<?> elems, float hintX, float hintY,
                                      boolean preferred) {
        int columns = _columns.length;
        int rows = elems.childCount() / columns;
        if (elems.childCount() % columns != 0) rows++;

        Metrics metrics = new Metrics();
        metrics.columnWidths = new float[columns];
        metrics.rowHeights = new float[rows];

        int row = 0, col = 0;
        float maxrh = 0;
        for (Element<?> elem : elems) {
            if (elem.isVisible()) {
                // TODO: supply sane x/y hints
                IDimension psize = elem.preferredSize(hintX, hintY);
                metrics.rowHeights[row] = Math.max(metrics.rowHeights[row], psize.height());
                metrics.columnWidths[col] = Math.max(metrics.columnWidths[col], psize.width());
            }
            if (++col == columns) {
                maxrh = Math.max(maxrh, metrics.rowHeights[row]);
                col = 0;
                row++;
            }
        }

        return metrics;
    }

    protected Column colcfg (int index) {
        Column col = _columns[index];
        return (col == null) ? DEF_COLUMN : col;
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

    protected static final float sum (float[] values) {
        float total = 0;
        for (float value : values) {
            total += value;
        }
        return total;
    }

    protected final Column[] _columns;
    protected int _rowgap, _colgap;

    protected static final Column DEF_COLUMN = new Column();
}
