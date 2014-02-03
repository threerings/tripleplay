//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game.trans;

import tripleplay.util.Interpolator;

/**
 * Handles shared code for transitions that use an interpolation.
 */
public abstract class InterpedTransition<T extends InterpedTransition<T>> extends AbstractTransition<T>
{
    public T linear () { return interp(Interpolator.LINEAR); }
    public T easeIn () { return interp(Interpolator.EASE_IN); }
    public T easeOut () { return interp(Interpolator.EASE_OUT); }
    public T easeInOut () { return interp(Interpolator.EASE_INOUT); }

    public T interp (Interpolator interp) {
        _interp = interp;
        return asT();
    }

    protected Interpolator defaultInterpolator () {
        return Interpolator.EASE_INOUT;
    }

    protected Interpolator _interp = defaultInterpolator();
}
