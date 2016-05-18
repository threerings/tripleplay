//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.sound;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pythagoras.f.MathUtil;
import react.Signal;
import react.Slot;
import react.Value;

import playn.core.Clock;
import playn.core.Platform;
import playn.core.Sound;

import tripleplay.util.Interpolator;

/**
 * Manages sound clips (sfx) and loops (music). Allows for master volume adjustment, and applying
 * adjusted volume to currently playing loops. The expected usage pattern is to create a separate
 * sound board for every collection of sounds that share a single volume control. For example, one
 * might create one board for SFX and one board for music so that each could be volume controlled
 * (and disabled) separately.
 */
public class SoundBoard {

    /** The platform on which this sound board is operating. */
    public final Platform plat;

    /** Controls the volume of this sound board. */
    public Value<Float> volume = new Value<Float>(1f) {
        @Override protected Float updateAndNotifyIf (Float value) {
            return super.updateAndNotifyIf(MathUtil.clamp(value, 0, 1));
        }
    };

    /** Controls whether this sound board is muted. When muted, no sounds will play. */
    public Value<Boolean> muted = Value.create(false);

    /** Creates a sound board which will play sounds via {@code plat} and connect to {@code paint}
      * to receive per-frame updates. */
    public SoundBoard (Platform plat, Signal<Clock> paint) {
        this.plat = plat;
        paint.connect(new Slot<Clock>() {
            public void onEmit (Clock clock) { update(clock.dt); }
        });
        volume.connect(new Slot<Float>() {
            @Override public void onEmit (Float volume) {
                for (LoopImpl active : _active) active.updateVolume(volume);
            }});
        muted.connect(new Slot<Boolean>() {
            @Override public void onEmit (Boolean muted) {
                for (LoopImpl active : _active) active.fadeForMute(muted);
            }});
    }

    /**
     * Creates and returns a clip with the supplied path. This clip will contain its own copy of
     * the sound data at the specified path, and thus should be retained by the caller if it will
     * be used multiple times before being released. Once all references to this clip are released,
     * it will be garbage collected and its sound data unloaded.
     */
    public Clip getClip (final String path) {
        return new ClipImpl() {
            @Override protected String path () { return path; }
        };
    }

    /**
     * Creates and returns a loop with the supplied path. The loop will contain its own copy of the
     * sound data at the specified path, and thus should be retained by the caller if it will be
     * used multiple times before being released. Once all references to this loop are released, it
     * will be garbage collected and its sound data unloaded.
     */
    public Loop getLoop (final String path) {
        return new LoopImpl() {
            @Override protected String path () { return path; }
        };
    }

    protected boolean shouldPlay () {
        return !muted.get() && volume.get() > 0;
    }

    protected void update (int delta) {
        // update any active faders
        for (int ii = 0, ll = _faders.size(); ii < ll; ii++) {
            if (_faders.get(ii).update(delta)) {
                _faders.remove(ii--);
                ll--;
            }
        }
    }

    protected abstract class ClipImpl extends LazySound implements Clip {
        @Override public void preload () {
            if (shouldPlay()) prepareSound();
        }
        @Override public void play () {
            if (shouldPlay()) prepareSound().play();
        }
        @Override public void fadeIn (float fadeMillis) {
            if (shouldPlay()) startFadeIn(fadeMillis);
        }
        @Override public void fadeOut (float fadeMillis) {
            if (shouldPlay()) startFadeOut(fadeMillis);
        }
        @Override public void stop () {
            if (isPlaying()) sound.stop();
        }
        @Override public Sound asSound () {
            return new Sound() {
                @Override public boolean play () {
                    ClipImpl.this.play();
                    return true;
                }
                @Override public void stop() {
                    ClipImpl.this.stop();
                }
            };
        }
        @Override public String toString () {
            return "clip:" + sound;
        }
        @Override protected Sound loadSound (String path) {
            return plat.assets().getSound(path);
        }
    }

    protected abstract class LoopImpl extends LazySound implements Loop {
        public void fadeForMute (boolean muted) {
            if (muted) startFadeOut(FADE_DURATION);
            else startFadeIn(FADE_DURATION);
        }

