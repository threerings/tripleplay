//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.layout;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import tripleplay.ui.Container;
import tripleplay.ui.Element;
import tripleplay.ui.Layout;
import tripleplay.ui.Style;

/**
 * Lays out elements in horizontal rows, starting a new row when a width limit is reached. By
 * default, the hint width is used as the limit; this can be overridden with a fixed value.
 * <p>TODO: vertical</p>
 */
public class FlowLayout extends Layout
{
    /** The default gap between rows and elements in a row. */
    public static final float DEFAULT_GAP = 5;

    /**
     * Sets the maximum width of a row of elements. This should normally be used whenever a flow
     * layout governs {@code Elements} that have horizontal siblings. By default, the hint width
     * is used.
     */
    public FlowLayout wrapAt (float width) {
        _wrapWidth = width;
        return this;
    }

    /**
     * Sets the gap, in pixels, to use between rows and between elements within a row.
     */
    public FlowLayout gaps (float gap) {
        _hgap = _vgap = gap;
        return this;
    }

    /**
     * Sets the gap, in pixels, to use between rows and between elements within a row.
     * @param hgap the gap to use between elements in a row
     * @param vgap the gap to use between rows
     */
    public FlowLayout gaps (float hgap, float vgap) {
        _hgap = hgap;
        _vgap = vgap;
        return this;
    }

    /**
     * Sets the alignment used for positioning elements within their row. By default, elements are
     * not stretched and centered vertically: {@link tripleplay.ui.Style.VAlign#CENTER}.
     */
    public FlowLayout align (Style.VAlign align) {
        _valign = align;
        return this;
    }

    /**
     * Stretch elements vertically to the maximum height of other elements in the same row. This
     * clears any previously set vertical alignment.
     */
    public FlowLayout stretch () {
        _valign = null;
        return this;
    }

    @Override public Dimension computeSize (Container<?> elems, float hintX, float hintY) {
        Metrics m = computeMetrics(elems, hintX, hintY);
        return m.size;
    }

    @Override public void layout (Container<?> elems,
                                  float left, float top, float width, float height) {
        Style.HAlign halign = resolveStyle(elems, Style.HALIGN);
        Metrics m = computeMetrics(elems, width, height);
        float y = top + resolveStyle(elems, Style.VALIGN).offset(m.size.height, height);
        for (int elemIdx = 0, row = 0, size = m.rowBreaks.size(); row < size; ++row) {
            Dimension rowSize = m.rows.get(row);
            float x = left + halign.offset(rowSize.width, width);
            for (; elemIdx < m.rowBreaks.get(row).intValue(); ++elemIdx) {
                Element<?> elem = elems.childAt(elemIdx);
                if (!elem.isVisible()) continue;
                IDimension esize = preferredSize(elem, width, height);
                if (_valign == null) {
                    setBounds(elem, x, y, esize.width(), rowSize.height());
                } else {
                    setBounds(elem, x, y + _valign.offset(esize.height(), rowSize.height()),
                        esize.width(), esize.height());
                }
                x += esize.width() + _hgap;
            }
            y += _vgap + rowSize.height;
        }
    }

    protected Metrics computeMetrics (Container<?> elems, float width, float height) {
        Metrics m = new Metrics();

        // adjust our maximum width if appropriate
        if (_wrapWidth != null) width = _wrapWidth;

        // fill in components horizontally, breaking rows as needed
        Dimension rowSize = new Dimension();
        for (int ii = 0, ll = elems.childCount(); ii < ll; ++ii) {
            Element<?> elem = elems.childAt(ii);
            if (!elem.isVisible()) continue;
            IDimension esize = preferredSize(elem, width, height);
            if (rowSize.width > 0 && width > 0 && rowSize.width + _hgap + esize.width() > width) {
                m.addBreak(ii, rowSize);
                rowSize = new Dimension(esize);
            } else {
                rowSize.width += (rowSize.width > 0 ? _hgap : 0) + esize.width();
                rowSize.height = Math.max(esize.height(), rowSize.height);
            }
        }
        m.addBreak(elems.childCount(), rowSize);
        return m;
    }

    public class Metrics
    {
        public Dimension size = new Dimension();
        public List<Dimension> rows = new ArrayList<Dimension>();
        public List<Integer> rowBreaks = new ArrayList<Integer>();

        protected void addBreak (int idx, Dimension lastRowSize) {
            if (lastRowSize.height == 0 && lastRowSize.width == 0) return;
            rowBreaks.add(idx);
            rows.add(lastRowSize);
            size.height += (size.height > 0 ? _vgap : 0) + lastRowSize.height;
            size.width = Math.max(size.width, lastRowSize.width);
        }
    }

    protected float _hgap = DEFAULT_GAP, _vgap = DEFAULT_GAP;
    protected Float _wrapWidth;
    protected Style.VAlign _valign = Style.VAlign.CENTER;
}
