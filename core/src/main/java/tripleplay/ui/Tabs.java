//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.List;

import playn.core.Asserts;
import playn.core.Image;

import react.Slot;
import react.Value;
import react.ValueView;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.util.Supplier;

/**
 * A composite {@code Elements} that implements tabbing. Has a horizontal row of buttons along
 * the top and a stretching content group underneath. The buttons are instances of {@link
 * ToggleButton}. Each button is associated with a content element. When the button is clicked,
 * its content is shown alone in the content group.
 *
 * <p>This diagram shows a {@code Tabs} with A and B tabs. When A's button is clicked, A's content
 * is shown. B content is not generated or not visible.
 * <code><pre>
 *   --------------------------------------
 *   |  -----  -----                      |
 *   |  |*A*|  | B |                      |  <--- the buttons group, A selected
 *   |  -----  -----                      |
 *   |------------------------------------|
 *   |                                    |  <--- the contentArea group
 *   |  --------------------------------  |
 *   |  |         A content            |  |
 *   |  --------------------------------  |
 *   |                                    |
 *   --------------------------------------
 * </code></pre></p>
 *
 * <p>The tab content associated with a button is supplied on demand via a {@link Supplier}
 * instance. The contract of {@code Supplier} is obeyed in that {@link Supplier#destroy()} is
 * called whenever the associated tab goes out of scope.</p>
 *
 * <p>NOTE: The inheritance from Elements is an implementation detail, callers should not add
 * and remove child elements directly.</p>
 *
 * TODO: we may need to do something about ToggleButton behavior here, it allows the tabs to be
 * deselected, which is not what you'd usually expect
 *
 * TODO: do we care about scrolling buttons?
 */
public class Tabs extends Elements<Tabs>
{
    /**
     * Defines the highlighting of a tab. A tab button may be highlighted if the application
     * wants to draw attention to it while it is unselected. When a button is selected, it
     * will be unhighlighted automatically. If the highlighter uses an external resource such
     * as a task or animation, it must ensure that the lifetime of the resource is tied to that
     * of the tab's button (in the hierarchy), or its layer.
     */
    public interface Highlighter {
        /**
         * Sets the highlight state of the given tab.
         */
        void highlight (Tab tab, boolean highlight);
    }

    /**
     * Represents a tab: button and content.
     */
    public class Tab
    {
        /** The button, which will show this tab's content when clicked. */
        public final ToggleButton button;

        /**
         * Creates a new tab with the supplied fields.
         */
        public Tab (ToggleButton button, Supplier generator) {
            this.button = button;
            _generator = generator;
        }

        /**
         * Selects this tab. This is just a shortcut for <code>{@link Tabs#selected}.update(this)
         * </code>.
         */
        public void select () {
            selected.update(this);
        }

        /**
         * Gets this tab's content, creating it if necessary.
         */
        public Element<?> content () {
            if (_content == null) _content = _generator.get();
            return _content;
        }

        public int index () {
            return _index;
        }

        public void setVisible (boolean visible) {
            if (!visible && selected.get() == this) selected.update(null);
            button.setVisible(visible);
        }

        public Tabs parent () {
            return Tabs.this;
        }

        /** The index of this tab in the parent {@link Tabs} instance. */
        protected int _index = -1;

        /** The supplier of this tab's content element. */
        protected final Supplier _generator;

        /** The content of this tab, if it has been shown before. */
        protected Element<?> _content;
    }

    /** Style for highlighting a tab. The default value is a no-op highlighter. */
    public static Style<Highlighter> HIGHLIGHTER = Style.<Highlighter>newStyle(true,
        new Highlighter() {
            @Override public void highlight (Tab tab, boolean highlight) {}
        });

    /** The row of buttons, one per tab. */
    public final Group buttons = new Group(AxisLayout.horizontal().gap(3));

    /** The content group. */
    public final Group contentArea = new Group(
        AxisLayout.horizontal().stretchByDefault().offStretch());

    /** The value containing the currently selected tab. */
    public final Value<Tab> selected = Value.create(null);

    /**
     * Creates a highlighter that will simply change the button's text color.
     * @param originalColor the button text color when unhighlighted
     * @param highlightColor the button text color when highlighted
     */
    public static Highlighter textColorHighlighter (
            final int originalColor, final int highlightColor) {
        return new Highlighter() {
            @Override public void highlight (Tab tab, boolean highlight) {
                if (tab.button.isSelected() && highlight) return;
                tab.button.addStyles(Style.COLOR.is(highlight ? highlightColor : originalColor));
            }
        };
    }

