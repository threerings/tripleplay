//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;

import playn.core.Asserts;
import playn.core.Color;
import playn.core.GroupLayer;
import playn.core.ImmediateLayer;
import playn.core.Layer;
import playn.core.Mouse;
import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.Surface;
import playn.core.Mouse.WheelEvent;
import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.IPoint;
import pythagoras.f.Point;
import pythagoras.f.Rectangle;
import react.Signal;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.util.XYFlicker;
import tripleplay.util.Colors;
import tripleplay.util.Layers;

/**
 * A composite element that manages horizontal and vertical scrolling of a single content element.
 * As shown below, the content can be thought of as moving around behind the scroll group, which
 * is clipped to create a "view" to the content. Methods {@link #xpos()} and {@link #ypos()} allow
 * reading the current position of the view. The view position can be set with {@link #scroll()}.
 * The view size and content size are available via {@link #viewSize()} and {@link #contentSize()}.
 * <pre>{@code
 *      Scrolled view (xpos,ypos>0)       View unscrolled (xpos,ypos=0)
 *     ---------------------------        ---------------------------
 *     |                :        |        | Scroll  |               |
 *     |   content      : ypos   |        | Group   |               |
 *     |                :        |        |  "view" |               |
 *     |           -----------   |        |----------               |
 *     |           | Scroll  |   |        |                         |
 *     |---xpos--->| Group   |   |        |                         |
 *     |           |  "view" |   |        |         content         |
 *     |           -----------   |        |                         |
 *     ---------------------------        ---------------------------
 * }</pre>
 *
 * <p>Scroll bars are overlaid at the edges of the view and fade out after a short period of
 * inactivity. By default, bars are drawn as simple semi-transparent overlays. The color, size and
 * overall rendering can be overridden with styles.</p>
 *
 * <p>NOTE: the scroll group inherits from Elements as an implementation detail: add and remove
 * should never be called directly. To "add" elements, callers should set {@link #content} to a
 * {@code Group} and add things to that.</p>
 *
 * <p>NOTE: since scrolling is done by pointer events, child elements cannot be clicked directly.
 * Instead, basic click handling can be done using the {@link #contentClicked()} signal.</p>
 *
 * TODO: some way to handle keyboard events (complicated by lack of a focus element)
 * TODO: more features when Mouse.hasMouse: show some paging buttons when bars are moused over
 * TODO: optional gutter for scroll bars?
 * TODO: more support for interacting with child elements
 */
public class Scroller extends Elements<Scroller>
{
    /** The size of scroll bars. */
    public static final Style<Float> BAR_SIZE = Style.newStyle(true, 5f);

    /** The color of scroll bars. */
    public static final Style<Integer> BAR_COLOR = Style.newStyle(
        true, Color.withAlpha(Colors.BLACK, 128));

    /** The renderer for scroll bars, by default a simple {@code Surface.fillRect}. */
    public static final Style<BarRenderer> BAR_RENDERER = Style.<BarRenderer>newStyle(
        false, new BarRenderer() {
            @Override public void drawBar (Surface surface, Rectangle area) {
                surface.fillRect(area.x,  area.y,  area.width,  area.height);
            }
        });

    /** The alpha per ms lost by the scroll bars when not moving. */
    public static final float BAR_FADE_SPEED = 1.5f / 1000;

    /** The alpha set on the scroll bars whenever they are first shown. */
    public static final float BAR_TOP_ALPHA = 3;

    /**
     * Interface for customizing how content is clipped and translated.
     * @see Scroller#Scroller(Element, Interface)
     */
    public interface Clippable {
        /**
         * Sets the size of the area the content should clip to. In the default clipping, this
         * has no effect (it relies solely on the clipped group surrounding the content).
         * This will always be called prior to {@code setPosition}.
         */
        void setViewArea (float width, float height);

        /**
         * Sets the translation of the content, based on scroll bar positions. Both numbers will
         * be non-positive, up to the maximum position of the content such that its right or
         * bottom edge aligns with the width or height of the view area, respectively. For the
         * default clipping, this just sets the translation of the content's layer.
         */
        void setPosition (float x, float y);
    }

