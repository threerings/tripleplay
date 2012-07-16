//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.sound;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pythagoras.f.MathUtil;
import react.Slot;
import react.Value;

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
    /** Controls the volume of this sound board. */
    public Value<Float> volume = new Value<Float>(1f) {
        @Override protected Float updateAndNotifyIf (Float value) {
            return super.updateAndNotifyIf(MathUtil.clamp(value, 0, 1));
        }
    };

    /** Controls whether this sound board is muted. When muted, no sounds will play. */
    public Value<Boolean> muted = Value.create(false);

    public SoundBoard () {
        volume.connect(new Slot<Float>() { public void onEmit (Float volume) {
            for (LoopImpl active : _active) active.setVolume(volume);
        }});
        muted.connect(new Slot<Boolean>() { public void onEmit (Boolean muted) {
            if (muted) for (LoopImpl active : _active) active.fadeOut();
            else for (LoopImpl active : _active) active.fadeIn(volume.get());
        }});
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

    protected boolean shouldPlay () {
        return !muted.get() && volume.get() > 0;
    }

    protected abstract class ClipImpl extends LazySound implements Clip {
        @Override public void preload () {
            if (shouldPlay()) prepareSound();
        }
        @Override public void play () {
            if (shouldPlay()) prepareSound().play();
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
            if (!_active.add(this)) return;
            if (shouldPlay() && !isPlaying()) prepareAndPlay();
        }
        @Override public void stop () {
            if (_active.remove(this)) fadeOut();
        }
        public void fadeIn (float toVolume) {
            if (!isPlaying()) prepareAndPlay();
            // TODO: actually fade this in
            sound.setVolume(toVolume);
        }
        public void fadeOut () {
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

        @Override public int hashCode () {
            return path().hashCode();
        }
        @Override public boolean equals (Object other) {
            return (other == this) ? true :
                (other != null) && other.getClass() == getClass() &&
                path().equals(((LazySound)other).path());
        }

        protected Sound prepareSound () {
            if (sound == null) sound = assets().getSound(path());
            sound.setVolume(volume.get());
            return sound;
        }

        protected abstract String path ();
    }

    protected static class Fader {
        public Fader (Sound sound) {
            _sound = sound;
            _start = _sound.volume();
            System.err.println("Fading from " + _start);
        }

        public boolean update (float delta) {
            _elapsed += delta;
            float vol = Interpolator.LINEAR.apply(_start, -_start, _elapsed, FADE_DURATION);
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

    protected final Set<LoopImpl> _active = new HashSet<LoopImpl>();
    protected final List<Fader> _faders = new ArrayList<Fader>();
}