    /**
     * Creates a new tabbed container.
     */
    public Tabs () {
        super(AxisLayout.vertical().gap(0).offStretch());

        add(buttons, contentArea.setConstraint(AxisLayout.stretched()));

        final Selector tabButtonSelector = new Selector(buttons, null);
        tabButtonSelector.selected.connect(new Slot<Element<?>> () {
            @Override public void onEmit (Element<?> button) {
                selected.update(forWidget(button));
            }
        });

        selected.connect(new ValueView.Listener<Tab>() {
            @Override public void onChange (Tab selected, Tab deselected) {
                // hide the deselected content
                if (deselected != null) deselected.content().setVisible(false);

                // show the new content, creating if necessary
                if (selected != null) {
                    // own it baby
                    if (selected.content().parent() != contentArea)
                        contentArea.add(selected.content());
                    // make sure its visible
                    selected.content().setVisible(true);
                    // unhighlight
                    highlighter().highlight(selected, false);
                }
                // now update the button (will noop if we're called from above slot)
                tabButtonSelector.selected.update(selected != null ? selected.button : null);
            }
        });
    }

    /**
     * Gets the number of tabs.
     */
    public int tabCount () {
        return _tabs.size();
    }

    /**
     * Gets the tab at the given index, or null if the index is out of range.
     */
    public Tab tabAt (int index) {
        return index >= 0 && index <= _tabs.size() ? _tabs.get(index) : null;
    }

    /**
     * Adds a new tab to the container with the given label and supplier. Adds a new button to
     * the {@link #buttons} group. The supplier is used to generate an element to put in the
     * {@link #contentArea} group if and when the tab is selected.
     * @return the newly added tab
     */
    public Tab add (String label, Supplier supplier) {
        return add(label, null, supplier);
    }

    /**
     * Adds a new tab to the container with a pre-constructed element for its content.
     * This is a shortcut for calling {@link Tabs#add(String, Supplier)} with a {@link
     * Supplier#auto()}.
     * @return the newly added tab
     */
    public Tab add (String label, Element<?> panel) {
        return add(label, Supplier.auto(panel));
    }

    /**
     * Adds a new tab to the container with the given label, icon and supplier. Adds a new button
     * to the {@link #buttons} group. The supplier is used to generate an element to put in the
     * {@link #contentArea} group when the tab is selected.
     * @return the newly added tab
     */
    public Tab add (String label, Image icon, Supplier supplier) {
        Tab tab = new Tab(new ToggleButton(label, icon), supplier);
        tab._index = _tabs.size();
        _tabs.add(tab);
        buttons.add(tab.button);
        return tab;
    }

    /**
     * Adds a new tab to the container with a pre-constructed element for its content.
     * See {@link Tabs#add(String, Supplier)}.
     * @return the newly added tab
     */
    public Tab add (String label, Image icon, Element<?> panel) {
        return add(label, icon, Supplier.auto(panel));
    }

    /**
     * Moves the given tab into the given position.
     */
    public void repositionTab (Tab tab, int position) {
        int prev = tab.index();
        Asserts.checkArgument(prev != -1);
        Asserts.checkArgument(position >= 0 && position < _tabs.size());
        if (prev == position) return;
        _tabs.remove(prev);
        buttons.remove(tab.button);
        _tabs.add(position, tab);
        buttons.add(position, tab.button);
        resetIndices();
    }

    /**
     * Removes the given tab and destroys its resources.
     */
    public void destroyTab (Tab tab) {
        Asserts.checkArgument(_tabs.contains(tab), "Tab isn't ours");
        if (tab == selected.get()) selected.update(null);
        _tabs.remove(tab.index());
        buttons.destroy(tab.button);
        if (tab._content != null) contentArea.destroy(tab._content);
        tab._generator.destroy();
        tab._index = -1;
        resetIndices();
        return;
    }

    /**
     * Gets our highlighter. Resolved from the {@link #HIGHLIGHTER} style.
     */
    public Highlighter highlighter () {
        if (_highlighter == null) _highlighter = resolveStyle(HIGHLIGHTER);
        return _highlighter;
    }

    @Override protected void clearLayoutData () {
        super.clearLayoutData();
        _highlighter = null;
    }

    @Override protected Class<?> getStyleClass () {
        return Tabs.class;
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        // let go of suppliers
        for (Tab tab : _tabs) {
            tab._generator.destroy();
        }
    }

    /** Sets the {@link Tab#_index} field of our tabs, after a change to ordering. */
    protected void resetIndices () {
        for (int ii = 0; ii < _tabs.size(); ++ii) {
            _tabs.get(ii)._index = ii;
        }
    }

    /** Looks up a tab with the given button. */
    protected Tab forWidget (Element<?> widget) {
        for (Tab tab : _tabs) {
            if (tab.button == widget) {
                return tab;
            }
        }
        return null;
    }

    protected List<Tab> _tabs = new ArrayList<Tab>();
    protected Highlighter _highlighter;
}
