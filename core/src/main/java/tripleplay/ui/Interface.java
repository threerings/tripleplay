//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.List;

import playn.core.Game;
import playn.core.GroupLayer;
import react.Slot;
import tripleplay.anim.Animator;
import tripleplay.ui.Element.Flag;

/**
 * The main class that integrates the Triple Play UI with a PlayN game. This class is mainly
 * necessary to automatically validate hierarchies of {@code Element}s during each paint.
 * Create an interface instance, create {@link Root} groups via the interface and add the
 * {@link Root#layer}s into your scene graph wherever you desire. Call {@link #update} and 
 * {@link #paint} from {@link Game#update} and {@link Game#paint} to render your interface.
 */
public class Interface
{
    /**
     * A time based task that requires an update per frame.
     * @see Interface#addTask(Task)
     */
    public interface Task {
        /**
         * Performs the update for this task.
         * @param delta time that has passed, normally passed down from
         * {@link playn.core.Game#update(float)}
         */
        void update (float delta);
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
     */
    public void addTask (Task task) {
        _tasks.add(task);
    }

    /**
     * Removes a previously added task. If a task is removed during an update and has not yet
     * been updated itself, then it will not be updated.
     */
    public void removeTask (Task task) {
        int idx = _tasks.indexOf(task);
        if (idx == -1) return;
        _tasks.remove(idx);
        // adjust iteration members
        if (_currentTask >= idx) _currentTask--;
        _currentTaskCount--;
    }

    /**
     * Ensures that the given task is always added to the interface's tasks when the given element
     * is added to a root.
     * @return the element for chaining
     * @see Element#hierarchyChanged()
     */
    public <T extends Element<?>> T updateWhenAdded (final T elem, final Task task) {
        Slot<Boolean> hierChange = new Slot<Boolean>() {
            @Override public void onEmit (Boolean added) {
                if (added) addTask(task);
                else removeTask(task);
            }
        };
        elem.hierarchyChanged().connect(hierChange);
        if (elem.isAdded()) hierChange.onEmit(true);
        return elem;
    }

    /**
     * Single parameter version of {@link #updateWhenAdded(Element, Task)} for use when the
     * {@code Element} also happens to be a task.
     * @param elem the Element and Task
     * @return the element for chaining
     */
    public <T extends Element<?> & Task> T updateWhenAdded (final T elem) {
        return updateWhenAdded(elem, elem);
    }

    /**
     * Updates the elements in this interface. Must be called from {@link Game#update}.
     */
    public void update (float delta) {
        // use members for task iteration to support concurrent modification
        for (_currentTask = 0, _currentTaskCount = _tasks.size(); _currentTask < _currentTaskCount;
                _currentTask++) _tasks.get(_currentTask).update(delta);
        _currentTask = -1;
        // update animator
        _elapsed += delta;
        _animator.update(_elapsed);
    }

    /**
     * "Paints" the elements in this interface. Must be called from {@link Game#paint}.
     */
    public void paint (float alpha) {
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
        return addRoot(new Root(layout, sheet));
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
    protected final List<Root> _dispatch = new ArrayList<Root>();
    protected final List<Runnable> _actions = new ArrayList<Runnable>();
    protected final Animator _animator = Animator.create();
    protected final List<Task> _tasks = new ArrayList<Task>();
    protected float _elapsed;
    protected int _currentTask, _currentTaskCount;
}
