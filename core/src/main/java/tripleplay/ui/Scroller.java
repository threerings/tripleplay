//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;
import pythagoras.f.IPoint;
import pythagoras.f.Point;

import playn.core.Color;
import playn.core.Events;
import playn.core.GroupLayer;
import playn.core.ImmediateLayer;
import playn.core.Layer;
import playn.core.Mouse;
import playn.core.PlayN;
import playn.core.Pointer;
import playn.core.Surface;
import playn.core.Mouse.WheelEvent;

import react.Signal;

import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.util.XYFlicker;
import tripleplay.util.Colors;
import tripleplay.util.Layers;

/**
 * A composite element that manages horizontal and vertical scrolling of a single content element.
 * As shown below, the content can be thought of as moving around behind the scroll group, which is
 * clipped to create a "view" to the content. Methods {@link #xpos} and {@link #ypos} allow reading
 * the current position of the view. The view position can be set with {@link #scroll}. The view
 * size and content size are available via {@link #viewSize} and {@link #contentSize}.
 *
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
 * <p>Scroll bars are configurable via the {@link #BAR_TYPE} style.</p>
 *
 * <p>NOTE: {@code Scroller} is a composite container, so callers can't add to or remove from it.
 * To "add" elements, callers should set {@link #content} to a {@code Group} and add things to it
 * instead.</p>
 *
 * <p>NOTE: scrolling is done by pointer events; there are two ways to provide interactive
 * (clickable) content.
 * <ul><li>The first way is to call {@link PlayN#setPropagateEvents(boolean)} with {@code true}.
 * This has global implications but allows any descendants within the content to be clicked
 * normally. Also, with this approach, after the pointer has been dragged more than a minimum
 * distance, the {@code Scroller} calls {@link Events.Input#capture()}, which will cancel all other
 * pointer interactions, including clickable descendants. For buttons or toggles, this causes the
 * element to be deselected, corresponding to popular mobile OS conventions.</li>
 * <li>The second way is to use the {@link #contentClicked} signal. This is more light weight but
 * only emits after the pointer is released less than a minimum distance away from its starting
 * position.</li></ul></p>
 *
 * TODO: some way to handle keyboard events (complicated by lack of a focus element)
 * TODO: more fine-grained setPropagateEvents (add a flag to playn Layer?)
 * TODO: temporarily allow drags past the min/max scroll positions and bounce back
 */
public class Scroller extends Composite<Scroller>
{
    /** The type of bars to use. By default, uses an instance of {@link TouchBars}. */
    public static final Style<BarType> BAR_TYPE = Style.<BarType>newStyle(true, new BarType() {
        @Override public Bars createBars (Scroller scroller) {
            return new TouchBars(scroller, Color.withAlpha(Colors.BLACK, 128), 5f, 3f, 1.5f / 1000);
        }
    });

    /**
     * Interface for customizing how content is clipped and translated.
     * @see Scroller#Scroller
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
     * Handles creating the scroll bars.
     */
    public static abstract class BarType {
        /**
         * Creates the scroll bars.
         */
        public abstract Bars createBars (Scroller scroller);
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
         * positive numbers, so correspond to the position of the view area over the content.
         * @param xpos the horizontal amount by which the view is offset
         * @param ypos the vertical amount by which the view is offset
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
     * A range along an axis for representing scroll bars. Using the content and view extent,
     * calculates the relative sizes.
     */
    public static class Range {
        /**
         * Returns the maximum value that this range can have, in content offset coordinates.
         */
        public float max () {
            return _max;
        }

        /**
         * Tests if the range is currently active. A range is inactive if it's turned off
         * explicitly or if the view size is larger than the content size.
         */
        public boolean active () {
            return _max != 0;
        }

        /** Gets the size of the content along this range's axis. */
        public float contentSize () {
            return _on ? _csize : _size;
        }

        /** Gets the size of the view along this scroll bar's axis. */
        public float viewSize () {
            return _size;
        }

        /** Gets the current content offset. */
        public float contentPos () {
            return _cpos;
        }

        protected void setOn (boolean on) {
            _on = on;
        }

        protected boolean on () {
            return _on;
        }

        /** Set the view size and content size along this range's axis. */
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

        /** Sets the position of the content along this range's axis. */
        protected boolean set (float cpos) {
            if (cpos == _cpos) return false;
            _cpos = cpos;
            _pos = _max == 0 ? 0 : cpos / _max * (_size - _extent);
            return true;
        }

        /** During size computation, extends the provided hint. */
        protected float extendHint (float hint) {
            // we want the content to take up as much space as it wants if this bar is on
            // TODO: use Float.MAX? that may cause trouble in other layout code
            return _on ? 100000 : hint;
        }

        /** If this range is in use. Set according to {@link Scroller.Behavior}. */
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
     * Handles the appearance and animation of scroll bars.
     */
    public static abstract class Bars
    {
        /**
         * Updates the scroll bars to match the current view and content size. This will be
         * called during layout, prior to the call to {@link #layer()}.
         */
        public void updateView () {}

