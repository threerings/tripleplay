//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.gesture;

import react.SignalView;

public interface Gesture<T extends Gesture<T>>
{
    public enum Direction { UP, DOWN, LEFT, RIGHT }

    public enum State {
        /**
         * For a gesture to be passive, the user interaction thus far has qualified toward
         * completing this gesture, but has not yet met a minimum requirement, or the gesture is not
         * greedy by nature.
         */
        PASSIVE,

        /**
         * A greedy gesture has had its minimum requirements met and is therefore considered to be
         * actively being performed by the user. There should only be one greedy gesture active at
         * a time, and if there is an active gesture in the greedy state, any passive gestures that
         * were being considered will no longer receive events.
         */
        GREEDY,

        /**
         * A complete gesture has successfully completed, indicating that no further gesture
         * evaluation should be performed for the remainder of this user interaction.
         */
        COMPLETE,

        /**
         * The current user action does not trigger this gesture, or a greedy gesture has superceded
         * this gesture's evaluation. Once a gesture enters UNQUALIFIED, it is assumed that it will
         * stay there until a call to start() puts it back in PASSIVE.
         */
        UNQUALIFIED
    }

    /**
     * Called at the start of a new user interaction, forcibly puts this gesture into the PASSIVE
     * state.
     */
    void start ();

    /**
     * Forcibly put this gesture into the UNQUALIFIED state, as a result of another gesture
     * transitioning to GREEDY.
     */
    void cancel ();

    /**
     * Perform a potential state transition in this Gesture based on the most recent touch
     * information.
     */
    void evaluate (GestureNode node);

    /**
     * Return the current gesture state.
     */
    State state ();

    /**
     * Sets whether this gesture should be greedy or not.
     */
    T greedy (boolean value);

    /**
     * Returns whether this gesture will be greedy.
     */
    boolean greedy ();

    /**
     * Dispatched if this gesture goes GREEDY.
     */
    SignalView<Void> started ();

    /**
     * Dispatched when this gesture is completed. The value is only false if the gesture was
     * GREEDY and it was canceled instead of completed.
     */
    SignalView<Boolean> completed ();
}
