//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.sound;

/**
 * Shared controls for clips and loops.
 */
public interface Playable
{
    /** Starts this clip or loop playing. If the sound data is not yet loaded it will be loaded and
     * then played. */
    void play ();

    /** Stops this clip or loop (fading it out over one second). */
    void stop ();

    /** Returns true if this playable is currently playing, false othwerise. */
    boolean isPlaying ();

	void setVolume(float volume);

	float volume();

	void fadeIn(float duration);

	void fadeIn();

	void fadeOut(float duration);

	void fadeOut();
}
