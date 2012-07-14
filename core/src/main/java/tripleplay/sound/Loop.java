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
    /** Returns whether this loop is currently playing. */
    boolean isPlaying ();
}
