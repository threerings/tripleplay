//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.Dimension;
import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;
import react.Closeable;
import react.UnitSlot;

import tripleplay.ui.Composite;
import tripleplay.ui.Container;
import tripleplay.ui.Element;
import tripleplay.ui.Group;
import tripleplay.ui.Layout;
import tripleplay.ui.Scroller;
import tripleplay.ui.layout.AxisLayout;
import static tripleplay.ui.Log.log;

/**
 * A scrolling vertical display, optimized for showing potentially very long lists such as a chat
 * log. Supports:
 *
 * <ul>
 * <li>addition of new elements on the end</li>
 * <li>pruning old elements from the beginning</li>
 * <li>progressive rendering of newly visible items in the list</li>
 * <li>automatically keeping the last item visible</li>
 * <li>purging of old rendered elements that are no longer visible
 * </ul>
 *
 * Items are stored in a backing array. Each entry in the array may or may not have a corresponding
 * Element visible (presuming that the rendering and storage of elements is expensive). When the
 * user scrolls, entries are rendered on demand using {@link #render}. Entries that are not visible
 * use an estimated size for layout purposes. This of course may produce some artifacts during
 * scrolling, which is the penalty of not computing the exact size.
 *
 * <p>NOTE: The elements in the UI (type {@code W}) must not be mutated after rendering and must
 * have a constant size given a particular item and width. See {@link #render}.
 *
 * @param <T> The type of item backing this history
 * @param <W> The type of element or widget stored in this history
 */
public abstract class HistoryGroup<T, W extends Element<?>> extends Composite<HistoryGroup<T, W>>
{
    /** A label that exposes the width hint and preferred size. */
    public static class RenderableLabel extends Label
    {
        /** Creates a new label. */
        public RenderableLabel (String text) {
            super(text);
        }

        /** Calculates the size of the label, using the given width hint. */
        public Dimension calcSize (float hintX) {
            return new Dimension(preferredSize(hintX, 0));
        }
    }

    /** History group of just labels. This makes some lightweight use cases really easy. */
    public static class Labels extends HistoryGroup<String, RenderableLabel>
    {
        @Override protected void render (final Entry entry) {
            entry.element = new RenderableLabel(entry.item);
        }

        @Override protected void calcSize (Entry entry) {
            entry.size = entry.element.calcSize(_entriesWidth);
        }
    }

    /** Tests if the history is currently at the bottom. If the history is at the bottom, then
     * subsequent additions will cause automatic scrolling. By default, new groups are at the
     * bottom. Subsequent upward scrolling will clear this and scrolling to the bottom will
     * set it again. */
    public boolean atBottom () {
        return _atBottom;
    }

    /** Issues a request to scroll to the bottom of the list of history elements. */
    public void scrollToBottom () {
        _scroller.queueScroll(0, Float.MAX_VALUE);
    }

    /** Prunes the given number of entries from the beginning of the history. */
    public void pruneOld (int adjustment) {
        if (adjustment != 0) {
            _entriesGroup.removeOldEntries(_baseIndex + adjustment);
            _entries.subList(0, Math.min(adjustment, _entries.size())).clear();
            _baseIndex += adjustment;
        }
    }

    /** Adds a new item to the end of the history. If the history is at the bottom, the item
     * will be rendered immediately, otherwise the message group is invalidated so that the
     * scroll bounds will be updated. */
    public void addItem (T item) {
        // always add a new entry
        Entry entry = addEntry(item);

        // if we're not currently displayed, do nothing else
        if (!_added) {
            return;
        }

        // render immediately if at the bottom
        if (atBottom()) {
            _entriesGroup.addEntry(entry);
        } else {
            _entriesGroup.invalidate();
        }

        // pick up the changes, if any (probably not)
        schedule();

        // keep up with the scrolling
        maybeScrollToBottom();
    }

    /** Sets the vertical gap between history elements. By default, the gap is set to 1. */
    public void setVerticalGap (int vgap) {
        _vgap = vgap;
        _entriesGroup.invalidate();
    }