        /**
         * Gets the layer to display the scroll bars. It gets added to the same parent as the
         * content's.
         */
        public abstract Layer layer ();

        /**
         * Updates the scroll bars' time based animation, if any, after the given time delta.
         */
        public void update (float dt) {}

        /**
         * Updates the scroll bars' positions. Not necessary for immediate layer bars.
         */
        public void updatePosition () {}

        /**
         * Destroys the resources created by the bars.
         */
        public void destroy () {
            layer().destroy();
        }

        /**
         * Space consumed by active scroll bars.
         */
        public float size () {
            return 0;
        }

        /**
         * Creates new bars for the given {@code Scroller}.
         */
        protected Bars (Scroller scroller) {
            _scroller = scroller;
        }

        protected final Scroller _scroller;
    }

    /**
     * Plain rectangle scroll bars that overlay the content area, consume no additional screen
     * space, and fade out after inactivity. Ideal for drag scrolling on a mobile device.
     */
    public static class TouchBars extends Bars
        implements ImmediateLayer.Renderer 
    {
        public TouchBars (Scroller scroller,
                int color, float size, float topAlpha, float fadeSpeed) {
            super(scroller);
            _color = color;
            _size = size;
            _topAlpha = topAlpha;
            _fadeSpeed = fadeSpeed;
            _layer = PlayN.graphics().createImmediateLayer(this);
        }

        @Override public void update (float delta) {
            // fade out the bars
            if (_alpha > 0 && _fadeSpeed > 0) setBarAlpha(_alpha - _fadeSpeed * delta);
        }

        @Override public void updatePosition () {
            // whenever the position changes, update to full visibility
            setBarAlpha(_topAlpha);
        }

        @Override public Layer layer () {
            return _layer;
        }

        @Override public void render (Surface surface) {
            surface.save();
            surface.setFillColor(_color);

            Range h = _scroller.hrange, v = _scroller.vrange;
            if (h.active()) drawBar(surface, h._pos, v._size - _size, h._extent, _size);
            if (v.active()) drawBar(surface, h._size - _size, v._pos, _size, v._extent);

            surface.restore();
        }

        protected void setBarAlpha (float alpha) {
            _alpha = Math.min(_topAlpha, Math.max(0, alpha));
            _layer.setAlpha(Math.min(_alpha, 1));
            _layer.setVisible(_alpha > 0);
        }

        protected void drawBar (Surface surface, float x, float y, float w, float h) {
            surface.fillRect(x, y, w, h);
        }

        protected float _alpha;
        protected float _topAlpha;
        protected float _fadeSpeed;
        protected int _color;
        protected float _size;
        protected Layer _layer;
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
        // means it hasn't been laid out yet and does not have its proper position; in that case
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

    /** The content contained in the scroller. */
    public final Element<?> content;

    /** Scroll ranges. */
    public final Range hrange = createRange(), vrange = createRange();

    /**
     * Creates a new scroller containing the given content and with {@link Behavior#BOTH}.
     * <p>If the content is an instance of {@link Clippable}, then translation will occur via
     * that interface. Otherwise, the content's layer translation will be set directly.
     * Graphics level clipping is always performed.</p>
     */
    public Scroller (Element<?> content) {
        setLayout(AxisLayout.horizontal().stretchByDefault().offStretch().gap(0));
        // our only immediate child is the _scroller, and that contains the content
        initChildren(_scroller = new Group(new ScrollLayout()) {
            @Override protected GroupLayer createLayer () {
                // use 1, 1 so we don't crash. the real size is set on validation
                return PlayN.graphics().createGroupLayer(1, 1);
            }
            @Override protected void layout () {
                super.layout();
                // do this after children have validated their bounding boxes
                updateVisibility();
            }
        });

        _scroller.add(this.content = content);

        // use the content's clipping method if it is Clippable
        if (content instanceof Clippable) {
            _clippable = (Clippable)content;

        } else {
            // otherwise, clip using layer translation
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
                if (vrange.active()) scrollY(ypos() + (int)(delta * viewSize().height()));
                else scrollX(xpos() + (int)(delta * viewSize().width()));
            }
        });

        // handle drag scrolling
        layer.addListener(_flicker = new XYFlicker());
    }

    /**
     * Sets the behavior of this scroller.
     */
    public Scroller setBehavior (Behavior beh) {
        hrange.setOn(beh.hasHorizontal());
        vrange.setOn(beh.hasVertical());
        invalidate();
        return this;
    }

    /**
     * Adds a listener to be notified of this scroller's changes.
     */
    public void addListener (Listener lner) {
        if (_lners == null) _lners = new ArrayList<Listener>();
        _lners.add(lner);
    }

    /**
     * Removes a previously added listener from this scroller.
     */
    public void removeListener (Listener lner) {
        if (_lners != null) _lners.remove(lner);
    }

    /**
     * Returns the offset of the left edge of the view area relative to that of the content.
     */
    public float xpos () {
        return hrange._cpos;
    }

