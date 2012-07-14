//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.XY;
import react.Value;

import playn.core.Asserts;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.Sound;

import tripleplay.sound.MultiSound;
import tripleplay.sound.SoundBoard;
import tripleplay.util.Layers;

/**
 * Handles creation and management of animations. Animations may involve the tweening of a
 * geometric property of a layer (x, y, rotation, scale, alpha), or simple delays, or performing
 * actions. Animations can also be sequenced to orchestrate complex correlated actions.
 */
public abstract class Animator
{
    /**
     * Creates an instance of an animator. The caller is responsible for calling {@link #update} on
     * said animator to drive the animation process.
     */
    public static Animator create () {
        return new Impl();
    }

    /**
     * Registers an animation with this animator. It will be started on the next frame and continue
     * until cancelled or it reports that it has completed.
     */
    public abstract <T extends Animation> T add (T tween);

    /**
     * Starts a tween on the supplied layer's x/y-translation.
     */
    public Animation.Two tweenTranslation (Layer layer) {
        return tweenXY(layer);
    }

    /**
     * Starts a tween on the supplied layer's x/y-translation.
     */
    public Animation.Two tweenXY (Layer layer) {
        return add(new Animation.Two(onX(layer), onY(layer)));
    }

    /**
     * Starts a tween on the supplied layer's x-translation.
     */
    public Animation.One tweenX (Layer layer) {
        return tween(onX(layer));
    }

    /**
     * Starts a tween on the supplied layer's y-translation.
     */
    public Animation.One tweenY (Layer layer) {
        return tween(onY(layer));
    }

    /**
     * Starts a tween on the supplied layer's rotation.
     */
    public Animation.One tweenRotation (final Layer layer) {
        Asserts.checkNotNull(layer);
        return tween(new Animation.Value() {
            public float initial () {
                return layer.transform().rotation();
            }
            public void set (float value) {
                layer.setRotation(value);
            }
        });
    }

    /**
     * Starts a tween on the supplied layer's x/y-scale.
     */
    public Animation.One tweenScale (final Layer layer) {
        Asserts.checkNotNull(layer);
        return tween(new Animation.Value() {
            public float initial () {
                return layer.transform().uniformScale();
            }
            public void set (float value) {
                layer.setScale(value);
            }
        });
    }

    /**
     * Starts a tween on the supplied layer's x/y-scale.
     */
    public Animation.Two tweenScaleXY (Layer layer) {
        return add(new Animation.Two(onScaleX(layer), onScaleY(layer)));
    }

    /**
     * Starts a tween on the supplied layer's x-scale.
     */
    public Animation.One tweenScaleX (Layer layer) {
        return tween(onScaleX(layer));
    }

    /**
     * Starts a tween on the supplied layer's y-scale.
     */
    public Animation.One tweenScaleY (Layer layer) {
        return tween(onScaleY(layer));
    }

    /**
     * Starts a tween on the supplied layer's transparency.
     */
    public Animation.One tweenAlpha (final Layer layer) {
        Asserts.checkNotNull(layer);
        return tween(new Animation.Value() {
            public float initial () {
                return layer.alpha();
            }
            public void set (float value) {
                layer.setAlpha(value);
            }
        });
    }

    /**
     * Starts a flipbook animation that displays in {@code layer}. Note that the image layer in
     * question will have its translation adjusted based on the offset of the current frame. Thus
     * it should be placed into a {@link GroupLayer} if it is to be positioned and animated
     * separately.
     */
    public Animation.Flip flipbook (ImageLayer layer, Flipbook book) {
        return add(new Animation.Flip(layer, book));
    }

    /**
     * Starts a flipbook animation in a new image layer which is created and added to {@code box}.
     * When the flipbook animation is complete, the newly created image layer will not be destroyed
     * automatically. This allows the animation to be repeated, if desired. The caller must destroy
     * eventually the image layer, or more likely, destroy {@code box} which will cause the created
     * image layer to be destroyed.
     */
    public Animation.Flip flipbook (GroupLayer box, Flipbook book) {
        ImageLayer image = PlayN.graphics().createImageLayer();
        box.add(image);
        return flipbook(image, book);
    }

    /**
     * Starts a flipbook animation that displays the supplied {@code book} at the specified
     * position in the supplied parent. The intermediate layers created to display the flipbook
     * animation will be destroyed on completion.
     */
    public Animation flipbookAt (GroupLayer parent, float x, float y, Flipbook book) {
        GroupLayer box = PlayN.graphics().createGroupLayer();
        box.setTranslation(x, y);
        return add(parent, box).then().flipbook(box, book).then().destroy(box);
    }

