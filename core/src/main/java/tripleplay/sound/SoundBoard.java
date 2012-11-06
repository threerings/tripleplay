//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.sound;

import static playn.core.PlayN.assets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import playn.core.Game;
import playn.core.Sound;
import pythagoras.f.MathUtil;
import react.Slot;
import react.Value;
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
	protected static final float DEFAULT_FADE_DURATION = 1000;

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
			for (LoopImpl active : _active) active.updateVolume();
        }});
        muted.connect(new Slot<Boolean>() { public void onEmit (Boolean muted) {
            if (muted) for (LoopImpl active : _active) active.fadeOut();
            else for (LoopImpl active : _active) active.fadeIn();
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
			if (isPlaying())
				_faders.add(new FadeOut(sound, DEFAULT_FADE_DURATION));
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
        
        @Override protected void configSound() {
			// do nothing
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

		@Override protected void configSound() {
			sound.setLooping(true);
		}
    }

    protected abstract class LazySound {
		protected Sound sound;
		private float channelVolume = 1;

		protected void prepareAndPlay() {
			Sound sound = prepareSound();
			configSound();
			sound.play();
		}

		protected abstract void configSound();

		public void fadeIn() {
			fadeIn(DEFAULT_FADE_DURATION);
		}

		public void fadeIn(float duration) {
			if (!isPlaying()) {
				prepareAndPlay();
			}
			_faders.add(new FadeIn(sound, getRealVolume(), duration));
		}

		public void fadeOut() {
			fadeOut(DEFAULT_FADE_DURATION);
		}

		public void fadeOut(float duration) {
			if (isPlaying())
				_faders.add(new FadeOut(sound, duration));
		}

		public float getRealVolume() {
			return channelVolume * SoundBoard.this.volume.get();
		}

		public void updateVolume() {
			if (sound != null) {
				float realVolume = getRealVolume();
				if (realVolume > 0) {
					sound.setVolume(realVolume);
				} else if (isPlaying()) {
					sound.stop();
				}
			}
		}

		public float volume() {
			return channelVolume;
		}

		public void setVolume(float volume) {
			channelVolume = MathUtil.clamp(volume, 0, 1);
			updateVolume();
		}

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
			sound.setVolume(getRealVolume());
            return sound;
        }

        protected abstract String path ();
    }

	protected abstract static class Fader {
		public Fader (Sound sound,float duration) {
            _sound = sound;
			_duration = duration;
        }

		public abstract boolean update(float delta);

        protected final Sound _sound;
		protected final float _duration;
        protected float _elapsed;
    }

	protected static class FadeOut extends Fader {
		public FadeOut(Sound sound, float duration) {
			super(sound, duration);
			// _sound.volume is the real volume
			_start = _sound.volume();
		}

		public boolean update(float delta) {
			_elapsed += delta;
			float vol = Interpolator.EASE_OUT.apply(_start, -_start, _elapsed, _duration);
			if (vol > 0) {
				_sound.setVolume(vol);
				return false;
			}
			_sound.stop();
			return true;
		}

		protected final float _start;
	}

	protected static class FadeIn extends Fader {
		private float _volume;
		public FadeIn(Sound sound, float volume, float duration) {
			super(sound, duration);
			sound.setVolume(0);
			this._volume = volume;
		}

		public boolean update(float delta) {
			_elapsed += delta;
			float vol = Interpolator.EASE_IN.apply(0, _volume, _elapsed, _duration);
			_sound.setVolume(vol);
			// PlayN.log().debug("volume: " + vol);
			return vol >= _volume;
		}

	}

    protected final Set<LoopImpl> _active = new HashSet<LoopImpl>();
    protected final List<Fader> _faders = new ArrayList<Fader>();
}
