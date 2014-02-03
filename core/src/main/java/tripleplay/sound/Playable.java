//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.sound;

/**
 * Shared controls for clips and loops.
 */
public interface Playable
{
    /** Returns the current volume configured for this clip. Note that the actual volume will be
     * the configured volume multiplied by the master volume of the owning soundboard. */
    float volume ();

    /** Configures the volume for this clip to a value between 0 and 1. Note that the actual volume
     * will be the configured volume multiplied by the master volume of the owning soundboard. */
    void setVolume (float volume);

    /** Returns true if this playable is currently playing, false otherwise. */
    boolean isPlaying ();

    /** Starts this clip or loop playing. If the sound data is not yet loaded it will be loaded and
     * then played. */
    void play ();

    /** Stops this clip or loop (fading it out over one second). */
    void stop ();

    /** Releases this playable when it is no longer needed. This releases any associated audio
     * resources.If this playable is used again, the underlying sound will be reloaded. */
    void release ();
}
