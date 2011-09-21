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
    /**
     * Creates a table layout with the specified number of columns.
     */
    public TableLayout (int columns) {
        _fixedColumns = new boolean[columns];
    }

    // TODO: specify whether each column stretches its elems or aligns them left/right/center
    // TODO: specify whether each row stretches its elems or aligns them top/bottom/center

    // /**
    //  * Configures a column as fixed or free. When laying out a table, the available width is
    //  * divided up among all of the non-fixed columns. All columns are non-fixed by default.
    //  */
    // public TableLayout fixedColumn (int column, boolean fixed) {
    //     _fixedColumns[column] = fixed;
    //     return this;
    // }

    // /**
    //  * Configures the table to force all rows to be a uniform size. Rows are sized to their
    //  * tallest element, by default.
    //  */
    // public TableLayout equalRows () {
    //     _equalRows = true;
    //     return this;
    // }

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
        return new Dimension(m.totalWidth() + (m.columns()-1) * _colgap,
                             m.totalHeight() + (m.rows()-1) * _rowgap);
    }

    @Override public void layout (Elements<?> elems,
                                  float left, float top, float width, float height) {
        Metrics m = computeMetrics(elems, width, height, false);
        int columns = m.columns(), row = 0, col = 0;
        float x = left, y = top; // TODO: account for group halign/valign
        Style.HAlign cellHAlign = Style.HAlign.CENTER; // TODO
        Style.VAlign cellVAlign = Style.VAlign.CENTER; // TODO
        for (Element<?> elem : elems) {
            float colWidth = m.columnWidths[col], rowHeight = m.rowHeights[row];
            if (elem.isVisible()) {
                IDimension psize = elem.preferredSize(0, 0); // will be cached
                float elemWidth = Math.min(psize.width(), colWidth);
                float elemHeight = Math.min(psize.height(), rowHeight);
                elem.setSize(elemWidth, elemHeight);
                elem.setLocation(x + cellHAlign.offset(elemWidth, colWidth),
                                 y + cellVAlign.offset(elemHeight, rowHeight));
            }
            x += (colWidth + _colgap);
            if (++col == columns) {
                col = 0;
                x = left;
                y += (rowHeight + _rowgap);
                row++;
            }
        }
    }

    protected Metrics computeMetrics (Elements<?> elems, float hintX, float hintY,
                                      boolean preferred) {
        int columns = _fixedColumns.length;
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

        // divvy up remaining space based on our preferred column widths (however, no adjusting if
        // we're computing our preferred size)

        // if (!preferred) {
        //     float naturalWidth = metrics.totalWidth();

        //     // sum the width of the non-fixed columns
        //     float freewid = 0;
        //     for (int ii = 0; ii < _fixedColumns.length; ii++) {
        //         if (!_fixedColumns[ii]) freewid += metrics.columnWidths[ii];
        //     }

        //     // now divide up the extra space among said non-fixed columns
        //     float avail = hintX - naturalWidth - (_colgap * (columns-1));
        //     int used = 0;
        //     for (int ii = 0; ii < metrics.columnWidths.length; ii++) {
        //         if (_fixedColumns[ii]) {
        //             continue;
        //         }
        //         float adjust = metrics.columnWidths[ii] * avail / freewid;
        //         metrics.columnWidths[ii] += adjust;
        //         used += adjust;
        //     }

        //     // add any rounding error to the first non-fixed column
        //     if (metrics.columnWidths.length > 0) {
        //         for (int ii = 0; ii < _fixedColumns.length; ii++) {
        //             if (!_fixedColumns[ii]) {
        //                 metrics.columnWidths[ii] += (avail - used);
        //                 break;
        //             }
        //         }
        //     }
        // }

        // // if we're equalizing rows, make all row heights the max
        // if (_equalRows) {
        //     Arrays.fill(metrics.rowHeights, maxrh);
        // }

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
        public float totalWidth () {
            return sum(columnWidths);
        }
        public float totalHeight () {
            return sum(rowHeights);
        }
    }

    protected static final float sum (float[] values) {
        float total = 0;
        for (float value : values) {
            total += value;
        }
        return total;
    }

    // protected final boolean[] _fixedColumns;
    // protected boolean _equalRows;
    protected int _rowgap, _colgap;
}
