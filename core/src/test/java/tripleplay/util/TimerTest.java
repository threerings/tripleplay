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
        Counter r1 = new Counter();
        timer.after(10, r1);
        // make sure the timer hasn't run yet
        timer.update(3);
        assertEquals(0, r1.ranCount);
        // elapse time past our expiry and make sure it has run
        timer.update(15);
        assertEquals(1, r1.ranCount);
    }

    @Test
    public void testCancelBefore () {
        Timer timer = new Timer(0);
        Counter r1 = new Counter();
        Timer.Handle h1 = timer.after(10, r1);
        // make sure the timer hasn't run yet
        timer.update(3);
        assertEquals(0, r1.ranCount);
        h1.cancel();
        // elapse time past our expiry and make sure our canceled timer did not run
        timer.update(15);
        assertEquals(0, r1.ranCount);
    }

    @Test
    public void testRepeat () {
        long time = 25;
        Timer timer = new Timer(time);
        Counter r1 = new Counter();
        Timer.Handle h1 = timer.every(10, r1);
        // make sure the timer hasn't run yet
        timer.update(time += 3);
        assertEquals(0, r1.ranCount);
        // elapse time past our expiry and make sure our action ran
        timer.update(time += 10);
        assertEquals(1, r1.ranCount);
        // elapse time past our expiry again and make sure our action ran again
        timer.update(time += 10);
        assertEquals(2, r1.ranCount);
        // cancel our timer and make sure it ceases to run
        h1.cancel();
        timer.update(time += 10);
        assertEquals(2, r1.ranCount);
    }

    @Test
    public void testMultiple () {
        long time = 0;
        Timer timer = new Timer(time);
        Counter r1 = new Counter(), r2 = new Counter(), r3 = new Counter();
        // set up timers to expire after 10, 10, and 25, the latter two repeating every 10 and 15
        timer.after(10, r1);
        timer.every(10, r2);
        timer.atThenEvery(25, 15, r3);

        // T=T+3: no timers have run
        time += 3; timer.update(time);
        assertEquals(0, r1.ranCount);
        assertEquals(0, r2.ranCount);
        assertEquals(0, r3.ranCount);

        // T=T+13; h1 and h2 expire, h3 doesn't
        time += 10; timer.update(time);
        assertEquals(1, r1.ranCount);
        assertEquals(1, r2.ranCount);
        assertEquals(0, r3.ranCount);

        // T=T+23; h1 is gone, h2 expires again, h3 not yet
        time += 10; timer.update(time);
        assertEquals(1, r1.ranCount);
        assertEquals(2, r2.ranCount);
        assertEquals(0, r3.ranCount);

        // T=T+33; h1 is gone, h2 expires again, h3 expires once
        time += 10; timer.update(time);
        assertEquals(1, r1.ranCount);
        assertEquals(3, r2.ranCount);
        assertEquals(1, r3.ranCount);

        // T=T+43; h2 expires again, h3 expires again
        time += 10; timer.update(time);
        assertEquals(1, r1.ranCount);
        assertEquals(4, r2.ranCount);
        assertEquals(2, r3.ranCount);
    }

    @Test
    public void testOrder () {
        long time = 0;
        Timer timer = new Timer(time);
        final int[] r1 = new int[1], r2 = new int[1], r3 = new int[1];

        // make sure that three timers set to expire at the same time go off in the order they were
        // registered
        timer.every(10, new Runnable() {
            public void run () {
                r1[0] += 1;
                assertTrue(r1[0] == r2[0]+1);
                assertTrue(r1[0] == r3[0]+1);
            }
        });
        timer.every(10, new Runnable() {
            public void run () {
                assertTrue(r1[0] == r2[0]+1);
                r2[0] += 1;
                assertTrue(r2[0] == r3[0]+1);
            }
        });
        timer.every(10, new Runnable() {
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
    public void testRescheduler () {
        final Timer timer = new Timer(0);

        // a sub task that may be rescheduled
        final Counter sub = new Counter();

        // a timer task that schedules the sub
        Counter main = new Counter() {
            @Override public void run () {
                super.run();
                sub.cancel();
                sub.handle = timer.after(2, sub);
            }
        };

        // queue up the subtask for tick 2
        timer.after(0, main);
        timer.update(0);
        assertEquals(1, main.ranCount);
        assertEquals(0, sub.ranCount);

        // dequeue and queue the subtask for tick 3
        timer.after(0, main);
        timer.update(1);
        assertEquals(2, main.ranCount);
        assertEquals(0, sub.ranCount);

        // process to tick 3, it should run the subtask once
        timer.update(2);
        timer.update(3);
        assertEquals(2, main.ranCount);
        assertEquals(1, sub.ranCount);
    }

    @Test
    public void testDoubleCancel () {
        final Counter ran1 = new Counter();
        final Counter ran2 = new Counter();
        Timer t = new Timer(0);
        Timer.Handle h = t.after(1, ran1);
        t.after(2, ran2);
        t.update(1);
        h.cancel();
        h.cancel();
        t.update(2);
        assertEquals(1, ran1.ranCount);
        assertEquals(1, ran2.ranCount);
    }

    @Test
    public void testInternalCancel () {
        final InternalCanceler ran1 = new InternalCanceler();
        Timer t = new Timer(0);
        ran1.handle = t.every(2, ran1);
        for (int ii = 0; ii < 7; ii++) t.update(ii + 1);
        assertEquals(1, ran1.ranCount);
    }

    protected static class Counter implements Runnable
    {
        public int ranCount;
        public Timer.Handle handle;

        public void run () {
            ++ranCount;
        }

        public void cancel () {
            if (handle != null) {
                handle.cancel();
                handle = null;
            }
        }
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

    protected static class InternalCanceler extends Counter
    {
        @Override public void run () {
            super.run();
            cancel();
        }
    }
}
