//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game.trans;

import tripleplay.game.ScreenStack;

/**
 * A base class for transitions that handles duration and the PITA machinery to case return values
 * to the right type.
 */
public abstract class AbstractTransition<T extends AbstractTransition>
    implements ScreenStack.Transition
{
    /** Configures the duration of the transition. */
    public T duration (float duration) {
        _duration = duration;
        return asT();
    }

    /**
     * Returns <code>this</code> cast to <code>T</code>.
     */
    @SuppressWarnings({"unchecked", "cast"}) protected T asT () {
        return (T)this;
    }

    protected float defaultDuration () {
        return 1000;
    }

    protected float _duration = defaultDuration();
}
