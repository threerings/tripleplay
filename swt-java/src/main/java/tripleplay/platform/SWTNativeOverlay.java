//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
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
    /** SWT widget that is being overlaid. */
    public Control ctrl;

    /**
     * Creates the control, from scratch.
     */
    abstract protected Control createControl (Composite parent);

    @Override public void setBounds (IRectangle bounds) {
        this.bounds.setBounds(bounds);
        if (ctrl != null) updateBounds();
    }

    @Override public void add () {
        if (ctrl != null) return;
        ctrl = createControl(SWTTPPlatform.instance().overlayParent());
        updateBounds();
        ctrl.moveAbove(null);
        didCreate();
        SWTTPPlatform.instance().addOverlay(this);
    }

    @Override public void remove () {
        if (ctrl == null) return;
        SWTTPPlatform.instance().removeOverlay(this);
        willDispose();
        ctrl.dispose();
        ctrl = null;
    }

    protected void updateBounds () {
        Log.log.info("Updating bounds", "val", bounds);
        ctrl.setBounds(
            (int)bounds.x(), (int)bounds.y(), (int)bounds.width(), (int)bounds.height());
    }

    /** Caleld after the SWT {@code Control} has been created. */
    protected void didCreate () {}

    /** Called just before the SWT {@code Control} is disposed. */
    protected void willDispose () {}

    protected final Rectangle bounds = new Rectangle();
}