    /**
     * Returns the offset of the top edge of the view area relative to that of the content.
     */
    public float ypos () {
        return vrange._cpos;
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
        x = Math.max(0, Math.min(x, hrange._max));
        y = Math.max(0, Math.min(y, vrange._max));
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
     * Gets the size of the content that we are responsible for scrolling. Scrolling is active for
     * a given axis when this is larger than {@link #viewSize} along that axis.
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
     * Gets the signal dispatched when a pointer click occurs in the scroller. This happens
     * only when the drag was not far enough to cause appreciable scrolling.
     */
    public Signal<Pointer.Event> contentClicked () {
        return _flicker.clicked;
    }

    /** Prepares the scroller for the next frame, at t = t + delta. */
    protected void update (float delta) {
        _flicker.update(delta);
        update(false);
        if (_bars != null) _bars.update(delta);
    }

    /** Updates the position of the content to match the flicker. If force is set, then the
     * relevant values will be updated even if there was no change. */
    protected void update (boolean force) {
        IPoint pos = _flicker.position();
        boolean dx = hrange.set(pos.x()), dy = vrange.set(pos.y());
        if (dx || dy || force) {
            _clippable.setPosition(-pos.x(), -pos.y());

            // now check the child elements for visibility
            if (!force) updateVisibility();

            firePositionChange();
            if (_bars != null) _bars.updatePosition();
        }
    }

    /**
     * A method for creating our {@code Range} instances. This is called once each for {@code
     * hrange} and {@code vrange} at creation time. Overriding this method will allow subclasses
     * to customize {@code Range} behavior.
     */
    protected Range createRange ()  {
        return new Range();
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
            @Override public void update (int dt) {
                Scroller.this.update(dt);
            }
        });
        invalidate();
    }

    @Override protected void wasRemoved () {
        _updater.remove();
        updateBars(null); // make sure bars get destroyed in case we don't get added again
        super.wasRemoved();
    }

    /** Hides the layers of any children of the content that are currently visible but outside
     * the clipping area. */
    // TODO: can we get the performance win without being so intrusive?
    protected void updateVisibility () {
        // only Container can participate, others must implement Clippable and do something else
        if (!(content instanceof Container)) {
            return;
        }

        // hide the layer of any child of content that isn't in bounds
        float x = hrange._cpos, y = vrange._cpos, wid = hrange._size, hei = vrange._size;
        for (Element<?> child : (Container<?>)content) {
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

    protected void updateBars (BarType barType) {
        if (_bars != null) {
            if (_barType == barType) return;
            _bars.destroy();
            _bars = null;
        }
        _barType = barType;
        if (_barType != null) _bars = _barType.createBars(this);
    }

    /** Extends the usual layout with scroll bar setup. */
    protected class BarsLayoutData extends CompositeLayoutData
    {
        public final BarType barType = resolveStyle(BAR_TYPE);

        @Override
        public void layout (float left, float top, final float width, final float height) {
            // set the bars first so the ScrollLayout can use it
            updateBars(barType);
            super.layout(left, top, width, height);
            if (_bars != null) layer.add(_bars.layer().setDepth(1).setTranslation(left,  top));
        }
    }

    /** Lays out the internal scroller group that contains the content. Performs all the jiggery
     * pokery necessary to make the content think it is in a large area and update the outer
     * {@code Scroller} instance. */
    protected class ScrollLayout extends Layout
    {
        @Override public Dimension computeSize (Container<?> elems, float hintX, float hintY) {
            // the content is always the 1st child, get the preferred size with extended hints
            assert elems.childCount() == 1 && elems.childAt(0) == content;
            _contentSize.setSize(preferredSize(elems.childAt(0),
                hrange.extendHint(hintX), vrange.extendHint(hintY)));
            return new Dimension(_contentSize);
        }

        @Override public void layout (Container<?> elems, float left, float top, float width,
                                      float height) {
            assert elems.childCount() == 1 && elems.childAt(0) == content;

            // if we're going to have H or V scrolling, make room on the bottom and/or right
            if (hrange.on() && _contentSize.width > width) height -= _bars.size();
            if (vrange.on() && _contentSize.height > height) width -= _bars.size();

            // reset ranges
            left = hrange.setRange(width, _contentSize.width);
            top = vrange.setRange(height, _contentSize.height);

            // let the bars know about the range change
            if (_bars != null) _bars.updateView();

            // set the content bounds to the large virtual area starting at 0, 0
            setBounds(content, 0, 0, hrange.contentSize(), vrange.contentSize());

            // clip the content in its own special way
            _clippable.setViewArea(width, height);

            // clip the scroller layer too, can't hurt
            ((GroupLayer.Clipped)_scroller.layer).setSize(width, height);

            // reset the flicker (it retains its current position)
            _flicker.reset(hrange.max(), vrange.max());

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

    protected final Group _scroller;
    protected final XYFlicker _flicker;
    protected final Clippable _clippable;
    protected final Dimension _contentSize = new Dimension();
    protected Interface.TaskHandle _updater;
    protected Point _queuedScroll;
    protected List<Listener> _lners;

    /** Scroll bar type, used to determine if the bars need to be recreated. */
    protected BarType _barType;

    /** Scroll bars, created during layout, based on the {@link BarType}. */
    protected Bars _bars;
}
