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
        @Override public void preload () {}
        @Override public void play () {}
        @Override public void stop () {}
        @Override public boolean isPlaying () { return false; }
        @Override public Sound asSound () { return new Sound.Silence(); }
    }

    /** Preloads this clip's underlying audio data. */
    void preload ();

    /** Views this clip as a {@link Sound}. Only the {@link Sound#play} and {@link Sound#stop}
     * methods can be used. Useful for passing a clip into code that expects {@link Sound}. */
    Sound asSound ();
}