    /**
     * Draws a scroll bar onto a surface.
     */
    public interface BarRenderer {
        /**
         * Draws the bar of the given area. The fill color if the surface is set to the resolved
         * {@link #BAR_COLOR} style. {@link Surface#save()} and restore are called automatically.
         * TODO: we may want an orientation passed too
         */
        void drawBar (Surface surface, Rectangle area);
    }

    /**
     * Listens for changes to the scrolling area or offset.
     */
    public interface Listener {
        /**
         * Notifies this listener of changes to the content size or scroll size. Normally this
         * happens when either the content or scroll group is validated.
         * @param contentSize the new size of the content
         * @param scrollSize the new size of the viewable area
         */
        void viewChanged (IDimension contentSize, IDimension scrollSize);

        /**
         * Notifies this listener of changes to the content offset. Note the offset values are
         * positive numbers, so correspond to the position of the content over 0, 0 of the view
         * area.
         * @param xpos the horizontal amount by which the content is offset
         * @param ypos the vertical amount by which the content is offset
         */
        void positionChanged (float xpos, float ypos);
    }

    /**
     * Defines the directions available for scrolling.
     */
    public enum Behavior {
        HORIZONTAL, VERTICAL, BOTH;

        public boolean hasHorizontal () {
            return this == HORIZONTAL || this == BOTH;
        }

        public boolean hasVertical () {
            return this == VERTICAL || this == BOTH;
        }
    }

    /**
     * A horizontal or vertical scroll bar.
     */
    public static class Bar {
        /**
         * Returns the maximum value that this scroll bar can have, in content offset coordinates.
         */
        public float max () {
            return _max;
        }

        /**
         * Tests if the scroll bar is currently active.
         */
        public boolean active () {
            return _max != 0;
        }

        /** Set the view size and content size along this bar's axis. */
        protected float setRange (float viewSize, float contentSize) {
            _size = viewSize;
            _csize = contentSize;
            if (!_on || _size >= _csize) {
                // no need to render, clear fields
                _max = _extent = _pos = _cpos = 0;
                return 0;

            } else {
                // prepare rendering fields
                _max = _csize - _size;
                _extent = _size * _size / _csize;
                _pos = Math.min(_pos,  _size - _extent);
                _cpos = _pos / (_size - _extent) * _max;
                return _cpos;
            }
        }

        /** Sets the position of the content along this bar's axis. */
        protected boolean set (float cpos) {
            if (cpos == _cpos) return false;
            _cpos = cpos;
            _pos = _max == 0 ? 0 : cpos / _max * (_size - _extent);
            return true;
        }

        /** Gets the size of the content along this scroll bar's axis. */
        protected float getContentSize () {
            return _on ? _csize : _size;
        }

        /** During size computation, extends the provided hint. */
        protected float extendHint (float hint) {
            // we want the content to take up as much space as it wants if this bar is on
            // TODO: use Float.MAX? that may cause trouble in other layout code
            return _on ? 100000 : hint;
        }

        /** If this scroll bar is in use. Set according to {@link Scroller.Behavior}. */
        protected boolean _on = true;

        /** View size. */
        protected float _size;

        /** Content size. */
        protected float _csize;

        /** Bar offset. */
        protected float _pos;

        /** Content offset. */
        protected float _cpos;

        /** Thumb size. */
        protected float _extent;

        /** The maximum position the content can have. */
        protected float _max;
    }

    /**
     * Finds the closest ancestor of the given element that is a {@code Scroller}, or null if
     * there isn't one. This uses the tripleplay ui hierarchy.
     */
    public static Scroller findScrollParent (Element<?> elem) {
        for (; elem != null && !(elem instanceof Scroller); elem = elem.parent()) {}
        return (Scroller)elem;
    }

