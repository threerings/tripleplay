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
        Animator anim = Animator.create();
        Runnable NOOP = new Runnable() { public void run () {} };
        Animator thenAnim = anim.action(NOOP).then();
        // it's OK to keep chaining animations
        thenAnim.action(NOOP).then().action(NOOP);
        // it's not OK to try to chain an animation off the then() animator that we kept a
        // reference to and have already chained an animation off of
        try {
            thenAnim.action(NOOP);
            fail("Double register failed to freakout");
        } catch (IllegalStateException ise) {} // success
    }
}
