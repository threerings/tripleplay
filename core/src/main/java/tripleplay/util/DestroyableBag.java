//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.ArrayList;
import java.util.List;

import react.MultiFailureException;

import playn.core.Layer;

/**
 * Maintains a bag of {@link Destroyable} instances which can all be cleared at once. This is
 * useful for tracking connections and other managed resources on a screen and then clearing them
 * all at once when the screen is hidden or removed.
 */
public class DestroyableBag {

    /** Adds {@code dable} to this bag. No check is made to see if {@code dable} is already in the
     * bag. If you add something twice, it gets destroyed twice, so don't do that. */
    public DestroyableBag add (Destroyable dable) {
        _dables.add(dable);
        return this;
    }

    /** Adds {@code layer} to this bag. {@link Layer#destroy} will be called on destruction. */
    public DestroyableBag add (final Layer layer) {
        return add(new Destroyable() {
            public void destroy () {
                layer.destroy();
            }
        });
    }

    /** Adds {@code conn} to this bag. {@code disconnect} will be called on destruction. */
    public DestroyableBag add (final react.Connection conn) {
        return add(new Destroyable() {
            public void destroy () {
                conn.disconnect();
            }
        });
    }

    /** Adds {@code conn} to this bag. {@code disconnect} will be called on destruction. */
    public DestroyableBag add (final playn.core.Connection conn) {
        return add(new Destroyable() {
            public void destroy () {
                conn.disconnect();
            }
        });
    }

    /** Removes, and <em>does not destroy</em>, {@code dable} from this bag.
     * @return true if {@code dable} was found and removed from the bag, false if it was not in the
     * bag. */
    public boolean remove (Destroyable dable) {
        return _dables.remove(dable);
    }

    /** Destroys all destroyables in this bag and clears its contents. */
    public void clear () {
        MultiFailureException mfe = null;
        for (Destroyable dable : _dables) {
            try {
                dable.destroy();
            } catch (Exception e) {
                if (mfe == null) mfe = new MultiFailureException();
                mfe.addFailure(e);
            }
        }
        _dables.clear();
        if (mfe != null) mfe.trigger();
    }

    protected List<Destroyable> _dables = new ArrayList<Destroyable>();
}