    /**
     * Attempts to scroll the given element into view.
     * @return true if successful
     */
    public static boolean makeVisible (final Element<?> elem) {
        Scroller scroller = findScrollParent(elem);
        if (scroller == null) return false;

        // the element in question may have been added and then immediately scrolled to, which
        // means it hasn't been layed out yet and does not have its proper position; in that case
        // defer this process a tick to allow it to be laid out
        if (!scroller.isSet(Flag.VALID)) {
            PlayN.invokeLater(new Runnable() {
                @Override public void run () {
                    makeVisible(elem);
                }
            });
            return true;
        }

        Point offset = Layers.transform(new Point(0, 0), elem.layer, scroller.content.layer);
        scroller.scroll(offset.x, offset.y);
        return true;
    }

    /** The content contained in the scroll group. */
    public final Element<?> content;

    /** Scroll bars. */
    public final Bar hbar = createBar(), vbar = createBar();

    /**
     * Creates a new scroll group containing the given content and with {@link Behavior#BOTH}.
     * <p>If the content is an instance of {@link Clippable}, then translation will occur via
     * that interface. Otherwise, the content's layer translation will be set directly.
     * Graphics level clipping is always performed.</p>
     */
    public Scroller (Element<?> content) {
        super(AxisLayout.horizontal().stretchByDefault().offStretch().gap(0));

        // out immediate child is the scroller, and that has the content
        add(_scroller.add(this.content = content));

        // use the content's clipping method if it is Clippable
        if (content instanceof Clippable) {
            _clippable = (Clippable)content;

        } else {
            // otherwise, clip using layer translation and GroupLayer.Clipped
            _clippable = new Clippable() {
                @Override public void setViewArea (float width, float height) { /* noop */ }
                @Override public void setPosition (float x, float y) {
                    Scroller.this.content.layer.setTranslation(x, y);
                }
            };
        }

        // absorb clicks so that pointer drag can always scroll
        set(Flag.HIT_ABSORB, true);

        // handle mouse wheel
        layer.addListener(new Mouse.LayerAdapter() {
            @Override public void onMouseWheelScroll (WheelEvent event) {
                // scale so each wheel notch is 1/4 the screen dimension
                float delta = event.velocity() * .25f;
                if (vbar.active()) scrollY(ypos() + (int)(delta * viewSize().height()));
                else scrollX(xpos() + (int)(delta * viewSize().width()));
            }
        });

        // handle drag scrolling
        layer.addListener(_flicker = new XYFlicker());
    }

    /**
     * Sets the behavior of this scroll bar.
     */
    public Scroller setBehavior (Behavior beh) {
        hbar._on = beh.hasHorizontal();
        vbar._on = beh.hasVertical();
        invalidate();
        return this;
    }

    /**
     * Adds a listener to be notified of this scroll group's changes.
     */
    public void addListener (Listener lner) {
        if (_lners == null) _lners = new ArrayList<Listener>();
        _lners.add(lner);
    }

    /**
     * Removes a previously added listener from this scroll group.
     */
    public void removeListener (Listener lner) {
        if (_lners != null) _lners.remove(lner);
    }

    /**
     * Returns the offset of the left edge of the view area relative to that of the content.
     */
    public float xpos () {
        return hbar._cpos;
    }

    /**
     * Returns the offset of the top edge of the view area relative to that of the content.
     */
    public float ypos () {
        return vbar._cpos;
    }

    /**
     * Sets the left edge of the view area relative to that of the content. The value is clipped
     * to be within its valid range.
     */
    public void scrollX (float x) {
        scroll(x, ypos());
    }

    /**
     * Sets the top edge of the view area relative to that of the content. The value is clipped
     * to be within its valid range.
     */
    public void scrollY (float y) {
        scroll(xpos(), y);
    }

    /**
     * Sets the left and top of the view area relative to that of the content. The values are
     * clipped to be within their respective valid ranges.
     */
    public void scroll (float x, float y) {
        x = Math.max(0, Math.min(x, hbar._max));
        y = Math.max(0, Math.min(y, vbar._max));
        _flicker.positionChanged(x, y);
    }