    /**
     * Starts a flipbook animation that displays the supplied {@code book} at the specified
     * position in the supplied parent. The intermediate layers created to display the flipbook
     * animation will be destroyed on completion.
     */
    public Animation flipbookAt (GroupLayer parent, XY pos, Flipbook book) {
        return flipbookAt(parent, pos.x(), pos.y(), book);
    }

    /**
     * Starts a tween using the supplied custom value. {@link Animation.Value#initial} will be used
     * (if needed) to obtain the initial value before the tween begins. {@link Animation.Value#set}
     * will be called each time the tween is updated with the intermediate values.
     */
    public Animation.One tween (Animation.Value value) {
        return add(new Animation.One(value));
    }

    /**
     * Creates an animation that delays for the specified number of seconds.
     */
    public Animation.Delay delay (float seconds) {
        return add(new Animation.Delay(seconds));
    }

    /**
     * Returns an animator which can be used to construct an animation that will be repeated until
     * the supplied layer has been removed from its parent. The layer must be added to a parent
     * before the next frame (if it's not already), or the cancellation will trigger immediately.
     */
    public Animator repeat (Layer layer) {
        return add(new Animation.Repeat(layer)).then();
    }

    /**
     * Creates an animation that executes the supplied runnable and immediately completes.
     */
    public Animation.Action action (Runnable action) {
        return add(new Animation.Action(action));
    }

    /**
     * Adds the supplied child to the supplied parent. This is generally done as the beginning of a
     * chain of animations, which itself may be delayed or subject to animation barriers.
     */
    public Animation.Action add (final GroupLayer parent, final Layer child) {
        return action(new Runnable() {
            public void run () {
                parent.add(child);
            }
        });
    }

    /**
     * Adds the supplied child to the supplied parent at the specified translation. This is
     * generally done as the beginning of a chain of animations, which itself may be delayed or
     * subject to animation barriers.
     */
    public Animation.Action addAt (GroupLayer parent, Layer child, XY pos) {
        return addAt(parent, child, pos.x(), pos.y());
    }

    /**
     * Adds the supplied child to the supplied parent at the specified translation. This is
     * generally done as the beginning of a chain of animations, which itself may be delayed or
     * subject to animation barriers.
     */
    public Animation.Action addAt (final GroupLayer parent,
                                   final Layer child, final float x, final float y) {
        return action(new Runnable() {
            public void run () {
                parent.addAt(child, x, y);
            }
        });
    }

    /**
     * Reparents the supplied child to the supplied new parent. This involves translating the
     * child's current coordinates to screen coordinates, moving it to its new parent layer and
     * translating its coordinates into the coordinate space of the new parent. Thus the child does
     * not change screen position, even though its coordinates relative to its parent will most
     * likely have changed.
     */
    public Animation.Action reparent(final GroupLayer newParent, final Layer child) {
        return action(new Runnable() {
            public void run () {
                Layers.reparent(child, newParent);
            }
        });
    }

    /**
     * Destroys the specified layer. This is generally done as the end of a chain of animations,
     * which culminate in the removal (destruction) of the target layer.
     */
    public Animation.Action destroy (final Layer layer) {
        return action(new Runnable() {
            public void run () {
                layer.destroy();
            }
        });
    }

    /**
     * Sets the specified layer's depth to the specified value.
     */
    public Animation.Action setDepth (final Layer layer, final float depth) {
        return action(new Runnable() {
            public void run () {
                layer.setDepth(depth);
            }
        });
    }

    /**
     * Sets the specified layer to visible or not.
     */
    public Animation.Action setVisible (final Layer layer, final boolean visible) {
        return action(new Runnable() {
            public void run () {
                layer.setVisible(visible);
            }
        });
    }

    /**
     * Sets a boolean value to true or false, usually to simplify the enabling/disabling of UI
     * components during an animation.
     */
    public Animation.Action setAnimating (final Value<Boolean> anim, final boolean animating) {
        return action(new Runnable() {
            public void run () {
                anim.update(animating);
            }
        });
    }

    /**
     * Plays the supplied clip or loop.
     */
    public Animation.Action play (final SoundBoard.Playable sound) {
        return action(new Runnable() {
            public void run () {
                sound.play();
            }
        });
    }

    /**
     * Plays the supplied sound.
     */
    public Animation.Action play (final Sound sound) {
        return action(new Runnable() {
            public void run () {
                sound.play();
            }
        });
    }

