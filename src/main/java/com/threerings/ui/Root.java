//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import pythagoras.f.IDimension;

/**
 * The root of a display hierarchy. An application can have one or more roots, but they should not
 * overlap and will behave as if oblivious to one another's existence.
 */
public class Root extends Group
{
    public Root (Layout layout) {
        super(layout);
    }

    /**
     * Sizes this root element to its preferred size.
     */
    public void pack () {
        IDimension psize = getPreferredSize(0, 0);
        setSize(psize.getWidth(), psize.getHeight());
    }

    /**
     * Sets the size of this root element.
     */
    public void setSize (float width, float height) {
        _size.setSize(width, height);
        invalidate();
    }

    /**
     * Updates this root element. Must be called from {@link Game#update}.
     */
    public void update (float delta) {
        // TODO
    }

    /**
     * "Paints" this root element. Must be called from {@link Game#update}.
     */
    public void paint (float alpha) {
        if (!_valid) {
            layout();
            _valid = true;
        }
    }

    @Override protected Root getRoot () {
        return this;
    }

    @Override protected void invalidate () {
        _valid = false;
    }

    protected boolean _valid;
}
