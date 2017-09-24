//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import pythagoras.f.Dimension;

import react.SignalView;
import react.Slot;
import react.UnitSlot;

import playn.core.TileSource;
import playn.scene.ImageLayer;

/**
 * A button that uses images for its different states.
 */
public class ImageButton extends Widget<ImageButton> implements Clickable<ImageButton> {

    /** Creates a button with the supplied image for use in up and down states. */
    public ImageButton (TileSource up) {
        this(up, up);
    }

    /** Creates a button with the supplied up and down images. */
    public ImageButton (TileSource up, TileSource down) {
        layer.add(_ilayer);
        setUp(up);
        setDown(down);
    }

    /** Configures the image used in our up state. */
    public ImageButton setUp (TileSource up) {
        _up = up;
        _up.tileAsync().onSuccess(new UnitSlot() {
            public void onEmit () { invalidate(); }
        });
        return this;
    }

    /** Configures the image used in our down state. */
    public ImageButton setDown (TileSource down) {
        _down = down;
        _down.tileAsync().onSuccess(new UnitSlot() {
            public void onEmit () { invalidate(); }
        });
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
    public ImageButton onClick (SignalView.Listener<? super ImageButton> onClick) {
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

    @Override protected Dimension computeSize (LayoutData ldata, float hintX, float hintY) {
        return _up.isLoaded() ?
            new Dimension(_up.tile().width(), _up.tile().height()) :
            new Dimension();
    }

    @Override protected void layout (LayoutData ldata, float left, float top,
                                     float width, float height) {
        _ilayer.setSource(isSelected() ? _down : _up);
        _ilayer.setTranslation(left, top);
    }

    protected final ImageLayer _ilayer = new ImageLayer();
    protected TileSource _up, _down;
}
