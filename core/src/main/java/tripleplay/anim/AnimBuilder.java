//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import pythagoras.f.XY;
import react.Signal;
import react.Value;

import playn.core.Asserts;
import playn.core.GroupLayer;
import playn.core.ImageLayer;
import playn.core.Layer;
import playn.core.Sound;
import static playn.core.PlayN.graphics;

import tripleplay.sound.Playable;
import tripleplay.util.Destroyable;
import tripleplay.util.Layers;

/**
 * Provides a fluent interface for building single chains of animations. See {@link Animator} for a
 * concrete entry point.
 */
public abstract class AnimBuilder
{
    /**
     * Registers an animation with this builder. If this is the root animator, it will be started
     * on the next frame and continue until cancelled or it reports that it has completed. If this
     * is an animator returned from {@link Animation#then} then the queued animation will be
     * started when the animation on which {@code then} was called has completed.
     */
    public abstract <T extends Animation> T add (T anim);

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
        return tween(onXY(layer));
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
     * Starts a tween on the supplied layer's origin.
     */
    public Animation.Two tweenOrigin (Layer layer) {
        return tween(onOrigin(layer));
    }

    /**
     * Starts a tween on the supplied layer's rotation.
     */
    public Animation.One tweenRotation (final Layer layer) {
        Asserts.checkNotNull(layer);
        return tween(new Animation.Value() {
            public float initial () { return layer.rotation(); }
            public void set (float value) { layer.setRotation(value); }
        });
    }

    /**
     * Starts a tween on the supplied layer's x/y-scale.
     */
    public Animation.One tweenScale (final Layer layer) {
        Asserts.checkNotNull(layer);
        return tween(new Animation.Value() {
            public float initial () { return layer.scaleX(); }
            public void set (float value) { layer.setScale(value); }
        });
    }

