//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.sound;

/**
 * Represents a looped sound (i.e. music).
 */
public interface Loop extends Playable
{
    /** A noop loop. */
    class Silence implements Loop {
        @Override public void play () {}
        @Override public void stop () {}
        @Override public boolean isPlaying () { return false; }
    }
}
