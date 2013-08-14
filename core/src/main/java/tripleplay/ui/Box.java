//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.Collections;
import java.util.Iterator;

import pythagoras.f.Dimension;

/**
 * A container that holds zero or one widget. The box delegates everything to its current contents
 * (its preferred size is its content's preferred size, it sizes its contents to its size).
 */
public class Box extends Container.Mutable<Box> {

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
            if (isSet(Flag.WILL_DESTROY)) _contents.set(Flag.WILL_DESTROY, true);
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
