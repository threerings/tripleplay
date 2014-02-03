//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.util.Clock;

/**
 * A shared interface by all display elements that can be animated.
 */
public interface Paintable
{
    /** Tells the element that time has passed. */
    void paint (Clock clock);
}