    /**
     * Sets the left and top of the view area relative to that of the content the next time the
     * container is laid out. This is needed if the caller invalidates the content and needs
     * to then set a scroll position which may be out of range for the old size.
     */
    public void queueScroll (float x, float y) {
        _queuedScroll = new Point(x, y);
    }

    /**
     * Gets the size of the content that we are responsible for scrolling. Scrolling is active
     * for a given axis when this is larger than {@link #scrollSize()} along that axis.
     */
    public IDimension contentSize () {
        return _contentSize;
    }

    /**
     * Gets the size of the view which renders some portion of the content.
     */
    public IDimension viewSize () {
        return _scroller.size();
    }

    /**
     * Gets the signal dispatched when a pointer click occurs in the scroll group. This happens
     * only when the drag was not far enough to cause appreciable scrolling.
     */
    public Signal<Pointer.Event> contentClicked () {
        return _flicker.clicked;
    }

    @Override public Scroller add (Element<?>... children) {
        Asserts.checkState(childCount() + children.length == 1);
        return super.add(children);
    }

    @Override public Scroller add (int index, Element<?> child) {
        Asserts.checkState(childCount() == 0);
        return super.add(index, child);
    }

    /** Prepares the scroll group for the next frame, at t = t + delta. */
    protected void update (float delta) {
        _flicker.update(delta);
        update(false);

        // fade out the bars
        if (_barAlpha > 0) setBarAlpha(_barAlpha - BAR_FADE_SPEED * delta);
    }

    /** Updates the position of the content to match the flicker. If force is set, then the
     * relevant values will be updated even if there was no change. */
    protected void update (boolean force) {
        IPoint pos = _flicker.position();
        boolean dx = hbar.set(pos.x()), dy = vbar.set(pos.y());
        if (dx || dy || force) {
            _clippable.setPosition(-pos.x(), -pos.y());

            // now check the child elements for visibility
            if (!force) updateVisibility();

            firePositionChange();
            setBarAlpha(BAR_TOP_ALPHA);
        }
    }

    /**
     * A method for creating our Bar instances. This is called once each for hbar and vbar at object
     * creation time. Overriding this method will allow subclasses to customize Bar behavior.
     */
    protected Bar createBar ()  {
        return new Bar();
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new BarsLayoutData();
    }

    @Override protected Class<?> getStyleClass () {
        return Scroller.class;
    }

    @Override protected void wasAdded () {
        super.wasAdded();
        _updater = root().iface().addTask(new Interface.Task() {
            @Override public void update (float delta) {
                Scroller.this.update(delta);
            }
        });
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        _updater.remove();
    }

    /** Hides the layers of any children of the content that are currently visible but outside
     * the clipping area. */
    // TODO: can we get the performance win without being so intrusive?
    protected void updateVisibility () {
        // non-Elements can't participate, they must implement Clippable and do something else
        if (!(content instanceof Elements)) {
            return;
        }

        // hide the layer of any child of content that isn't in bounds
        float x = hbar._cpos, y = vbar._cpos, wid = hbar._size, hei = vbar._size;
        for (Element<?> child : (Elements<?>)content) {
            IDimension size = child.size();
            if (child.isVisible()) child.layer.setVisible(
                child.x() < x + wid && child.x() + size.width() > x &&
                child.y() < y + hei && child.y() + size.height() > y);
        }
    }

    /** Dispatches a {@link Listener#viewChanged()} to listeners. */
    protected void fireViewChanged () {
        if (_lners == null) return;
        IDimension csize = contentSize(), ssize = viewSize();
        for (Listener lner : _lners) {
            lner.viewChanged(csize, ssize);
        }
    }

    /** Dispatches a {@link Listener#positionChanged()} to listeners. */
    protected void firePositionChange ()
    {
        if (_lners == null) return;
        for (Listener lner : _lners) {
            lner.positionChanged(xpos(), ypos());
        }
    }

    /** Sets the alpha of the scroll bars layer. */
    protected void setBarAlpha (float alpha) {
        _barAlpha = Math.min(BAR_TOP_ALPHA, Math.max(0, alpha));
        if (_barLayer != null) {
            _barLayer.setAlpha(Math.min(_barAlpha, 1));
            _barLayer.setVisible(_barAlpha > 0);
        }
    }

