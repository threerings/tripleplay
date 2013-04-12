//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import java.util.ArrayList;
import java.util.List;

import playn.core.util.Clock;

import tripleplay.util.Paintable;

/**
 * Handles creation and management of animations. Animations may involve the tweening of a
 * geometric property of a layer (x, y, rotation, scale, alpha), or simple delays, or performing
 * actions. Animations can also be sequenced to orchestrate complex correlated actions.
 *
 * <p> The user of this class must call {@link #paint} with an up-to-date {@link Clock} on every
 * {@link playn.core.Game.Default#paint} call to drive the animations. </p>
 */
public class Animator extends AnimBuilder
    implements Paintable
{
    /** @deprecated Just construct Animator directly now. */
    @Deprecated public static Animator create () {
        return new Animator();
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
     * specified delay (in milliseconds) has elapsed <em>after this barrier becomes active</em>.
     * Any previously registered barriers must first expire and this barrier must move to the head
     * of the list before its delay timer will be started. This is probably what you want.
     */
    public void addBarrier (float delay) {
        Barrier barrier = new Barrier(delay);
        _barriers.add(barrier);
        // pushing a barrier causes subsequent animations to be accumulated separately
        _accum = barrier.accum;
    }

    /**
     * Performs per-frame animation processing. This should be called from your game's
     * {@code paint} method using an up-to-date clock.
     *
     * @param clock a clock containing the current alpha-adjusted time.
     */
    public void paint (Clock clock) {
        float time = clock.time();

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

    /**
     * Clears out any pending animations. <em>NOTE</em> all animations simply disappear. Any queued
     * animations that invoked actions will not execute, nor will the cleanup actions of any
     * animations that involve cleanup. This should only be invoked if you know the layers involved
     * in animations will be destroyed separately.
     */
    public void clear () {
        _anims.clear();
        _nanims.clear();
        _barriers.clear();
        _accum = _nanims;
    }

    /**
     * Registers an animation with this animator. It will be started on the next frame and continue
     * until cancelled or it reports that it has completed.
     */
    @Override public <T extends Animation> T add (T anim) {
        _accum.add(anim);
        return anim;
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

    protected List<Animation> _anims = new ArrayList<Animation>();
    protected List<Animation> _nanims = new ArrayList<Animation>();
    protected List<Animation> _accum = _nanims;
    protected List<Barrier> _barriers = new ArrayList<Barrier>();
}
