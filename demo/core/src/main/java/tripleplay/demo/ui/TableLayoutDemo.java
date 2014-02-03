//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.ui;

import java.util.ArrayList;
import java.util.List;

import react.Slot;
import react.UnitSlot;

import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.FlowLayout;
import tripleplay.ui.layout.TableLayout;
import tripleplay.util.Colors;

import tripleplay.demo.DemoScreen;

public class TableLayoutDemo extends DemoScreen
{
    static class ExposedColumn extends TableLayout.Column
    {
        public ExposedColumn (Style.HAlign halign, boolean stretch, float weight, float minWidth) {
            super(halign, stretch, weight, minWidth);
        }
        public Style.HAlign halign () { return _halign; }
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
        ExposedColumn col = new ExposedColumn(Style.HAlign.CENTER, false, 1, 0);
        Slider weight = new Slider(col.weight(), 0, 50).setIncrement(1),
                minWidth = new Slider(col.minWidth(), 0, 150).setIncrement(1);
        ToggleButton stretch = new ToggleButton("Stretch");
        Button halign = new Button(col.halign().name());

        ColumnEditor () {
            super(new FlowLayout());
            add(slider("Weight:", weight), slider("Min Width:", minWidth), stretch, halign);
            stretch.selected().update(col.isStretch());
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
            stretch.selected().connect(new Slot<Boolean>() {
                @Override public void onEmit (Boolean event) {
                    col = new ExposedColumn(col.halign(), event, col.weight(), col.minWidth());
                }
            });
            halign.clicked().connect(new Slot<Button>() {
                @Override public void onEmit (Button event) {
                    Style.HAlign[] values = Style.HAlign.values();
                    Style.HAlign next = values[
                        (Style.HAlign.valueOf(halign.text.get()).ordinal() + 1) % values.length];
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
        List<TableLayout.Column> columns = new ArrayList<TableLayout.Column>();
        Styles tableStyles = Styles.make(Style.BACKGROUND.is(Background.solid(Colors.LIGHT_GRAY)),
            Style.VALIGN.top);

        TableEditor () {
            super(AxisLayout.vertical().offStretch(), Style.VALIGN.top);
            class CellAdder extends Button {
                final int count;
                CellAdder (int count) {
                    super("+" + count);
                    this.count = count;
                    onClick(new UnitSlot() {
                        @Override public void onEmit () { addCells(CellAdder.this.count); }
                    });
                }
            }
            Button add = new Button("Add").onClick(new UnitSlot() {
                @Override public void onEmit () { addColumn(); }
            });
            Button reset = new Button("Reset").onClick(new UnitSlot() {
                @Override public void onEmit () { reset(); }
            });
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
                new TableLayout(columns.toArray(new TableLayout.Column[0])), tableStyles));
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
