//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.Functions;
import react.SignalView;
import react.Slot;
import react.Value;

import playn.scene.Pointer;

/**
 * Displays a checkbox which can be toggled. The checkbox must be configured with either a
 * font-based checkmark, or a checkmark icon, which will be shown when it is checked.
 */
public class CheckBox extends TextWidget<CheckBox> implements Togglable<CheckBox>
{
    /** Creates a checkbox using the default check glyph: U+2713. */
    public CheckBox () {
        this('\u2713');
    }

    /** Creates a checkbox with the supplied check character. */
    public CheckBox (char checkChar) {
        this(checkChar, (Icon)null);
    }

    public CheckBox (Icon checkIcon) {
        this((char)0, checkIcon);
    }

    /**
     * Updates the selected state of this checkbox. This method is called when the user taps and
     * releases the checkbox. One can override this method if they want to react to only
     * user-interaction-initiated changes to the checkbox's state (versus listening to
     * {@link #selected} which can be updated programmatically).
     */
    public void select (boolean selected) {
        selected().update(selected);
    }

    @Override public Value<Boolean> selected () {
        return ((Behavior.Toggle<CheckBox>)_behave).selected;
    }

    @Override public SignalView<CheckBox> clicked () {
        return ((Behavior.Toggle<CheckBox>)_behave).clicked;
    }

    @Override public void click () {
        ((Behavior.Toggle<CheckBox>)_behave).click();
    }

    @Override public String toString () {
        return "CheckBox(" + text() + ")";
    }

    protected CheckBox (char checkChar, Icon checkIcon) {
        _checkStr = String.valueOf(checkChar);
        _checkIcon = checkIcon;
        selected().connect(new Slot<Boolean> () {
            @Override public void onEmit (Boolean checked) {
                updateCheckViz(checked);
            }
        });
    }

    @Override protected Class<?> getStyleClass () {
        return CheckBox.class;
    }

    @Override protected String text () {
        return (_checkIcon == null) ? _checkStr : null;
    }

    @Override protected Icon icon () {
        return _checkIcon;
    }

    @Override protected Behavior<CheckBox> createBehavior () {
        return new Behavior.Toggle<CheckBox>(asT());
    }

    @Override protected void layout () {
        super.layout();
        updateCheckViz(selected().get());
    }

    protected void updateCheckViz (boolean isChecked) {
        if (_tglyph.layer() != null) _tglyph.layer().setVisible(isChecked);
        if (_ilayer != null) _ilayer.setVisible(isChecked);
    }

    protected final String _checkStr;
    protected final Icon _checkIcon;
}
