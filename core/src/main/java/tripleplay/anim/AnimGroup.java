//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows one to specify a group of one or more animations that will be queued up to be started on
 * an {@link Animator} at some later time. All animations added to the group will be started in
 * parallel.
 *
 * <pre>{@code
 * AnimGroup group = new AnimGroup();
 * group.tweenXY(...).then().tweenAlpha(...);
 * group.play(sound).then().action(...);
 * // the two animation chains (the tween chain and the play/action chain) will run in parallel
 * // after this group is added to an Animator
 * anim.add(group.toAnim());
 * }</pre>
 *
 * One can combine multiple animation groups to achieve any desired construction of sequential and
 * parallel animations.
 *
 * <pre>{@code
 * AnimGroup group2 = new AnimGroup();
 * group2.tweenXY(...).then().tweenAlpha(...);
 * group2.play(sound).then().action(...);
 * AnimGroup group1 = new AnimGroup();
 * group1.delay(1000).then().add(group2.toAnim());
 * group1.delay(500).then().play(sound);
 * // group 1's two animation chains will be queued up to run in parallel, and the first of its
 * // chains will delay 1s and then trigger group 2's chains, which themselves run in parallel
 * anim.add(group1.toAnim());
 * }</pre>
 *
 * It is of course also possible to add a group with a single animation chain, which will contain
 * no parallelism but can still be useful for situations where one wants to compose sequences of
 * animations internally and then return that package of animations to be sequenced with other
 * packages of animations by some outer mechanism:
 *
 * <pre>{@code
 * class Ship {
 *   Animation createExplosionAnim () {
 *     AnimGroup group = new AnimGroup();
 *     group.play(sound).then().flipbook(...);
 *     return group.toAnim();
 *   }
 * }
 * }</pre>
 */
public class AnimGroup extends AnimBuilder
{
    /**
     * Adds an animation to this group. This animation will be started in parallel with all other
     * animations added to this group when the group is turned into an animation and started via
     * {@link Animator#add} or added to another chain of animations that was added to an animator.
     *
     * @throws IllegalStateException if this method is called directly or implicitly (by any of the
     * {@link AnimBuilder} fluent methods) after {@link #toAnim} has been called.
     */
    @Override public <T extends Animation> T add (T anim) {
        if (anim == null) throw new IllegalArgumentException("Animation can't be null.");
        if (_anims == null) throw new IllegalStateException("AnimGroup already animated.");
        _anims.add(anim);
        return anim;
    }

    /**
     * Returns a single animation that will execute all of the animations in this group to
     * completion (in parallel) and will report itself as complete when the final animation in the
     * group is complete. After calling this method, this group becomes unusable. It is not valid
     * to call {@link #add} (or any other method) after {@link #toAnim}.
     */
    public Animation toAnim () {
        final Animation[] groupAnims = _anims.toArray(new Animation[_anims.size()]);
        _anims = null;
        return new Animation() {
            @Override protected void init (float time) {
                super.init(time);
                for (int ii = 0; ii < groupAnims.length; ii++) {
                    (_curAnims[ii] = groupAnims[ii]).init(time);
                }
            }

            @Override protected float apply (Animator animator, float time) {
                _animator = animator;
                return super.apply(animator, time);
            }

            @Override protected float apply (float time) {
                float remain = Float.NEGATIVE_INFINITY;
                int processed = 0;
                for (int ii = 0; ii < _curAnims.length; ii++) {
                    Animation anim = _curAnims[ii];
                    if (anim == null) continue;
                    float aremain = anim.apply(_animator, time);
                    // if this animation is now complete, remove it from the array
                    if (aremain <= 0) _curAnims[ii] = null;
                    // note this animation's leftover time, we want our remaining time to be the
                    // highest remaining time of our internal animations
                    remain = Math.max(remain, aremain);
                    // note that we processed an animation
                    processed++;
                }
                // if we somehow processed zero animations, return 0 (meaning we're done) rather
                // than -infinity which would throw off any animation queued up after this one
                return processed == 0 ? 0 : remain;
            }
            
            @Override protected void complete () {
                if (_start == 0) {
                    // animation has not being initialized so cancel all base animations
                    for (int ii = 0; ii < groupAnims.length; ++ii) {
                        completeAnimation(groupAnims[ii]);
                    }
                } else {
                    // animation has started so cancel all active animations
                    for (int ii = 0; ii < _curAnims.length; ++ii) {
                        Animation anim = _curAnims[ii];
                        if (anim == null) {
                            continue;
                        }
                        
                        completeAnimation(anim);
                    }
                }
            }
            
            protected void completeAnimation (Animation animation) {
                // prevent recursion onto cancelled/completed animations
                if (animation._canceled) {
                    return;
                }
                
                // complete the animation first since it's first in the animation chain
                animation.complete();
                animation.cancel();
                
                // recursively complete the next animations
                Animation next = animation.next();
                while (next != null) {
                    completeAnimation(next);
                    next = next.next();
                }
            }

            protected Animator _animator;
            protected Animation[] _curAnims = new Animation[groupAnims.length];
        };
    }

    protected List<Animation> _anims = new ArrayList<Animation>();
}
