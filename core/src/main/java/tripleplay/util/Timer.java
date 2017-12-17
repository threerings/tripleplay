//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import playn.core.Platform;
import react.Slot;

/**
 * Handles execution of actions after a specified delay. Create a {@code Timer} and connect it to
 * the {@link Platform#frame} signal:
 *
 * <pre>{@code
 * public class MyGame extends SceneGame {
 *   public final Timer timer = new Timer();
 *   public MyGame (Platform plat) {
 *     plat.frame.connect(timer);
 *   }
 * }
 * }</pre>
 *
 * Then you can register actions to be performed at times in the future like so:
 *
 * <pre>{@code
 *   // wherever
 *   game.timer.after(500, () -> {
 *      // this is run after 500ms
 *   });
 * }</pre>
 */
public class Timer implements Slot<Platform>
{
    /** A handle on registered actions that can be used to cancel them. */
    public static interface Handle {
        /** Cancels the action in question. */
        void cancel ();
    }

    /** Creates a timer instance that can be used to schedule actions. Connect this timer to the
      * frame signal to make it operable. */
    public Timer () {
        this(System.currentTimeMillis());
    }

    /** Executes the supplied action after the specified number of milliseconds have elapsed.
      * @return a handle that can be used to cancel the execution of the action. */
    public Handle after (int millis, Runnable action) {
        return add(millis, 0, action);
    }

    /** Executes the supplied action starting {@code millis} from now and every {@code millis}
      * thereafter.
      * @return a handle that can be used to cancel the execution of the action. */
    public Handle every (int millis, Runnable action) {
        return atThenEvery(millis, millis, action);
    }

    /** Executes the supplied action starting {@code initialMillis} from now and every {@code
      * repeatMillis} there after.
      * @return a handle that can be used to cancel the execution of the action. */
    public Handle atThenEvery (int initialMillis, int repeatMillis, Runnable action) {
        return add(initialMillis, repeatMillis, action);
    }

    @Override public void onEmit (Platform plat) {
        update(plat, System.currentTimeMillis());
    }

    // this and update exist so that we can unit test this class
    protected Timer (long now) {
        _currentTime = now;
    }

    protected void update (Platform plat, long now) {
        _currentTime = now;

        Action root = _root;
        while (root.next != null && root.next.nextExpire <= now) {
            Action act = root.next;
            if (!act.cancelled()) {
                execute(plat, act.action);
                if (act.repeatMillis == 0) {
                    act.cancel();
                } else if (!act.cancelled()) {
                    act.nextExpire += act.repeatMillis;
                    root.next = insert(act, act.next);
                }
            }
        }
    }

    protected void execute (Platform plat, Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            plat.log().warn("Action failed", e);
        }
    }

    protected Handle add (int initialMillis, int repeatMillis, Runnable action) {
        Action act = new Action(initialMillis, repeatMillis, action);
        _root.next = insert(act, _root.next);
        return act;
    }

    protected Action insert (Action target, Action tail) {
        if (tail == null || tail.nextExpire > target.nextExpire) {
            target.next = tail;
            return target;
        } else {
            tail.next = insert(target, tail.next);
            return tail;
        }
    }

    protected Action remove (Action target, Action tail) {
        if (target == tail) return tail.next;
        else if (tail == null) return null;
        else {
            tail.next = remove(target, tail.next);
            return tail;
        }
    }

    protected class Action implements Handle {
        public final int repeatMillis;
        public final Runnable action;

        public long nextExpire;
        public Action next;

        public Action (int initialMillis, int repeatMillis, Runnable action) {
            this.nextExpire = _currentTime + initialMillis;
            this.repeatMillis = repeatMillis;
            this.action = action;
        }

        public boolean cancelled () {
            return nextExpire == -1;
        }

        @Override public void cancel () {
            if (!cancelled()) {
                _root.next = remove(this, _root.next);
                nextExpire = -1;
                next = null;
            }
        }

        @Override public String toString () {
            return nextExpire + "/" + repeatMillis + "/" + action + " -> " + next;
        }
    }

    protected final Action _root = new Action(0, 0, null);
    protected long _currentTime;
}
