//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.sound;

import playn.core.Sound;

/**
 * Represents a one-shot clip.
 */
public interface Clip extends Playable
{
    /** A noop clip. */
    class Silence implements Clip {
        @Override public float volume () { return 0; }
        @Override public void setVolume (float volume) {}
        @Override public boolean isPlaying () { return false; }
        @Override public void play () {}
        @Override public void fadeIn (float fadeMillis) {}
        @Override public void fadeOut (float fadeMillis) {}
        @Override public void stop () {}
        @Override public void release () {}
        @Override public void preload () {}
        @Override public Sound asSound () { return new Sound.Silence(); }
    }

    /** Fades this clip in over the specified duration. */
    void fadeIn (float fadeMillis);

    /** Fades this clip out over the specified duration. */
    void fadeOut (float fadeMillis);

    /** Preloads this clip's underlying audio data. */
    void preload ();

    /** Views this clip as a {@link Sound}. Only the {@link Sound#play} and {@link Sound#stop}
     * methods can be used. Useful for passing a clip into code that expects {@link Sound}. */
    Sound asSound ();
}