        @Override public void play () {
            if (!_active.add(this)) return;
            if (shouldPlay() && !isPlaying()) prepareSound().play();
        }
        @Override public void fadeIn (float fadeMillis) {
            if (_active.add(this) && shouldPlay()) startFadeIn(fadeMillis);
        }
        @Override public void fadeOut (float fadeMillis) {
            if (_active.remove(this) && shouldPlay()) startFadeOut(fadeMillis);
        }
        @Override public void stop () {
            if (_active.remove(this) && isPlaying()) sound.stop();
        }
        @Override public String toString () {
            return "loop:" + sound;
        }

        @Override protected Sound prepareSound () {
            Sound sound = super.prepareSound();
            sound.setLooping(true);
            return sound;
        }
        @Override protected void fadeOutComplete () {
            sound.release();
            sound = null;
        }
        @Override protected Sound loadSound (String path) {
            return plat.assets().getMusic(path);
        }
    }

    protected abstract class LazySound implements Playable {
        public Sound sound;

        @Override public boolean isPlaying () {
            return (sound == null) ? false : sound.isPlaying();
        }
        @Override public void setVolume (float volume) {
            _volume = volume;
            updateVolume(SoundBoard.this.volume.get());
        }
        @Override public float volume () {
            return _volume;
        }
        @Override public void release () {
            if (sound != null) {
                if (sound.isPlaying()) sound.stop();
                sound.release();
                sound = null;
            }
        }

        @Override public int hashCode () {
            return path().hashCode();
        }
        @Override public boolean equals (Object other) {
            return (other == this) ? true :
                (other != null) && other.getClass() == getClass() &&
                path().equals(((LazySound)other).path());
        }

        public void updateVolume (float volume) {
            if (isPlaying()) {
                float effectiveVolume = volume * _volume;
                if (effectiveVolume > 0) sound.setVolume(effectiveVolume);
                else stop();
            }
        }

        protected void startFadeIn (final float fadeMillis) {
            cancelFaders();
            if (!isPlaying()) prepareSound().play();
            sound.setVolume(0); // start at zero, fade in from there
            _faders.add(new Fader() {
                @Override public boolean update (int delta) {
                    _elapsed += delta;
                    float vol = Interpolator.LINEAR.apply(0, _range, _elapsed, fadeMillis);
                    updateVolume(vol);
                    return (vol >= _range); // we're done when the volume hits the target
                }
                @Override public void cancel () {}
                protected final float _range = volume.get();
                protected int _elapsed;
            });
        }

        protected void startFadeOut (final float fadeMillis) {
            cancelFaders();
            if (isPlaying()) _faders.add(new Fader() {
                @Override public boolean update (int delta) {
                    _elapsed += delta;
                    float vol = Interpolator.LINEAR.apply(_start, -_start, _elapsed, fadeMillis);
                    updateVolume(vol);
                    if (vol > 0) return false;
                    else { // we're done when the volume hits zero
                        fadeOutComplete();
                        return true;
                    }
                }
                @Override public void cancel () {
                    updateVolume(0);
                    fadeOutComplete();
                }
                protected final float _start = volume.get();
                protected int _elapsed;
            });
        }

        protected void cancelFaders () {
            for (Fader fader : _faders) {
                fader.cancel();
            }
            _faders.clear();
        }

        protected Sound prepareSound () {
            if (sound == null) {
                sound = loadSound(path());
                sound.prepare();
            }
            sound.setVolume(volume.get() * _volume);
            return sound;
        }

        protected void fadeOutComplete () {
        }

        protected abstract Sound loadSound (String path);
        protected abstract String path ();

        protected float _volume = 1;
    }

    protected abstract class Fader {
        public abstract boolean update (int delta);
        public abstract void cancel ();
    }

    protected final Set<LoopImpl> _active = new HashSet<LoopImpl>();
    protected final List<Fader> _faders = new ArrayList<Fader>();

    protected static final float FADE_DURATION = 1000;
}
