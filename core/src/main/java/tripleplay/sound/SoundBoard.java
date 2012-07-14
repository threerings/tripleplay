//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pythagoras.f.MathUtil;

import playn.core.Game;
import playn.core.Sound;
import static playn.core.PlayN.assets;

import tripleplay.util.Interpolator;

/**
 * Manages sound clips (sfx) and loops (music). Allows for master volume adjustment, and applying
 * adjusted volume to currently playing loops. The expected usage pattern is to create a separate
 * sound board for every collection of sounds that share a single volume control. For example, one
 * might create one board for SFX and one board for music so that each could be volume controlled
 * (and disabled) separately.
 */
public class SoundBoard
{
    /**
     * Returns the master volume for this sound board. The value will be between 0 and 1.
     */
    public float volume () {
        return _volume;
    }

    /**
     * Configures the volume for all sounds in this board. If a zero volume is supplied, the board
     * will be disabled. Any currently playing loops will have their volume adjusted.
     *
     * @param volume a value between 0 and 1.
     */
    public void setVolume (float volume) {
        _volume = MathUtil.clamp(volume, 0, 1);
        // adjust the volume of any currently playing loops
        for (LoopImpl active : _active) active.setVolume(_volume);
    }

    /**
     * This must be called from your {@link Game#update} method.
     */
    public void update (float delta) {
        // update any active faders
        for (int ii = 0, ll = _faders.size(); ii < ll; ii++) {
            if (_faders.get(ii).update(delta)) {
                _faders.remove(ii--);
                ll--;
            }
        }
    }

    /**
     * Creates and returns a clip with the supplied path. This clip will contain its own copy of
     * the sound data at the specified path, and thus should be retained by the caller if it will
     * be used multiple times before being released. Once all references to this clip are released,
     * it will be garbage collected and its sound data unloaded.
     */
    public Clip getClip (final String path) {
        return new ClipImpl() {
            protected String path () { return path; }
        };
    }

    /**
     * Creates and returns a loop with the supplied path. THe loop will contain its own copy of the
     * sound data at the specified path, and thus should be retained by the caller if it will be
     * used multiple times before being released. Once all references to this loop are released, it
     * will be garbage collected and its sound data unloaded.
     */
    public Loop getLoop (final String path) {
        return new LoopImpl() {
            protected String path () { return path; }
        };
    }

    protected abstract class ClipImpl extends LazySound implements Clip {
        @Override public void preload () {
            if (_volume > 0) prepareSound();
        }
        @Override public void play () {
            if (_volume > 0) prepareSound().play();
        }
        @Override public void stop () {
            if (isPlaying()) _faders.add(new Fader(sound));
        }
        @Override public Sound asSound () {
            return new Sound.Silence() {
                @Override public boolean play () {
                    ClipImpl.this.play();
                    return true;
                }
                @Override public void stop() {
                    ClipImpl.this.stop();
                }
            };
        }
    }

    protected abstract class LoopImpl extends LazySound implements Loop {
        @Override public void play () {
            if (isPlaying()) return;
            prepareAndPlay();
            _active.add(this);
        }
        @Override public void stop () {
            _active.remove(this);
            if (isPlaying()) _faders.add(new Fader(sound));
        }
        public void setVolume (float volume) {
            if (volume > 0) {
                if (!isPlaying()) prepareAndPlay();
                sound.setVolume(volume);
            } else if (isPlaying()) {
                sound.stop();
            }
        }
        protected void prepareAndPlay () {
            Sound sound = prepareSound();
            sound.setLooping(true);
            sound.play();
        }
    }

    protected abstract class LazySound {
        public Sound sound;

        public boolean isPlaying () {
            return (sound == null) ? false : sound.isPlaying();
        }

        protected Sound prepareSound () {
            if (sound == null) sound = assets().getSound(path());
            sound.setVolume(_volume);
            return sound;
        }

        protected abstract String path ();
    }

    protected static class Fader {
        public Fader (Sound sound) {
            _sound = sound;
            _start = _sound.volume();
        }

        public boolean update (float delta) {
            _elapsed += delta;
            float vol = Interpolator.EASE_IN.apply(_start, -_start, _elapsed, FADE_DURATION);
            if (vol > 0) {
                _sound.setVolume(vol);
                return false;
            }
            _sound.stop();
            return true;
        }

        protected final Sound _sound;
        protected final float _start;
        protected float _elapsed;

        protected static final float FADE_DURATION = 1000;
    }

    protected final List<LoopImpl> _active = new ArrayList<LoopImpl>();
    protected final List<Fader> _faders = new ArrayList<Fader>();
    protected float _volume;
}
