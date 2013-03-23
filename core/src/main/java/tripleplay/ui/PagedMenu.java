package tripleplay.ui;

import react.Slot;
import react.UnitSlot;
import react.Value;
import react.ValueView;

/**
 * A menu that is also capable of showing one page of its items at a time. Note that the caller
 * must connect buttons or sliders or some other UI elements within the menu to perform paging.
 * <p>Note that this implementation assumes items are added in order of their page. Removal and
 * of items and addition of items to the end of the last page is naturally supported.</p>
 * TODO: support insertion of items in the middle of a page
 */
public class PagedMenu extends Menu
{
    /** Number of items on a page is constant. */
    public final int itemsPerPage;

    /**
     * Creates a new paged menu with the given layout and number of items per page.
     */
    public PagedMenu (Layout layout, int itemsPerPage) {
        super(layout);
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * Gets a view of the current page value.
     */
    public ValueView<Integer> page () {
        return _page;
    }

    /**
     * Gets a view of the number of pages value.
     */
    public ValueView<Integer> numPages () {
        return _numPages;
    }

    /**
     * Gets a slot that will update the page when emitted.
     */
    public Slot<Integer> pageSlot () {
        return new Slot<Integer>() {
            @Override public void onEmit (Integer page) {
                setPage(page);
            }
        };
    }

    /**
     * Gets a slot that will increment the page by the given delta when emitted.
     */
    public UnitSlot incrementPage (final int delta) {
        return new UnitSlot() {
            @Override public void onEmit () {
                setPage(_page.get() + delta);
            }
        };
    }

    /**
     * Gets the current page.
     */
    public int getPage () {
        return _page.get().intValue();
    }

    /**
     * Sets the current page. Items on the page are shown. All others are hidden.
     */
    public PagedMenu setPage (int page) {
        int oldPage = _page.get();
        if (page != oldPage) {
            _page.update(page);
            updateVisibility(oldPage, oldPage);
            updateVisibility(page, page);
        }
        return this;
    }

    protected int pageOfItem (int itemIdx) {
        return itemIdx / itemsPerPage;
    }

    protected void updateNumPages () {
        int numItems = _items.size();
        _numPages.update(numItems == 0 ? 0 : (numItems - 1) / itemsPerPage + 1);
    }

    protected void updateVisibility (int fromPage, int toPage) {
        int itemIdx = fromPage * itemsPerPage, size = _items.size();
        for (int pp = fromPage; pp <= toPage; ++pp) {
            boolean vis = pp == _page.get();
            for (int ii = 0; ii < itemsPerPage; ii++) {
                if (itemIdx >= size) break;
                _items.get(itemIdx++).setVisible(vis);
            }
        }
    }

    @Override protected void connectItem (MenuItem item) {
        int itemIdx = _items.size();
        super.connectItem(item);
        updateNumPages();
        int page = pageOfItem(itemIdx);
        if (page != _page.get()) item.setVisible(false);
        if (page <= _page.get()) updateVisibility(_page.get(), _page.get() + 1);
    }

    @Override protected void didDisconnectItem (MenuItem item, int itemIdx) {
        updateNumPages();
        if (_page.get() == _numPages.get()) incrementPage(-1);
        else {
            int page = pageOfItem(itemIdx);
            if (page < _page.get()) updateVisibility(_page.get() - 1, _page.get());
            else if (page == _page.get()) updateVisibility(_page.get(), _page.get());
        }
    }

    protected Value<Integer> _page = Value.create(0);
    protected Value<Integer> _numPages = Value.create(0);
}
