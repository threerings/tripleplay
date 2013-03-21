//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.List;

import playn.core.Game.Default;
import playn.core.GroupLayer;
import playn.core.util.Clock;

import tripleplay.anim.Animator;
import tripleplay.ui.Element.Flag;
import tripleplay.util.Paintable;

/**
 * The main class that integrates the Triple Play UI with a PlayN game. This class is mainly
 * necessary to automatically validate hierarchies of {@code Element}s during each paint.
 * Create an interface instance, create {@link Root} groups via the interface and add the
 * {@link Root#layer}s into your scene graph wherever you desire.
 *
 * <p> Call {@link #update} and {@link #paint} from {@link Default#update} and
 * {@link Default#paint} to drive your interface. </p>
 */
public class Interface
    implements Paintable
{
    /**
     * A time based task that requires an update per frame. See {@link Interface#addTask}.
     */
    public interface Task {
        /** Performs the update for this task.
         * @param delta time that has passed (in ms), normally passed down from
         * {@link Default#update}. */
        void update (int delta);
    }

    /**
     * An object that can be used to remove a previously added task.
     */
    public interface TaskHandle {
        /** Removes the task associated with this handle. */
        void remove ();
    }

    /**
     * Posts a runnable that will be executed after the next time the interface is validated.
     * Processing deferred actions is not tremendously efficient, so don't call this every frame.
     */
    public void deferAction (Runnable action) {
        _actions.add(action);
    }

    /**
     * Adds a task that will henceforth be updated once per game update. If a task is added during
     * the task update iteration, it will be updated for the first time on the following game
     * update.
     * @return a handle that will remove the task when invoked the first time. Subsequent
     * invocations will do nothing.
     */
    public TaskHandle addTask (final Task task) {
        _tasks.add(task);
        return new TaskHandle() {
            Task target = task;
            public void remove () {
                if (target == null) return;
                int idx = _tasks.indexOf(target);
                if (idx == -1) return;
                _tasks.remove(idx);
                // adjust iteration members
                if (_currentTask >= idx) _currentTask--;
                _currentTaskCount--;
                // clear state so we don't try and remove again
                target = null;
            }
        };
    }

    /**
     * Updates the elements in this interface. Normally called from {@link Default#update}.
     */
    public void update (int delta) {
        // use members for task iteration to support concurrent modification
        for (_currentTask = 0, _currentTaskCount = _tasks.size();
             _currentTask < _currentTaskCount; _currentTask++) {
            Task task = _tasks.get(_currentTask);
            try {
                task.update(delta);
            } catch (Exception e) {
                Log.log.warning("Interface task failed: " + task, e);
            }
        }
        _currentTask = -1;
    }

    /**
     * "Paints" the elements in this interface. Normally called from {@link Default#paint}.
     */
    public void paint (Clock clock) {
        // update the animator
        _animator.paint(clock);

        // ensure that our roots are validated
        for (int ii = 0, ll = _roots.size(); ii < ll; ii++) {
            _roots.get(ii).validate();
        }

        // run any deferred actions
        if (!_actions.isEmpty()) {
            List<Runnable> actions = new ArrayList<Runnable>(_actions);
            _actions.clear();
            for (Runnable action : actions) {
                try {
                    action.run();
                } catch (Exception e) {
                    Log.log.warning("Interface action failed: " + action, e);
                }
            }
        }
    }

    /**
     * Returns an iterable over the current roots. Don't delete from this iterable!
     */
    public Iterable<Root> roots () {
        return _roots;
    }

    /**
     * Returns an animator that can be used within the scope of this interface.
     */
    public Animator animator () {
        return _animator;
    }

    /**
     * Creates a root element with the specified layout and stylesheet.
     */
    public Root createRoot (Layout layout, Stylesheet sheet) {
        return addRoot(new Root(this, layout, sheet));
    }

    /**
     * Creates a root element with the specified layout and stylesheet and adds its layer to the
     * specified parent.
     */
    public Root createRoot (Layout layout, Stylesheet sheet, GroupLayer parent) {
        Root root = createRoot(layout, sheet);
        parent.add(root.layer);
        return root;
    }

    /**
     * Adds a root to this interface. The root must have been created with this interface and not
     * be added to any other interfaces. Generally you should use {@link #createRoot}, but this
     * method is exposed for callers with special needs.
     */
    public Root addRoot (Root root) {
        _roots.add(root);
        return root;
    }

    /**
     * Removes the supplied root element from this interface. If the root's layer has a parent, the
     * layer will be removed from the parent as well. This leaves the Root's layer in existence, so
     * it may be used again. If you're done with the Root and all of the elements inside of it, call
     * destroyRoot to free its resources.
     */
    public void removeRoot (Root root) {
        _roots.remove(root);
        root.wasRemoved();
        if (root.layer.parent() != null) root.layer.parent().remove(root.layer);
    }

    /**
     * Removes the supplied root element from this interface and destroys its layer. Destroying the
     * layer destroys the layers of all elements contained in the root as well. Use this method if
     * you're done with the Root. If you'd like to reuse it, call removeRoot instead.
     */
    public void destroyRoot (Root root) {
        _roots.remove(root);
        root.set(Flag.WILL_DESTROY, true);
        root.wasRemoved();
        root.layer.destroy();
    }

    protected final List<Root> _roots = new ArrayList<Root>();
    protected final List<Runnable> _actions = new ArrayList<Runnable>();
    protected final Animator _animator = new Animator();
    protected final List<Task> _tasks = new ArrayList<Task>();
    protected int _currentTask, _currentTaskCount;
}
