//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Pointer;

/**
 * The base class for all user interface widgets. Provides helper methods for managing a canvas
 * into which a widget is rendered when its state changes.
 */
public abstract class Widget<T extends Widget<T>> extends Element<T>
{
    /**
     * Widgets that are interactive can call this method to wire up the appropriate listeners.
     */
    protected void enableInteraction () {
        // an interactive widget absorbs clicks and does not descend (propagate them to sublayers)
        set(Flag.HIT_DESCEND, false);
        set(Flag.HIT_ABSORB, true);

        // add a pointer listener for handling input events
        layer.addListener(new Pointer.Listener() {
            public void onPointerStart (Pointer.Event event) {
                Widget.this.onPointerStart(event, event.localX(), event.localY());
            }
            public void onPointerDrag (Pointer.Event event) {
                Widget.this.onPointerDrag(event, event.localX(), event.localY());
            }
            public void onPointerEnd (Pointer.Event event) {
                Widget.this.onPointerEnd(event, event.localX(), event.localY());
            }
            public void onPointerCancel (Pointer.Event event) {
                Widget.this.onPointerCancel(event);
            }
        });
    }

    /** Called when the pointer is clicked on this widget. */
    protected void onPress (Pointer.Event event) {
        set(Flag.SELECTED, true);
        invalidate();
    }

    /** Called as the user drags the pointer around with the widget depressed. */
    protected void onHover (Pointer.Event event, boolean inBounds) {
        if (inBounds != isSelected()) {
            set(Flag.SELECTED, inBounds);
            invalidate();
        }
    }

    /** Called when the pointer is released after having been pressed on this widget. This should
     * {@link #onClick} if appropriate. */
    protected void onRelease (Pointer.Event event) {
        if (isSelected()) {
            set(Flag.SELECTED, false);
            invalidate();
            onClick(event);
        }
    }

    /** Called when the interaction is canceled after having been pressed on this widget. This
     * should not result in a call to {@link #onClick}. */
    protected void onCancel (Pointer.Event event) {
        if (isSelected()) {
            set(Flag.SELECTED, false);
            invalidate();
        }
    }

    /** Called when the pointer is pressed and released over this widget. */
    protected void onClick (Pointer.Event event) {
        // nada by default
    }

    /**
     * Called when the a touch/drag is started within the bounds of this component.
     *
     * @param event the pointer event that triggered this call.
     * @param x the x-coordinate of the event, translated into this element's coordinates.
     * @param y the y-coordinate of the event, translated into this element's coordinates.
     */
    protected void onPointerStart (Pointer.Event event, float x, float y) {
        if (isEnabled()) onPress(event);
    }

    /**
     * Called when a touch that started within the bounds of this component is dragged. The drag
     * may progress outside the bounds of this component, but the events will still be dispatched
     * to this component until the touch is released.
     *
     * @param event the pointer event that triggered this call.
     * @param x the x-coordinate of the event, translated into this element's coordinates.
     * @param y the y-coordinate of the event, translated into this element's coordinates.
     */
    protected void onPointerDrag (Pointer.Event event, float x, float y) {
        if (isEnabled()) onHover(event, contains(x, y));
    }

    /**
     * Called when a touch that started within the bounds of this component is released. The
     * coordinates may be outside the bounds of this component, but the touch in question started
     * inside this component's bounds.
     *
     * @param event the pointer event that triggered this call.
     * @param x the x-coordinate of the event, translated into this element's coordinates.
     * @param y the y-coordinate of the event, translated into this element's coordinates.
     */
    protected void onPointerEnd (Pointer.Event event, float x, float y) {
        onRelease(event);
    }

    /**
     * Called when a touch that started within the bounds of this component is canceled. This
     * should generally terminate any active interaction without triggering a click, etc.
     *
     * @param event the pointer event that triggered this call.
     */
    protected void onPointerCancel (Pointer.Event event) {
        onCancel(event);
    }

    /**
     * Extends base Glyph to automatically wire up to this Widget's {@link #layer}.
     */
    protected class Glyph extends tripleplay.util.Glyph {
        public Glyph () {
            super(layer);
        }
    }
}
