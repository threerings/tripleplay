//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import org.junit.*;
import static org.junit.Assert.*;

public class AnimatorTest
{
    @Test public void testAnimDoubleRegisterFreakout () {
        Animator anim = new Animator();
        Runnable NOOP = new Runnable() { public void run () {} };
        AnimationBuilder chain = anim.action(NOOP).then();
        // it's OK to keep chaining animations
        chain.action(NOOP).then().action(NOOP);
        // it's not OK to try to chain an animation off the then() builder to which we kept a
        // reference and off of which we have already chained an animation
        try {
            chain.action(NOOP);
            fail("Double register failed to freakout");
        } catch (IllegalStateException ise) {} // success
    }
}
