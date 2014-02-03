//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.ValueView;
import react.Value;

/**
 * An abstract base class for buttons with text labels.
 */
public abstract class AbstractTextButton<T extends AbstractTextButton<T>> extends TextWidget<T>
{
    /** The text displayed by this button, or null. */
    public final Value<String> text = Value.create((String)null);

    /** The icon displayed by this button, or null. */
    public final Value<Icon> icon = Value.<Icon>create(null);

    /**
     * Binds the text of this button to the supplied reactive value. The current text will be
     * adjusted to match the state of {@code text}.
     */
    public T bindText (ValueView<String> text) {
        text.connectNotify(this.text.slot());
        return asT();
    }

    /**
     * Binds the icon of this button to the supplied reactive value. The current icon will be
     * adjusted to match the state of {@code icon}.
     */
    public T bindIcon (ValueView<Icon> icon) {
        icon.connectNotify(this.icon.slot());
        return asT();
    }

    protected AbstractTextButton (String text, Icon icon) {
        this.text.update(text);
        this.text.connect(textDidChange());
        // update after connect so we trigger iconDidChange, in case our icon is a not-ready-image
        this.icon.connect(iconDidChange());
        this.icon.update(icon);
    }

    @Override protected String text () {
        return text.get();
    }

    @Override protected Icon icon () {
        return icon.get();
    }
}
