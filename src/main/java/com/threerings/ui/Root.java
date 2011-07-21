//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

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
            // TODO: revalidate!
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