    protected void update () {
        if (!_added) {
            log.warning("Whassup, scheduled while removed?");
            cancel();
            return;
        }

        if (_widthUpdated) {
            // a bit cumbersome, but rare... remove all previously created labels
            _entriesGroup.removeAllEntries();
            _widthUpdated = false;
        }

        // maybe wait until next frame to get valid
        if (!isSet(Flag.VALID)) return;

        if (_entries.size() == 0) {
            // no entries, we're done here
            cancel();
            return;
        }

        // walk up from the bottom and render the first null one
        int bottom = findEntry(_scroller.ypos() + _viewHeight);
        int top = bottom;
        for (; top >= 0; top--) {
            Entry e = _entries.get(top);
            if (e.ypos + e.size.height() < _scroller.ypos()) {
                break;
            }
            if (e.element == null) {
                // render this one and do more next update
                // TODO: use a maximum frame time
                _entriesGroup.addEntry(e);
                return;
            }
        }

        // all entries in view are rendered, now delete ones that are far away
        float miny = _scroller.ypos() - _viewHeight;
        float maxy = _scroller.ypos() + _viewHeight * 2;

        // walk up one more view height
        for (; top >= 0; top--) {
            Entry e = _entries.get(top);
            if (e.bottom() < miny) {
                break;
            }
        }

        // walk down one more view height
        for (int size = _entries.size(); bottom < size; bottom++) {
            Entry e = _entries.get(bottom);
            if (e.ypos >= maxy) {
                break;
            }
        }

        _entriesGroup.removeEntriesNotInRange(_baseIndex + top, _baseIndex + bottom);
        cancel();
    }

    /** Creates a new history group. */
    protected HistoryGroup () {
        setLayout(AxisLayout.horizontal().stretchByDefault().offStretch());
        initChildren(_scroller = new Scroller(_entriesGroup = new EntriesGroup()).setBehavior(Scroller.Behavior.VERTICAL));
    }

    /** Sets up the {@link Entry#element} member. After this call, the element will be added to
     * the group so that style information is available.
     * <p>Note that the {@code Element.size()} value is ignored and only the entry size is
     * considered during layout, as determined in {@link #calcSize(Entry)}. */
    protected abstract void render (Entry entry);

    /** Calculates and sets the {@link Entry#size} member, according to the current
     * {@link #_entriesWidth}. Normally this must be done using a Widget that exposes its
     * {@code preferredSize} method and allows a wrap width to be set.
     * <p>This method is called after the {@link Entry#element} member is added to the group so
     * that style information can be determined.</p> */
    protected abstract void calcSize (Entry entry);

    /** Called during layout after a change to {@link #_entriesWidth} occurs. Subclasses may want
     * to update some internal layout state that relies on the width. */
    protected void didUpdateWidth (float oldWidth) {}

    /** Sets the estimated height for entries that are currently not in view. */
    protected void setEstimatedHeight (float height) {
        _estimatedSize = new Dimension(1, height);
        _entriesGroup.invalidate();
    }

    /** Scroll to the bottom if they were already at the bottom. */
    protected void maybeScrollToBottom () {
        if (atBottom()) {
            scrollToBottom();
        }
    }

    /** Convenience method to clear out all currently rendered messages and do them again. */
    protected void resetEntries () {
        _entriesGroup.removeAllEntries();
        schedule();
    }

    /** Adds a new history entry without the UI check or the scrolling to bottom. Useful for batch
     * additions from the subclass/game model of the backing storage. */
    protected Entry addEntry (T item) {
        Entry entry = new Entry(item, _baseIndex + _entries.size());
        _entries.add(entry);
        return entry;
    }

    @Override protected void wasAdded () {
        super.wasAdded();
        _added = true;

        // update the elements for visible entries later
        schedule();
    }

    @Override protected void wasRemoved () {
        _added = false;

        // free up the all currently rendered elements for garbage collection
        _entriesGroup.removeAllEntries();

        // kill off task
        cancel();

        super.wasRemoved();
    }

    @Override protected Class<?> getStyleClass () {
        return HistoryGroup.class;
    }

    protected void schedule () {
        if (_conn == Closeable.Util.NOOP && _added) {
            _conn = root().iface.frame.connect(new UnitSlot() {
                public void onEmit () { update(); }
            });
        }
    }

    protected void cancel () {
        _conn = Closeable.Util.close(_conn);
    }

    /** Find the index of the entry at the given y position. */
    protected int findEntry (float ypos) {
        int max = _entries.size() - 1, low = 0, high = max;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            float midpos = _entries.get(mid).ypos;
            if (ypos > midpos) {
                low = mid + 1;
            } else if (ypos < midpos) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return Math.min(low, max);
    }

    /**
     * An item in the history and associated layout info.
     */
    protected class Entry
    {
        /** The item. */
        public final T item;

        /** The unique index for this entry (increases by one per new entry). */
        public final int index;

        /** The last rendered size of the entry, or the estimated size if not in view. */
        public Dimension size = _estimatedSize;

        /** The y position of the top of this entry. */
        public float ypos;

        /** The rendered element for this entry, if it is currently in view. */
        public W element;

        /**
         * Creates a new entry.
         */
        public Entry (T message, int index) {
            this.item = message;
            this.index = index;
        }

        /** Do the full on render of everything, if needed. */
        public W render () {
            if (element != null) {
                return element;
            }

            HistoryGroup.this.render(this);
            return element;
        }

        public float bottom () {
            return ypos + size.height();
        }
    }

