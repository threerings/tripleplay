//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import pythagoras.f.IRectangle;
import pythagoras.f.Rectangle;

/**
 * Basic machinery for managing a SWT native child view.
 */
public abstract class SWTNativeOverlay implements NativeOverlay
{
    /** SWT widget that is overlaid on the canvas. */
    public Control ctrl;

    public SWTNativeOverlay (SWTTPPlatform plat) {
        _plat = plat;
    }

    @Override public void setBounds (IRectangle bounds) {
        _bounds.setBounds(bounds);
        if (ctrl != null) updateBounds();
    }

    @Override public void add () {
        if (ctrl != null) return;
        ctrl = createControl(_plat.overlayParent());
        updateBounds();
        ctrl.moveAbove(null);
        didCreate();
        _plat.addOverlay(this);
    }

    @Override public void remove () {
        if (ctrl == null) return;
        _plat.removeOverlay(this);
        willDispose();
        ctrl.dispose();
        ctrl = null;
    }

    /**
     * Creates the control, from scratch.
     */
    abstract protected Control createControl (Composite parent);

    abstract protected void refreshBounds ();

    protected void updateBounds () {
        ctrl.setBounds((int)_bounds.x(), (int)_bounds.y(),
                       (int)_bounds.width(), (int)_bounds.height());
    }

    /** Caleld after the SWT {@code Control} has been created. */
    protected void didCreate () {}

    /** Called just before the SWT {@code Control} is disposed. */
    protected void willDispose () {}

    protected SWTConvert convert () {
        return _plat.convert();
    }

    protected final SWTTPPlatform _plat;
    protected final Rectangle _bounds = new Rectangle();
}
