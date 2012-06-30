//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.sound;

import java.util.ArrayList;
import java.util.List;

import playn.core.PlayN;
import playn.core.Sound;

/**
 * Provides a sound that can be played multiple times. Callers tell the multisound to prepare a
 * copy which may result in loading a copy of the sound from {@link Assets} if no reserves are
 * available to play it, they then play the sound, and after a configured duration that sound
 * either goes back into the reserves, or is disposed, if the reserves are already full.
 */
public class MultiSound
{
    /** A handle on a copy of a sound. Used to play it. */
    public interface Copy {
        /** Plays this copy of the sound and then releases it. */
        void play ();
        /** Releases this copy of the sound without playing it. */
        void release ();
    }

    /**
     * Creates a multisound with the supplied configuration.
     *
     * @param path the path to the underlying sound.
     * @param reserveCopies the minimum number of copies of the sound to keep in memory.
     * @param duration the duration of the sound (in seconds). This will be used to determine when
     * it is safe to reuse a copy of the sound.
     */
    public MultiSound (String path, int reserveCopies, float duration) {
        _path = path;
        _reserveCopies = reserveCopies;
        _duration = duration * 1000;
    }

    /**
     * Obtains a copy of the sound (from the reserves if possible or loaded from storage if not).
     * The copy must be {@link Copy#play}ed or {@link Copy#release}d by the caller.
     */
    public Copy reserve () {
        double now = PlayN.currentTime();
        for (int ii = 0, ll = _copies.size(); ii < ll; ii++) {
            CopyImpl copy = _copies.get(ii);
            if (copy.releaseTime < now) {
                return _copies.remove(ii);
            }
        }
        return new CopyImpl();
    }

    protected class CopyImpl implements Copy {
        public final Sound sound = PlayN.assets().getSound(_path);
        public double releaseTime;

        public void play () {
            sound.play();
            if (_copies.size() < _reserveCopies) {
                releaseTime = PlayN.currentTime() + _duration;
                _copies.add(this);
            }
        }

        public void release () {
            if (_copies.size() < _reserveCopies) {
                releaseTime = PlayN.currentTime();
                _copies.add(this);
            }
        }
    }

    protected final String _path;
    protected final int _reserveCopies;
    protected final float _duration;
    protected final List<CopyImpl> _copies = new ArrayList<CopyImpl>();
}
