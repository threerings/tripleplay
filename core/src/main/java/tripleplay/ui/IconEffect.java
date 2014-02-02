//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Layer;
import playn.core.util.Callback;

/**
 * Used to apply effects to an Icon.
 */
public abstract class IconEffect
{
    /** Leaves well enough alone. */
    public static final IconEffect NONE = new IconEffect() {
        @Override public Icon apply (Icon icon) {
            return icon;
        }
    };

    /**
     * Creates an IconEffect that sets the alpha on the Icon's created layer.
     */
    public static final IconEffect alpha (final float alpha) {
        return new IconEffect() {
            @Override public Icon apply (Icon icon) {
                return new Proxy(icon) {
                    @Override public Layer render () {
                        return super.render().setAlpha(alpha);
                    }
                };
            }
        };
    }

    /** Does the needful. */
    public abstract Icon apply (Icon icon);

    /**
     * Wrap an Icon for fiddling.
     */
    protected static class Proxy implements Icon {
        protected Proxy (Icon icon) {
            _icon = icon;
        }

        @Override public float width () {
            return _icon.width();
        }

        @Override public float height () {
            return _icon.height();
        }

        @Override public Layer render () {
            return _icon.render();
        }

        @Override public void addCallback (Callback<? super Icon> callback) {
            _icon.addCallback(callback);
        }

        protected final Icon _icon;
    }
}
