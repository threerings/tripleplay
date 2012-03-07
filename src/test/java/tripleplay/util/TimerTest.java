//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import org.junit.*;
import static org.junit.Assert.*;

public class TimerTest
{
    @Test
    public void testOneShot () {
        Timer timer = new Timer(0);
        int[] r1 = new int[1];
        Timer.Handle h1 = timer.after(10, action(r1));
        // make sure the timer hasn't run yet
        timer.update(3);
        assertEquals(0, r1[0]);
        // elapse time past our expirey and make sure it has run
        timer.update(15);
        assertEquals(1, r1[0]);
    }

    @Test
    public void testCancelBefore () {
        Timer timer = new Timer(0);
        int[] r1 = new int[1];
        Timer.Handle h1 = timer.after(10, action(r1));
        // make sure the timer hasn't run yet
        timer.update(3);
        assertEquals(0, r1[0]);
        h1.cancel();
        // elapse time past our expirey and make sure our canceled timer did not run
        timer.update(15);
        assertEquals(0, r1[0]);
    }

    @Test
    public void testRepeat () {
        long time = 25;
        Timer timer = new Timer(time);
        int[] r1 = new int[1];
        Timer.Handle h1 = timer.every(10, action(r1));
        // make sure the timer hasn't run yet
        timer.update(time += 3);
        assertEquals(0, r1[0]);
        // elapse time past our expirey and make sure our action ran
        timer.update(time += 10);
        assertEquals(1, r1[0]);
        // elapse time past our expirey again and make sure our action ran again
        timer.update(time += 10);
        assertEquals(2, r1[0]);
        // cancel our timer and make sure it ceases to run
        h1.cancel();
        timer.update(time += 10);
        assertEquals(2, r1[0]);
    }

    @Test
    public void testMultiple () {
        long time = 0;
        Timer timer = new Timer(time);
        int[] r1 = new int[1], r2 = new int[2], r3 = new int[3];
        // set up timers to expire after 10, 10, and 25, the latter two repeating every 10 and 15
        Timer.Handle h1 = timer.after(10, action(r1));
        Timer.Handle h2 = timer.every(10, action(r2));
        Timer.Handle h3 = timer.atThenEvery(25, 15, action(r3));

        // T=T+3: no timers have run
        time += 3; timer.update(time);
        assertEquals(0, r1[0]);
        assertEquals(0, r2[0]);
        assertEquals(0, r3[0]);

        // T=T+13; h1 and h2 expire, h3 doesn't
        time += 10; timer.update(time);
        assertEquals(1, r1[0]);
        assertEquals(1, r2[0]);
        assertEquals(0, r3[0]);

        // T=T+23; h1 is gone, h2 expires again, h3 not yet
        time += 10; timer.update(time);
        assertEquals(1, r1[0]);
        assertEquals(2, r2[0]);
        assertEquals(0, r3[0]);

        // T=T+33; h1 is gone, h2 expires again, h3 expires once
        time += 10; timer.update(time);
        assertEquals(1, r1[0]);
        assertEquals(3, r2[0]);
        assertEquals(1, r3[0]);

        // T=T+43; h2 expires again, h3 expires again
        time += 10; timer.update(time);
        assertEquals(1, r1[0]);
        assertEquals(4, r2[0]);
        assertEquals(2, r3[0]);
    }

    @Test
    public void testOrder () {
        long time = 0;
        Timer timer = new Timer(time);
        final int[] r1 = new int[1], r2 = new int[2], r3 = new int[3];

        // make sure that three timers set to expire at the same time go off in the order they were
        // registered
        Timer.Handle h1 = timer.every(10, new Runnable() {
            public void run () {
                r1[0] += 1;
                assertTrue(r1[0] == r2[0]+1);
                assertTrue(r1[0] == r3[0]+1);
            }
        });
        Timer.Handle h2 = timer.every(10, new Runnable() {
            public void run () {
                assertTrue(r1[0] == r2[0]+1);
                r2[0] += 1;
                assertTrue(r2[0] == r3[0]+1);
            }
        });
        Timer.Handle h3 = timer.every(10, new Runnable() {
            public void run () {
                assertTrue(r1[0] == r3[0]+1);
                assertTrue(r2[0] == r3[0]+1);
                r3[0] += 1;
            }
        });

        // T=T+3: no timers have run
        time += 3; timer.update(time);
        assertEquals(0, r1[0]);
        assertEquals(0, r2[0]);
        assertEquals(0, r3[0]);

        // T=T+13: all timers have run once
        time += 10; timer.update(time);
        assertEquals(1, r1[0]);
        assertEquals(1, r2[0]);
        assertEquals(1, r3[0]);

        // T=T+23: all timers have run twice
        time += 10; timer.update(time);
        assertEquals(2, r1[0]);
        assertEquals(2, r2[0]);
        assertEquals(2, r3[0]);
    }

    @Test
    public void testConcurrentReschedule () {
        // check to make sure we can reschedule concurrently
        new Rescheduler(true).test();
        new Rescheduler(false).test();
    }

    @Test
    public void testDoubleCancel () {
        final int[] ran = {0};
        Timer t = new Timer(0);
        Timer.Handle h = t.after(1, action(ran));
        t.after(2, action(ran));
        t.update(1);
        h.cancel();
        h.cancel();
        t.update(2);
        assertEquals(2, ran[0]);
    }

    protected Runnable action (final int[] ranCount) {
        return new Runnable() {
            public void run () {
                ranCount[0] += 1;
            }
        };
    }

    protected static class Rescheduler implements Runnable
    {
        public Timer timer = new Timer(0);
        public Timer.Handle handle;
        public int ran;
        public boolean cancelBefore;

        public Rescheduler (boolean cancelBefore) {
            handle = timer.after(1, this);
            this.cancelBefore = cancelBefore;
        }

        public void run () {
            ran++;
            Timer.Handle h = handle;
            if (cancelBefore) {
                h.cancel();
            }
            handle = timer.after(1, this);
            if (!cancelBefore) {
                h.cancel();
            }
        }

        public void test () {
            timer.update(1);
            timer.update(2);
            assertEquals(2, ran);
        }
    }
}
