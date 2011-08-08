//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.game;

import java.util.ArrayList;
import java.util.List;

import playn.core.PlayN;
import playn.core.Game;

/**
 * Manages a stack of screens. The stack supports useful manipulations: pushing a new screen onto
 * the stack, replacing the screen at the top of the stack with a new screen, popping a screen from
 * the stack.
 *
 * <p> Care is taken to preserve stack invariants even in the face of errors thrown by screens when
 * being added, removed, shown or hidden. Users can override {@link #handleError} and either simply
 * log the error, or rethrow it if they would prefer that a screen failure render their entire
 * screen stack unusable. </p>
 */
public abstract class ScreenStack
{
    /**
     * Pushes the supplied screen onto the stack, making it the visible screen. The currently
     * visible screen will be hidden.
     * @throws IllegalArgumentException if the supplied screen is already in the stack.
     */
    public void push (Screen screen) {
        if (_screens.contains(screen)) {
            throw new IllegalArgumentException("Cannot add screen to stack twice.");
        }
        if (!_screens.isEmpty()) hide(top());
        add(screen);
    }

    /**
     * Pops the current screen from the top of the stack and pushes the supplied screen on as its
     * replacement.
     * @throws IllegalArgumentException if the supplied screen is already in the stack.
     */
    public void replace (Screen screen) {
        if (_screens.contains(screen)) {
            throw new IllegalArgumentException("Cannot add screen to stack twice.");
        }
        if (!_screens.isEmpty()) removeTop();
        add(screen);
    }

    /**
     * Removes the specified screen from the stack. If it is the currently visible screen, it will
     * first be hidden, and the next screen below in the stack will be made visible.
     */
    public boolean remove (Screen screen) {
        if (top() == screen) {
            removeTop();
            show(top());
            return true;

        } else {
            boolean removed = _screens.remove(screen);
            if (removed) {
                try { screen.wasRemoved(); }
                catch (RuntimeException e) { handleError(e); }
            }
            return removed;
        }
    }

    /**
     * Updates the currently visible screen. A screen stack client should call this method from
     * {@link Game#update}.
     */
    public void update (float delta) {
        top().update(delta);
    }

    /**
     * Paints the currently visible screen. A screen stack client should call this method from
     * {@link Game#paint}.
     */
    public void paint (float alpha) {
        top().paint(alpha);
    }

    protected Screen top () {
        return _screens.get(0);
    }

    protected void add (Screen screen) {
        _screens.add(0, screen);
        try { screen.wasAdded(); }
        catch (RuntimeException e) { handleError(e); }
        show(screen);
    }

    protected void show (Screen screen) {
        PlayN.graphics().rootLayer().add(screen.layer);
        try { screen.wasShown(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected void hide (Screen screen) {
        PlayN.graphics().rootLayer().remove(screen.layer);
        try { screen.wasHidden(); }
        catch (RuntimeException e) { handleError(e); }
    }

    protected void removeTop () {
        hide(top());
        Screen screen = _screens.remove(0);
        try { screen.wasRemoved(); }
        catch (RuntimeException e) { handleError(e); }
    }

    /** Called if any exceptions are thrown by the screen callback functions. */
    protected abstract void handleError (RuntimeException error);

    /** Containts the stacked screens from top-most, to bottom-most. */
    protected final List<Screen> _screens = new ArrayList<Screen>();
}
