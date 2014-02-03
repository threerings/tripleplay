//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;

import playn.core.Image;
import playn.core.ImageLayer;
import static playn.core.PlayN.graphics;

import react.SignalView;
import react.Slot;

/**
 * A button that uses images for its different states.
 */
public class ImageButton extends Widget<ImageButton> implements Clickable<ImageButton> {

    /** Creates a button with the supplied image for use in up and down states. */
    public ImageButton (Image up) {
        this(up, up);
    }

    /** Creates a button with the supplied up and down images. */
    public ImageButton (Image up, Image down) {
        layer.add(_ilayer);
        _up = up;
        _down = down;
    }

    /** Configures the image used in our up state. */
    public ImageButton setUp (Image up) {
        _up = up;
        invalidate();
        return this;
    }

    /** Configures the image used in our down state. */
    public ImageButton setDown (Image down) {
        _down = down;
        invalidate();
        return this;
    }

    /** Programmatically triggers a click of this button. This triggers the action sound, but does
     * not cause any change in the button's visualization. <em>Note:</em> this does not check the
     * button's enabled state, so the caller must handle that if appropriate. */
    public void click () {
        ((Behavior.Click<ImageButton>)_behave).click();
    }

    /** A convenience method for registering a click handler. Assumes you don't need the result of
     * {@link SignalView#connect}, because it throws it away. */
    public ImageButton onClick (Slot<? super ImageButton> onClick) {
        clicked().connect(onClick);
        return this;
    }

    @Override public SignalView<ImageButton> clicked () {
        return ((Behavior.Click<ImageButton>)_behave).clicked;
    }

    @Override public String toString () {
        return "ImageButton(" + size() + ")";
    }

    @Override protected Class<?> getStyleClass () {
        return ImageButton.class;
    }

    @Override protected Behavior<ImageButton> createBehavior () {
        return new Behavior.Click<ImageButton>(this);
    }

    @Override protected LayoutData createLayoutData (float hintX, float hintY) {
        return new ImageButtonLayoutData();
    }

    protected class ImageButtonLayoutData extends LayoutData {
        @Override public Dimension computeSize (float hintX, float hintY) {
            return new Dimension(_up.width(), _up.height());
        }

        @Override public void layout (float left, float top, float width, float height) {
            _ilayer.setImage(isSelected() ? _down : _up);
            _ilayer.setTranslation(left, top);
        }
    }

    protected final ImageLayer _ilayer = graphics().createImageLayer();
    protected Image _up, _down;
}
