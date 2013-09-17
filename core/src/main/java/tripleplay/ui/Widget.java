//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

/**
 * The base class for all user interface widgets. Provides helper methods for managing a canvas
 * into which a widget is rendered when its state changes.
 */
public abstract class Widget<T extends Widget<T>> extends Element<T>
{
    protected Widget () {
        _behave = createBehavior();
        if (_behave != null) {
            // absorbs clicks and do not descend (propagate clicks to sublayers)
            set(Flag.HIT_DESCEND, false);
            set(Flag.HIT_ABSORB, true);
            // wire up our behavior as a layer listener
            layer.addListener(_behave);
        }
    }

    @Override protected void layout () {
        super.layout();
        if (_behave != null) _behave.layout();
    }

    /**
     * Creates the behavior for this widget, if any. Defaults to returning null, which means no
     * behavior. This is called once, in the widget's constructor.
     */
    protected Behavior<T> createBehavior () {
        return null;
    }

    /**
     * Extends base Glyph to automatically wire up to this Widget's {@link #layer}.
     */
    protected class Glyph extends tripleplay.util.Glyph {
        public Glyph () {
            super(layer);
        }
    }

    protected final Behavior<T> _behave;
}