    /** Groups the rendered items in the history. */
    protected class EntriesGroup extends Group
        implements Scroller.Clippable
    {
        public EntriesGroup () {
            super(new EntriesLayout());
        }

        @Override public void setViewArea (float width, float height) {
            _viewHeight = height;
            maybeScrollToBottom();
        }

        @Override public void setPosition (float x, float y) {
            IRectangle bounds = bounds(new Rectangle());
            if (_viewHeight > bounds.height()) {
                // nail the group to the bottom of the scroll area.
                layer.setTranslation(x, _viewHeight - bounds.height());
                _atBottom = true;
            } else {
                layer.setTranslation(x, (float)Math.floor(y));
                _atBottom = -y == bounds.height() - _viewHeight;
            }
            schedule();
        }

        public void addEntry (Entry e) {
            // find the place to insert the entry
            int position = _renderedEntries.size() - 1;
            for (; position >= 0; position--) {
                Entry test = _renderedEntries.get(position);
                if (e.index > test.index) {
                    break;
                }
            }

            // add the rendered item to the ui
            position++;
            add(position, e.render());
            calcSize(e);

            // keep track of what we've rendered
            _renderedEntries.add(position, e);
        }

        public void removeEntry (Entry e) {
            int index = _renderedEntries.indexOf(e);
            if (index == -1) throw new IllegalArgumentException(
                "Removing entry that isn't in the list: " + e);
            removeUI(index);
        }

        public void removeOldEntries (int minIndex) {
            removeEntriesNotInRange(minIndex, Integer.MAX_VALUE);
        }

        public void removeEntriesNotInRange (int minIndex, int maxIndex) {
            for (int ii = 0; ii < _renderedEntries.size();) {
                int index = _renderedEntries.get(ii).index;
                if (index < minIndex || index > maxIndex) {
                    removeUI(ii);
                } else {
                    ii++;
                }
            }
        }

        public void removeAllEntries () {
            while (!_renderedEntries.isEmpty()) {
                removeUI(_renderedEntries.size() - 1);
            }
        }

        protected void removeUI (int index) {
            if (childAt(index) != _renderedEntries.get(index).element)
                throw new IllegalArgumentException("Mismatched entry and element");
            destroyAt(index);
            _renderedEntries.get(index).element = null;
            _renderedEntries.remove(index);
        }

        /** List of entries in exact correspondence with _children. */
        protected List<Entry> _renderedEntries = new ArrayList<Entry>();
    }

    /**
     * Lays out the history items.
     */
    protected class EntriesLayout extends Layout
    {
        @Override
        public Dimension computeSize (Container<?> elems, float hintX, float hintY) {
            // report a large width since we expect to always be stretched, not fixed
            Dimension size = new Dimension(4096, 0);
            if (!_entries.isEmpty()) {
                size.height += _vgap * (_entries.size() - 1);
                for (Entry e : _entries) {
                    size.height += e.size.height();
                }
            }
            return size;
        }

        @Override
        public void layout (Container<?> elems, float left, float top, float width, float height) {
            // deal with width updates
            if (_entriesWidth != width) {
                // update our width
                float old = _entriesWidth;
                _entriesWidth = width;

                didUpdateWidth(old);

                // schedule the refresh
                _widthUpdated = true;
                schedule();
            }

            // update all entries so they have a sensible ypos when needed
            for (Entry e : _entries) {
                float eheight = e.size.height();
                if (e.element != null) {
                    setBounds(e.element, left, top, e.size.width(), eheight);
                }
                e.ypos = top;
                top += eheight + _vgap;
            }
        }
    }

    /** The scrollable area, our only proper child. */
    protected Scroller _scroller;

    /** The rendered items contained in the scrollable area. */
    protected EntriesGroup _entriesGroup;

    /** A frame tick registration, or NOOP if we're not updating. */
    protected Closeable _conn = Closeable.Util.NOOP;

    /** The list of history entries. */
    protected List<Entry> _entries = new ArrayList<Entry>();

    /** The current width of the rendered items group, or 0 prior to layout. */
    protected float _entriesWidth;

    /** The vertical gap between history items. */
    protected int _vgap = 1;

    /** The current height of the view area (this can be different from _scroller.size() if it
     * is ever given an inset background. */
    protected float _viewHeight;

    /** Set if we discover a change to the width during layout that needs to update UI on the
     * next update. */
    protected boolean _widthUpdated;

    /** Set if we should automatically scroll to show newly added items. */
    protected boolean _atBottom = true;

    /** Tracks isAdded(), for faster testing. */
    protected boolean _added;

    /** The unique index of the 0th entry in the history. */
    protected int _baseIndex;

    /** The size to use for new, unrendered, history entries. */
    protected Dimension _estimatedSize = new Dimension(1, 18f);
}
