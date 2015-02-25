//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.Collections;
import java.util.Iterator;

import pythagoras.f.Dimension;
import pythagoras.f.FloatMath;
import pythagoras.f.Point;
import react.Closeable;
import react.Slot;

import playn.core.Clock;
import playn.core.Graphics;
import playn.scene.GroupLayer;
import playn.scene.LayerUtil;

import tripleplay.shaders.RotateYBatch;
import tripleplay.util.Interpolator;

/**
 * A container that holds zero or one widget. The box delegates everything to its current contents
 * (its preferred size is its content's preferred size, it sizes its contents to its size).
 */
public class Box extends Container.Mutable<Box> {

    /** A {@code Box} which draws its children clipped to their preferred size. */
    public static class Clipped extends Box {
        /** Creates an empty clipped box. */
        public Clipped () { this(null); }

        /** Creates a clipped box with the specified starting contents. */
        public Clipped (Element<?> contents) { set(contents); }

        @Override protected GroupLayer createLayer () { return new GroupLayer(1, 1); }
        @Override protected void wasValidated () {
            layer.setSize(size().width(), size().height());
        }
    }

    /** Manages transitions for {@link #transition}. */
    public static abstract class Trans extends Slot<Clock> {

        /** Configures the interpolator to use for the transition. */
        public Trans interp (Interpolator interp) {
            _interp = interp;
            return this;
        }

        protected Trans (int duration) {
            _duration = duration;
        }

        void start (Box box, Element<?> ncontents) {
            if (_box != null) throw new IllegalStateException("Cannot reuse transitions.");
            _box = box;
            _ocontents = box.contents();

            _ncontents = ncontents;
            _box.didAdd(_ncontents);
            _ncontents.setLocation(_ocontents.x(), _ocontents.y());
            _ncontents.setSize(_ocontents.size().width(), _ocontents.size().height());
            _ncontents.validate();

            _conn = box.root().iface.frame.connect(this);
            init();
            update(0);
        }

        @Override public void onEmit (Clock clock) {
            _elapsed += clock.dt;
            float pct = Math.min(_elapsed/_duration, 1);
            // TODO: interp!
            update(_interp.apply(pct));
            if (pct == 1) {
                _box.set(_ncontents); // TODO: avoid didAdd
                _conn.close();
                cleanup();
            }
        }

        protected void init () {}
        protected abstract void update (float pct);
        protected void cleanup () {}

        protected Element<?> _ocontents, _ncontents;

        private final float _duration; // ms
        private float _elapsed;
        private Box _box;
        private Interpolator _interp = Interpolator.LINEAR;
        private Closeable _conn;
    }

    /** A transition that fades from the old contents to the new. */
    public static class Fade extends Trans {
        public Fade (int duration) { super(duration); }

        @Override protected void update (float pct) {
            _ocontents.layer.setAlpha(1-pct);
            _ncontents.layer.setAlpha(pct);
        }
        @Override protected void cleanup () {
            _ocontents.layer.setAlpha(1);
        }
    }

    public static class Flip extends Trans {
        public Flip (int duration) { super(duration); }

        @Override protected void init () {
            // TODO: compute the location of the center of the box in screen coordinates, place
            // the eye there in [0, 1] coords
            Graphics gfx = _ocontents.root().iface.plat.graphics();
            Point eye = LayerUtil.layerToScreen(
                _ocontents.layer, _ocontents.size().width()/2, _ocontents.size().height()/2);
            eye.x /= gfx.viewSize.width();
            eye.y /= gfx.viewSize.height();
            _obatch = new RotateYBatch(gfx.gl, eye.x, eye.y, 1);
            _nbatch = new RotateYBatch(gfx.gl, eye.x, eye.y, 1);
            _ocontents.layer.setBatch(_obatch);
            _ncontents.layer.setBatch(_nbatch);
        }
        @Override protected void update (float pct) {
            _obatch.angle = FloatMath.PI * pct;
            _nbatch.angle = -FloatMath.PI * (1-pct);
            _ocontents.layer.setVisible(pct < 0.5f);
            _ncontents.layer.setVisible(pct >= 0.5f);
        }
        @Override protected void cleanup () {
            _ocontents.layer.setBatch(null);
            _ncontents.layer.setBatch(null);
        }

        protected RotateYBatch _obatch, _nbatch;
    }

    /** Creates an empty box. */
    public Box () {
        this(null);
    }

    /** Creates a box with the specified starting contents. */
    public Box (Element<?> contents) {
        set(contents);
    }

    /** Returns the box's current contents. */
    public Element<?> contents () {
        return _contents;
    }

    /** Updates the box's contents. The previous contents, if any, is removed but not destroyed.
     * To destroy the old contents and set the new, use {@code destroyContents().set(contents)}.*/
    public Box set (Element<?> contents) {
        if (contents != _contents) set(contents, false);
        return this;
    }

    /** Performs an animated transition from the box's current contents to {@code contents}.
      * @param trans describes and manages the transition (duration, style, etc.). */
    public Box transition (Element<?> contents, Trans trans) {
        trans.start(this, contents);
        return this;
    }

    /** Clears out the box's current contents. */
    public Box clear () {
        return set(null);
    }

    /** Clears out the box's current contents and destroys it immediately. */
    public Box destroyContents () {
        return set((Element<?>)null, true);
    }

    @Override public Stylesheet stylesheet () {
        return null; // boxes provide no styles
    }

    @Override public int childCount () {
        return (_contents == null) ? 0 : 1;
    }

    @Override public Element<?> childAt (int index) {
        if (_contents == null || index != 0) throw new IndexOutOfBoundsException();
        return _contents;
    }

    @Override public Iterator<Element<?>> iterator () {
        return (_contents == null) ?
            Collections.<Element<?>>emptyList().iterator() :
            Collections.<Element<?>>singleton(_contents).iterator();
    }

    @Override public void remove (Element<?> child) {
        if (_contents == child) clear();
    }

    @Override public void removeAt (int index) {
        if (_contents == null || index != 0) throw new IndexOutOfBoundsException();
        clear();
    }

    @Override public void removeAll () {
        clear();
    }

    @Override public void destroy (Element<?> child) {
        if (_contents == child) destroyContents();
    }

    @Override public void destroyAt (int index) {
        if (_contents == null || index != 0) throw new IndexOutOfBoundsException();
        destroyContents();
    }

    @Override public void destroyAll () {
        destroyContents();
    }

    @Override protected Class<?> getStyleClass () {
        return Box.class;
    }

    @Override protected void wasAdded () {
        super.wasAdded();
        if (_contents != null) {
            _contents.set(Flag.IS_ADDING, true);
            _contents.wasAdded();
        }
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        if (_contents != null) {
            if (isSet(Flag.WILL_DISPOSE)) _contents.set(Flag.WILL_DISPOSE, true);
            _contents.set(Flag.IS_REMOVING, true);
            _contents.wasRemoved();
        }
    }

    protected Box set (Element<?> contents, boolean destroy) {
        if (_contents != null) {
            didRemove(_contents, destroy);
        }
        _contents = contents;
        if (contents != null) {
            didAdd(contents);
        }
        invalidate();
        return this;
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new BoxLayoutData();
    }

    protected class BoxLayoutData extends LayoutData {
        @Override public Dimension computeSize (float hintX, float hintY) {
            return (_contents == null) ? new Dimension() : _contents.computeSize(hintX, hintY);
        }

        @Override public void layout (float left, float top, float width, float height) {
            if (_contents != null) {
                _contents.setSize(width, height);
                _contents.setLocation(left, top);
                _contents.validate();
            }
        }
    }

    protected Element<?> _contents;
}
