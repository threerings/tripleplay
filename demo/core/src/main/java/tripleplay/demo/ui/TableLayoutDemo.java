//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import java.util.ArrayList;
import java.util.List;

import playn.core.Pointer;
import react.Slot;
import tripleplay.demo.DemoScreen;
import tripleplay.ui.Background;
import tripleplay.ui.Button;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Shim;
import tripleplay.ui.SizableGroup;
import tripleplay.ui.Slider;
import tripleplay.ui.Style;
import tripleplay.ui.Style.HAlign;
import tripleplay.ui.Styles;
import tripleplay.ui.ToggleButton;
import tripleplay.ui.ValueLabel;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.FlowLayout;
import tripleplay.ui.layout.TableLayout;
import tripleplay.ui.layout.TableLayout.Column;
import tripleplay.util.Colors;

public class TableLayoutDemo extends DemoScreen
{
    static class ExposedColumn extends Column
    {
        public ExposedColumn (HAlign halign, boolean stretch, float weight, float minWidth) {
            super(halign, stretch, weight, minWidth);
        }
        public HAlign halign () { return _halign; }
        public float weight () { return _weight; }
        public float minWidth () { return _minWidth; }
        public boolean isStretch () { return _stretch; }
    }

    static Group slider (String label, Slider slider) {
        return new Group(AxisLayout.horizontal()).add(new Label(label),
            new SizableGroup(AxisLayout.horizontal(), 30, 0).add(new ValueLabel(slider.value)),
            slider);
    }

    static class ColumnEditor extends Group
    {
        ExposedColumn col = new ExposedColumn(HAlign.CENTER, false, 1, 0);
        Slider weight = new Slider(col.weight(), 0, 50).setIncrement(1),
                minWidth = new Slider(col.minWidth(), 0, 150).setIncrement(1);
        ToggleButton stretch = new ToggleButton("Stretch");
        Button halign = new Button(col.halign().name());

        ColumnEditor () {
            super(new FlowLayout());
            add(slider("Weight:", weight), slider("Min Width:", minWidth), stretch, halign);
            stretch.selected.update(col.isStretch());
            weight.value.connect(new Slot<Float>() {
                @Override public void onEmit (Float event) {
                    col = new ExposedColumn(col.halign(), col.isStretch(), event, col.minWidth());
                }
            });
            minWidth.value.connect(new Slot<Float>() {
                @Override public void onEmit (Float event) {
                    col = new ExposedColumn(col.halign(), col.isStretch(), col.weight(), event);
                }
            });
            stretch.selected.connect(new Slot<Boolean>() {
                @Override public void onEmit (Boolean event) {
                    col = new ExposedColumn(col.halign(), event, col.weight(), col.minWidth());
                }
            });
            halign.clicked().connect(new Slot<Button>() {
                @Override public void onEmit (Button event) {
                    HAlign[] values = HAlign.values();
                    HAlign next = values[
                        (HAlign.valueOf(halign.text.get()).ordinal() + 1) % values.length];
                    halign.text.update(next.name());
                    col = new ExposedColumn(next, col.isStretch(), col.weight(), col.minWidth());
                }
            });
        }
    }

    static class DemoCell extends Label
    {
        DemoCell (String text) {
            super(text);
        }
    }

    static class TableEditor extends Group
    {
        ColumnEditor column = new ColumnEditor();
        Group tableHolder = new Group(AxisLayout.horizontal().stretchByDefault().offStretch(),
            Style.BACKGROUND.is(Background.bordered(Colors.WHITE, Colors.BLACK, 1).inset(5)),
            Style.VALIGN.top);
        Group table;
        List<Column> columns = new ArrayList<Column>();
        Styles tableStyles = Styles.make(Style.BACKGROUND.is(Background.solid(Colors.LIGHT_GRAY)),
            Style.VALIGN.top);

        TableEditor () {
            super(AxisLayout.vertical().offStretch(), Style.VALIGN.top);
            class CellAdder extends Button {
                final int count;
                CellAdder (int count) {
                    super("+" + count);
                    this.count = count;
                }
                @Override public void onClick (Pointer.Event event) {
                    super.onClick(event);
                    addCells(count);
                }
            }
            Button add = new Button("Add") {
                @Override public void onClick (Pointer.Event event) {
                    super.onClick(event);
                    addColumn();
                }
            };
            Button reset = new Button("Reset") {
                @Override public void onClick (Pointer.Event event) {
                    super.onClick(event);
                    reset();
                }
            };
            add(column,
                new Group(AxisLayout.horizontal()).add(
                    new Label("Columns:"), add, reset, new Shim(5, 1),
                    new Label("Cells:"), new CellAdder(1), new CellAdder(2),
                    new CellAdder(5), new CellAdder(10)),
                tableHolder.setConstraint(AxisLayout.stretched()));

            reset();
        }

        void reset () {
            columns.clear();
            columns.add(column.col);
            refresh();
        }

        void refresh () {
            Group oldTable = table;
            if (table != null) tableHolder.remove(table);
            tableHolder.add(table = new Group(
                new TableLayout(columns.toArray(new Column[0])), tableStyles));
            if (oldTable!= null) {
                while (oldTable.childCount() > 0) table.add(oldTable.childAt(0));
            }
        }

        void addCells (int count) {
            while (count-- > 0) {
                table.add(new DemoCell("Sample").addStyles(Style.BACKGROUND.is(
                    Background.solid(0xFFDDDD70 + (table.childCount() % 8) * 0x10))));
            }
        }

        void addColumn () {
            columns.add(column.col);
            refresh();
        }
    }

    @Override protected String name () {
        return "TableLayout";
    }

    @Override protected String title () {
        return "UI: TableLayout";
    }

    @Override protected Group createIface () {
        return new TableEditor();
    }
}
