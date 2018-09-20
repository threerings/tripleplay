//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.sound;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a sound clip that can be played multiple times. Callers tell the multiclip to prepare a
 * copy which may result in loading a copy of the sound from a {@link SoundBoard} if no reserves
 * are available to play it, they then play the sound, and after a configured duration that sound
 * either goes back into the reserves, or is disposed, if the reserves are already full.
 */
public class MultiClip
{
    /** A handle on a copy of a clip. Used to play it. */
    public interface Copy extends Playable {
        /** Plays this copy of the sound and then releases it. */
        void play ();
        /** Releases this copy of the sound without playing it. */
        void release ();
    }

    /**
     * Creates a multiclip with the supplied configuration.
     *
     * @param board the soundboard from which to obtain clips.
     * @param path the path to the underlying sound.
     * @param reserveCopies the minimum number of copies of the sound to keep in memory.
     * @param duration the duration of the sound (in seconds). This will be used to determine when
     * it is safe to reuse a copy of the sound.
     */
    public MultiClip (SoundBoard board, String path, int reserveCopies, float duration) {
        _board = board;
        _path = path;
        _reserveCopies = reserveCopies;
        _duration = duration * 1000;
    }

    /**
     * Obtains a copy of the sound (from the reserves if possible or loaded from storage if not).
     * The copy must be {@link Copy#play}ed or {@link Copy#release}d by the caller.
     */
    public Copy reserve () {
        double now = _board.plat.time();
        for (int ii = 0, ll = _copies.size(); ii < ll; ii++) {
            CopyImpl copy = _copies.get(ii);
            if (copy.releaseTime < now) {
                return _copies.remove(ii);
            }
        }
        return new CopyImpl();
    }

    /**
     * Releases all of the clips obtained by this multiclip, freeing their audio resources.
     */
    public void release () {
        for (CopyImpl copy : _copies) copy.sound.release();
        _copies.clear();
    }

    protected class CopyImpl implements Copy {
        public final Clip sound = _board.getClip(_path); {
            sound.setVolume(1);
            sound.preload();
        }
        public double releaseTime;

        @Override public float volume () {
            return sound.volume();
        }

        @Override public void setVolume (float volume) {
            sound.setVolume(volume);
        }

        @Override public void play () {
            sound.play();
            if (_copies.size() < _reserveCopies) {
                releaseTime = _board.plat.time() + _duration;
                _copies.add(this);
            }
        }

        @Override public void release () {
            if (_copies.size() < _reserveCopies) {
                releaseTime = _board.plat.time();
                _copies.add(this);
            }
        }

        @Override public void stop () {
            sound.stop();
            releaseTime = 0; // release immediately
        }

        @Override public boolean isPlaying () {
            return sound.isPlaying();
        }
    }

    protected final SoundBoard _board;
    protected final String _path;
    protected final int _reserveCopies;
    protected final float _duration;
    protected final List<CopyImpl> _copies = new ArrayList<CopyImpl>();
}