    /** Extends the usual Elements layout with scroll bar setup. */
    protected class BarsLayoutData extends ElementsLayoutData
    {
        public final int barColor = resolveStyle(BAR_COLOR);
        public final float barSize = resolveStyle(BAR_SIZE).floatValue();
        public final BarRenderer barRenderer = resolveStyle(BAR_RENDERER);

        @Override
        public void layout (float left, float top, final float width, final float height) {
            super.layout(left, top, width, height);

            // all children are now validated, update layer visibility
            updateVisibility();

            ImmediateLayer bars = null;

            if (barRenderer != null && (hbar.active() || vbar.active())) {
                // make a new layer to render the scroll bars
                bars = PlayN.graphics().createImmediateLayer(new ImmediateLayer.Renderer() {

                    final Rectangle area = new Rectangle();

                    @Override public void render (Surface surface) {
                        surface.save();
                        surface.setFillColor(barColor);

                        if (hbar.active()) {
                            area.setBounds(hbar._pos, height - barSize, hbar._extent, barSize);
                            barRenderer.drawBar(surface, area);
                        }

                        if (vbar.active()) {
                            area.setBounds(width - barSize, vbar._pos, barSize, vbar._extent);
                            barRenderer.drawBar(surface, area);
                        }

                        surface.restore();
                    }
                });
            }

            // out with the old, in with the new
            if (_barLayer != null) _barLayer.destroy();
            if ((_barLayer = bars) != null) _scroller.layer.add(
                _barLayer.setDepth(1).setAlpha(_barAlpha));
        }
    }

    /** Lays out the internal scroller group that contains the content. Performs all the jiggery
     * pokery necessary to make the content think it is in a large area and keep the outer
     * Scroller up to date. */
    protected class ScrollLayout extends Layout
    {
        @Override public Dimension computeSize (Elements<?> elems, float hintX, float hintY) {
            // the content is always the 1st child, get the preferred size with extended hints
            Asserts.checkArgument(elems.childCount() == 1 && elems.childAt(0) == content);
            _contentSize.setSize(preferredSize(elems.childAt(0),
                hbar.extendHint(hintX), vbar.extendHint(hintY)));
            return new Dimension(_contentSize);
        }

        @Override public void layout (Elements<?> elems, float left, float top, float width,
                                      float height) {
            Asserts.checkArgument(elems.childCount() == 1 && elems.childAt(0) == content);
            // reset range of scroll bars
            left = hbar.setRange(width, _contentSize.width);
            top = vbar.setRange(height, _contentSize.height);

            // set the content bounds to the large virtual area starting at 0, 0
            setBounds(content, 0, 0, hbar.getContentSize(), vbar.getContentSize());

            // clip the content in its own special way
            _clippable.setViewArea(width, height);

            // clip the scroller layer too, can't hurt
            ((GroupLayer.Clipped)_scroller.layer).setSize(width, height);

            // reset the flicker (it retains its current position)
            _flicker.reset(hbar.max(), vbar.max());

            // scroll the content
            if (_queuedScroll != null) {
                scroll(_queuedScroll.x, _queuedScroll.y);
                _queuedScroll = null;
            } else {
                scroll(left, top);
            }

            // force an update so the scroll bars have properly aligned positions
            update(true);

            // notify listeners of a view change
            fireViewChanged();
        }
    }

    protected final Group _scroller = new Group(new ScrollLayout()) {
        @Override protected GroupLayer createLayer () {
            // use 1, 1 so we don't crash. the real size is set on validation
            return PlayN.graphics().createGroupLayer(1, 1);
        }
    };

    protected final XYFlicker _flicker;
    protected final Clippable _clippable;
    protected final Dimension _contentSize = new Dimension();
    protected Interface.TaskHandle _updater;
    protected Layer _barLayer;
    protected Point _queuedScroll;
    protected float _barAlpha;
    protected BarRenderer _barRenderer;
    protected java.util.List<Listener> _lners;
}