    /**
     * Starts a tween on the supplied layer's x/y-scale.
     */
    public Animation.Two tweenScaleXY (Layer layer) {
        return tween(onScaleXY(layer));
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
            public float initial () { return layer.alpha(); }
            public void set (float value) { layer.setAlpha(value); }
        });
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
     * Starts a tween using the supplied custom X/Y value.
     */
    public Animation.Two tween (Animation.XYValue value) {
        return add(new Animation.Two(value));
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
        ImageLayer image = graphics().createImageLayer();
        box.add(image);
        return flipbook(image, book);
    }

    /**
     * Starts a flipbook animation that displays the supplied {@code book} at the specified
     * position in the supplied parent. The intermediate layers created to display the flipbook
     * animation will be destroyed on completion.
     */
    public Animation flipbookAt (GroupLayer parent, float x, float y, Flipbook book) {
        GroupLayer box = graphics().createGroupLayer();
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
     * Creates a shake animation on the specified layer.
     */
    public Animation.Shake shake (Layer layer) {
        return add(new Animation.Shake(layer));
    }

    /**
     * Creates an animation that delays for the specified duration in milliseconds.
     */
    public Animation.Delay delay (float duration) {
        return add(new Animation.Delay(duration));
    }

    /**
     * Returns a builder which can be used to construct an animation that will be repeated until
     * the supplied layer has been removed from its parent. The layer must be added to a parent
     * before the next frame (if it's not already), or the cancellation will trigger immediately.
     */
    public AnimBuilder repeat (Layer layer) {
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
        return action(new Runnable() { public void run () {
            parent.add(child);
        }});
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
        return action(new Runnable() { public void run () {
            parent.addAt(child, x, y);
        }});
    }

    /**
     * Reparents the supplied child to the supplied new parent. This involves translating the
     * child's current coordinates to screen coordinates, moving it to its new parent layer and
     * translating its coordinates into the coordinate space of the new parent. Thus the child does
     * not change screen position, even though its coordinates relative to its parent will most
     * likely have changed.
     */
    public Animation.Action reparent (final GroupLayer newParent, final Layer child) {
        return action(new Runnable() { public void run () {
            Layers.reparent(child, newParent);
        }});
    }

    /**
     * Destroys the specified layer. This is generally done as the end of a chain of animations,
     * which culminate in the removal (destruction) of the target layer.
     */
    public Animation.Action destroy (final Layer layer) {
        return action(new Runnable() { public void run () {
            if (!layer.destroyed()) layer.destroy();
        }});
    }

    /**
     * Destroys the specified destroyable.
     */
    public Animation.Action destroy (final Destroyable dable) {
        return action(new Runnable() { public void run () {
            dable.destroy();
        }});
    }

    /**
     * Sets the specified layer's depth to the specified value.
     */
    public Animation.Action setDepth (final Layer layer, final float depth) {
        return action(new Runnable() { public void run () {
            layer.setDepth(depth);
        }});
    }

    /**
     * Sets the specified layer to visible or not.
     */
    public Animation.Action setVisible (final Layer layer, final boolean visible) {
        return action(new Runnable() { public void run () {
            layer.setVisible(visible);
        }});
    }

    /**
     * Plays the supplied clip or loop.
     */
    public Animation.Action play (final Playable sound) {
        return action(new Runnable() { public void run () {
            sound.play();
        }});
    }

    /**
     * Stops the supplied clip or loop.
     */
    public Animation.Action stop (final Playable sound) {
        return action(new Runnable() { public void run () {
            sound.stop();
        }});
    }

    /**
     * Plays the supplied sound.
     */
    public Animation.Action play (final Sound sound) {
        return action(new Runnable() { public void run () {
            sound.play();
        }});
    }

    /**
     * Tweens the volume of the supplied playable. Note, this does not play or stop the sound,
     * those must be enacted separately.
     */
    public Animation.One tweenVolume (final Playable sound) {
        Asserts.checkNotNull(sound);
        return tween(new Animation.Value() {
            public float initial () { return sound.volume(); }
            public void set (float value) { sound.setVolume(value); }
        });
    }

    /**
     * Tweens the volume of the supplied sound. Useful for fade-ins and fade-outs. Note, this does
     * not play or stop the sound, those must be enacted separately.
     */
    public Animation.One tweenVolume (final Sound sound) {
        Asserts.checkNotNull(sound);
        return tween(new Animation.Value() {
            public float initial () { return sound.volume(); }
            public void set (float value) { sound.setVolume(value); }
        });
    }

    /**
     * Stops the supplied sound from playing.
     */
    public Animation.Action stop (final Sound sound) {
        return action(new Runnable() { public void run () {
            sound.stop();
        }});
    }

    /**
     * Emits {@code value} on {@code signal}.
     */
    public <T> Animation.Action emit (final Signal<T> signal, final T value) {
        return action(new Runnable() { public void run () {
            signal.emit(value);
        }});
    }

    /**
     * Sets a value to the supplied constant.
     */
    public <T> Animation.Action setValue (final Value<T> value, final T newValue) {
        return action(new Runnable() { public void run () {
            value.update(newValue);
        }});
    }

    /**
     * Increments (or decrements if {@code amount} is negative} an int value.
     */
    public Animation.Action increment (final Value<Integer> value, final int amount) {
        return action(new Runnable() { public void run () {
            value.update(value.get() + amount);
        }});
    }

    protected static Animation.Value onX (final Layer layer) {
        Asserts.checkNotNull(layer);
        return new Animation.Value() {
            public float initial () { return layer.tx(); }
            public void set (float value) { layer.setTx(value); }
        };
    }

    protected static Animation.Value onY (final Layer layer) {
        Asserts.checkNotNull(layer);
        return new Animation.Value() {
            public float initial () { return layer.ty(); }
            public void set (float value) { layer.setTy(value); }
        };
    }

    protected static Animation.XYValue onXY (final Layer layer) {
        Asserts.checkNotNull(layer);
        return new Animation.XYValue() {
            public float initialX () { return layer.tx(); }
            public float initialY () { return layer.ty(); }
            public void set (float x, float y) { layer.setTranslation(x, y); }
        };
    }

    protected static Animation.Value onScaleX (final Layer layer) {
        Asserts.checkNotNull(layer);
        return new Animation.Value() {
            public float initial () { return layer.scaleX(); }
            public void set (float value) { layer.setScaleX(value); }
        };
    }

    protected static Animation.Value onScaleY (final Layer layer) {
        Asserts.checkNotNull(layer);
        return new Animation.Value() {
            public float initial () { return layer.scaleY(); }
            public void set (float value) { layer.setScaleY(value); }
        };
    }

    protected static Animation.XYValue onScaleXY (final Layer layer) {
        Asserts.checkNotNull(layer);
        return new Animation.XYValue() {
            public float initialX () { return layer.scaleX(); }
            public float initialY () { return layer.scaleY(); }
            public void set (float x, float y) { layer.setScale(x, y); }
        };
    }

    protected static Animation.XYValue onOrigin (final Layer layer) {
        Asserts.checkNotNull(layer);
        return new Animation.XYValue() {
            public float initialX () { return layer.originX(); }
            public float initialY () { return layer.originY(); }
            public void set (float x, float y) { layer.setOrigin(x, y); }
        };
    }
}