    /**
     * Tweens the volumne of the supplied sound. Useful for fade-ins and fade-outs. Note, this does
     * not play or stop the sound, those must be enacted separately.
     */
    public Animation.One tweenVolume (final Sound sound) {
        Asserts.checkNotNull(sound);
        return tween(new Animation.Value() {
            public float initial () {
                return sound.volume();
            }
            public void set (float value) {
                sound.setVolume(value);
            }
        });
    }

    /**
     * Stops the supplied sound from playing.
     */
    public Animation.Action stop (final Sound sound) {
        return action(new Runnable() {
            public void run () {
                sound.stop();
            }
        });
    }

    /**
     * Causes this animator to delay the start of any subsequently registered animations until all
     * currently registered animations are complete.
     */
    public void addBarrier () {
        addBarrier(0);
    }

    /**
     * Causes this animator to delay the start of any subsequently registered animations until the
     * specified delay has elapsed <em>after this barrier becomes active</em>. Any previously
     * registered barriers must first expire and this barrier must move to the head of the list
     * before its delay timer will be started. This is probably what you want.
     */
    public void addBarrier (float delay) {
        throw new UnsupportedOperationException(
            "Barriers are only supported on the top-level animator.");
    }

    /**
     * Performs per-frame animation processing.
     * @param time a monotonically increasing seconds value.
     */
    public void update (float time) {
        // nada by default
    }

    protected static Animation.Value onX (final Layer layer) {
        Asserts.checkNotNull(layer);
        return new Animation.Value() {
            public float initial () {
                return layer.transform().tx();
            }
            public void set (float value) {
                layer.transform().setTx(value);
            }
        };
    }

    protected static Animation.Value onY (final Layer layer) {
        Asserts.checkNotNull(layer);
        return new Animation.Value() {
            public float initial () {
                return layer.transform().ty();
            }
            public void set (float value) {
                layer.transform().setTy(value);
            }
        };
    }

    protected static Animation.Value onScaleX (final Layer layer) {
        Asserts.checkNotNull(layer);
        return new Animation.Value() {
            public float initial () {
                return layer.transform().scaleX();
            }
            public void set (float value) {
                layer.transform().setScaleX(value);
            }
        };
    }

    protected static Animation.Value onScaleY (final Layer layer) {
        Asserts.checkNotNull(layer);
        return new Animation.Value() {
            public float initial () {
                return layer.transform().scaleY();
            }
            public void set (float value) {
                layer.transform().setScaleY(value);
            }
        };
    }

    /** Implementation details, avert your eyes. */
    protected static class Impl extends Animator {
        @Override public <T extends Animation> T add (T anim) {
            _accum.add(anim);
            return anim;
        }

        @Override public void addBarrier (float delay) {
            Barrier barrier = new Barrier(delay);
            _barriers.add(barrier);
            // pushing a barrier causes subsequent animations to be accumulated separately
            _accum = barrier.accum;
        }

        @Override public void update (float time) {
            // if we have any animations queued up to be added, add those now
            if (!_nanims.isEmpty()) {
                for (int ii = 0, ll = _nanims.size(); ii < ll; ii++) {
                    _nanims.get(ii).init(time);
                }
                _anims.addAll(_nanims);
                _nanims.clear();
            }

            // now process all of our registered animations
            for (int ii = 0, ll = _anims.size(); ii < ll; ii++) {
                if (_anims.get(ii).apply(this, time) <= 0) {
                    _anims.remove(ii--);
                    ll -= 1;
                }
            }

            // if we have no active animations, or a timed barrier has expired, unblock a barrier
            boolean noActiveAnims = _anims.isEmpty() && _nanims.isEmpty();
            if (!_barriers.isEmpty() && (noActiveAnims || _barriers.get(0).expired(time))) {
                Barrier barrier = _barriers.remove(0);
                _nanims.addAll(barrier.accum);
                // if we just unblocked the last barrier, start accumulating back on _nanims
                if (_barriers.isEmpty()) {
                    _accum = _nanims;
                }
            }
        }

        protected List<Animation> _anims = new ArrayList<Animation>();
        protected List<Animation> _nanims = new ArrayList<Animation>();
        protected List<Animation> _accum = _nanims;
        protected List<Barrier> _barriers = new ArrayList<Barrier>();
    }

    /** Implementation details, avert your eyes. */
    protected static class Barrier {
        public List<Animation> accum = new ArrayList<Animation>();
        public float expireDelay;
        public float absoluteExpireTime;

        public Barrier (float expireDelay) {
            this.expireDelay = expireDelay;
        }

        public boolean expired (float time) {
            if (expireDelay == 0) return false;
            if (absoluteExpireTime == 0) absoluteExpireTime = time + expireDelay;
            return time > absoluteExpireTime;
        }
    }
}
