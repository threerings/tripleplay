//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.sound;

/**
 * Represents a looped sound (i.e. music).
 */
public interface Loop extends Playable
{
    /** A noop loop. */
    class Silence implements Loop {
        @Override public float volume () { return 0; }
        @Override public void setVolume (float volume) {}
        @Override public boolean isPlaying () { return false; }
        @Override public void play () {}
        @Override public void fadeIn (float fadeMillis) {}
        @Override public void fadeOut (float fadeMillis) {}
        @Override public void stop () {}
        @Override public void release () {}
    }

    /** Fades this loop in over the specified duration. */
    void fadeIn (float fadeMillis);

    /** Fades this loop out over the specified duration. */
    void fadeOut (float fadeMillis);
}
