//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.Slot;
import react.Value;

import playn.core.Image;
import playn.core.Sound;

/**
 * Displays a checkbox which can be toggled. The checkbox must be configured with either a
 * font-based checkmark, or a checkmark icon, which will be shown when it is checked.
 */
public class CheckBox extends ClickableTextWidget<CheckBox>
{
    /** The checked status of this widget. */
    public final Value<Boolean> checked = Value.create(false);

    /** Creates a checkbox using the default check glyph: U+2713. */
    public CheckBox () {
        this('\u2713');
    }

    /** Creates a checkbox with the supplied check character. */
    public CheckBox (char checkChar) {
        this(checkChar, null);
    }

    public CheckBox (Image checkIcon) {
        this((char)0, checkIcon);
    }

    protected CheckBox (char checkChar, Image checkIcon) {
        _checkStr = String.valueOf(checkChar);
        _checkIcon = checkIcon;
        checked.connect(new Slot<Boolean> () {
            @Override public void onEmit (Boolean checked) {
                updateCheckViz();
            }
        });
    }

    @Override protected Class<?> getStyleClass ()
    {
        return CheckBox.class;
    }

    @Override protected String text () {
        return (_checkIcon == null) ? _checkStr : null;
    }

    @Override protected Image icon () {
        return _checkIcon;
    }

    @Override protected void onClick () {
        if (_actionSound != null) _actionSound.play();
        checked.update(!checked.get());
    }

    @Override protected void layout () {
        super.layout();
        _actionSound = resolveStyle(Style.ACTION_SOUND);
        updateCheckViz();
    }

    protected void updateCheckViz () {
        boolean isChecked = checked.get();
        if (_tglyph.layer() != null) _tglyph.layer().setVisible(isChecked);
        if (_ilayer != null) _ilayer.setVisible(isChecked);
    }

    protected final String _checkStr;
    protected final Image _checkIcon;
    protected Sound _actionSound;
}
