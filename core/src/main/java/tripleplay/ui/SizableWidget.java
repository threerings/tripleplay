package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

/**
 * A widget that allows configuring its preferred size. The size is always returned when the size
 * of the widget is calculated, but the widget may end up being stretched when contained in a
 * layout that does so.
 * TODO: is there some way to roll this into Element? It seems like any element could potentially
 * have its size configured, depending on the application
 * TODO: a better name
 */
public class SizableWidget<T extends Widget<T>> extends Widget<T>
{
    /** Creates the sizable widget with preferred width and height of 0. */
    public SizableWidget () {
        this(0, 0);
    }

    /** Creates the sizable widget with the given preferred size. */
    public SizableWidget (IDimension size) {
        this(size.width(), size.height());
    }

    /** Creates the sizable widget with preferred width and height. */
    public SizableWidget (float width, float height) {
        _preferredSize = new Dimension(width, height);
    }

    /** Sets the preferred size of this widget to the given width and height. Returns the widget
     * for chaining. */
    public T setPreferredSize (float width, float height) {
        _preferredSize.setSize(width, height);
        invalidate();
        return asT();
    }

    /** Sets the preferred width of this widget. The preferred height is not changed. */
    public T setPreferredWidth (float width) {
        _preferredSize.width = width;
        invalidate();
        return asT();
    }

    /** Sets the preferred height of this widget. The preferred width is not changed. */
    public T setPreferredHeight (float height) {
        _preferredSize.height = height;
        invalidate();
        return asT();
    }

    /** Sets the preferred size of this widget to the given dimension. Returns the widget for
     * chaining. */
    public T setPreferredSize (IDimension size) {
        _preferredSize.setSize(size);
        invalidate();
        return asT();
    }

    @Override protected SizableLayoutData createLayoutData (float hintX, float hintY) {
        return new SizableLayoutData();
    }

    protected class SizableLayoutData extends LayoutData {
        @Override public Dimension computeSize (float hintX, float hintY) {
            return new Dimension(_preferredSize);
        }
    }

    protected final Dimension _preferredSize;
}
