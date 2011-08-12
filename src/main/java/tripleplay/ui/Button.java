//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.Image;

import pythagoras.f.Dimension;
import pythagoras.f.IRectangle;

import react.Signal;

/**
 * A button that displays text, or an icon, or both.
 */
public class Button extends TextWidget
{
    /** A signal that is emitted when this button is clicked. */
    public final Signal<Button> click = Signal.create();

    /**
     * Creates a button with no custom styles.
     */
    public Button () {
    }

    /**
     * Creates a button with the specified custom styles.
     */
    public Button (Styles styles) {
        setStyles(styles);
    }

    /**
     * Returns the currently configured text, or null if the button does not use text.
     */
    @Override public String text () {
        return _text;
    }

    /**
     * Sets the text of this button to the supplied value.
     */
    @Override public Button setText (String text) {
        super.setText(text);
        return this;
    }

    @Override public Button setIcon (Image icon) {
        super.setIcon(icon);
        return this;
    }

    @Override public Button setIcon (Image icon, IRectangle region) {
        super.setIcon(icon, region);
        return this;
    }

    @Override public String toString () {
        return "Button(" + _text + ")";
    }

    @Override protected void wasRemoved () {
        super.wasRemoved();
        // clear out our background instance
        if (_bginst != null) {
            _bginst.destroy();
            _bginst = null;
        }
        // if we're added again, we'll be re-laid-out
    }

    @Override protected Dimension computeSize (float hintX, float hintY) {
        LayoutData ldata = computeLayout(hintX, hintY);
        Dimension size = computeTextSize(ldata, new Dimension());
        return ldata.bg.addInsets(size);
    }

    @Override protected void layout () {
        float width = _size.width, height = _size.height;
        LayoutData ldata = computeLayout(width, height);

        // prepare our background
        Background bg = ldata.bg;
        if (_bginst != null) _bginst.destroy();
        if (_size.width > 0 && _size.height > 0) {
            _bginst = bg.instantiate(_size);
            _bginst.addTo(layer);
        }
        width -= bg.width();
        height -= bg.height();

        // prepare our label and icon
        renderLayout(ldata, bg.left, bg.top, width, height);

        clearLayoutData(); // we no longer need our layout data
    }

    @Override protected void onPointerStart (float x, float y) {
        super.onPointerStart(x, y);
        set(Flag.DOWN, true);
        invalidate();
    }

    @Override protected void onPointerDrag (float x, float y) {
        super.onPointerDrag(x, y);
        boolean down = contains(x, y);
        if (down != isSet(Flag.DOWN)) {
            set(Flag.DOWN, down);
            invalidate();
        }
    }

    @Override protected void onPointerEnd (float x, float y) {
        super.onPointerEnd(x, y);
        // we don't check whether the supplied coordinates are in our bounds or not because only
        // the drag changes result in changes to the button's visualization, and we want to behave
        // based on what the user sees
        if (isSet(Flag.DOWN)) {
            set(Flag.DOWN, false);
            invalidate();
            click.emit(this); // emit a click event
        }
    }

    @Override protected State state () {
        State sstate = super.state();
        switch (sstate) {
        case DEFAULT: return isSet(Flag.DOWN) ? State.DOWN : State.DEFAULT;
        default:      return sstate;
        }
    }

    @Override protected void clearLayoutData () {
        _ldata = null;
    }

    protected LayoutData computeLayout (float hintX, float hintY) {
        if (_ldata != null) return _ldata;
        _ldata = new LayoutData();

        // determine our background
        Background bg = resolveStyle(state(), Style.BACKGROUND);
        hintX -= bg.width();
        hintY -= bg.height();
        _ldata.bg = bg;

        // layout our text
        layoutText(_ldata, _text, hintX, hintY);

        return _ldata;
    }

    protected static class LayoutData extends TextWidget.LayoutData {
        public Background bg;
    }

    protected Background.Instance _bginst;
    protected LayoutData _ldata;
}
