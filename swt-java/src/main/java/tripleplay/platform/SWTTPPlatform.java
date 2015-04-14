//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import pythagoras.f.Point;
import react.Slot;

import playn.core.Platform;
import playn.java.JavaPlatform;
import playn.java.SWTPlatform;

import tripleplay.ui.Field;

import static tripleplay.platform.Log.log;

/**
 * Implements SWT-specific TriplePlay services. Please note this does not follow the pattern of
 * inheriting from JavaTPPlatform, because that can die in a fire.
 */
public class SWTTPPlatform extends TPPlatform
{
    /** Possible operating systems. */
    public enum OS { MAC, WINDOWS, LINUX, UNKNOWN }

    public SWTTPPlatform (SWTPlatform platform, JavaPlatform.Config config) {
        _platform = platform;
        _overlay = _platform.composite();
        _convert = new SWTConvert(display());

        // Figure out the os
        String osname = System.getProperty("os.name");
        osname = (osname == null) ? "" : osname;
        if (osname.indexOf("Windows") != -1) _os = OS.WINDOWS;
        else if (osname.indexOf("Mac OS") != -1 || osname.indexOf("MacOS") != -1) _os = OS.MAC;
        else if (osname.indexOf("Linux") != -1) _os = OS.LINUX;
        else System.err.println("Unmatching os name: " + osname);

        if (_os == OS.MAC) {
            // TODO: figure out why this hack allows native text fields to work on Mac
            // In my investigation so far, the hack has to be added on startup, moveAbove must be
            // called and the bounds must overlap with the canvas'.
            Composite hack = new Composite(_overlay, SWT.NONE);
            hack.moveAbove(null);

            // make the hack small and black; TODO: expose color to apps if this hack is permanent
            hack.setBounds(0, 0, 1, 1);
            hack.setBackground(new Color(display(), 0, 0, 0));
        }

        display().addFilter(SWT.MouseDown, new Listener() {
            @Override public void handleEvent (Event event) {
                if (event.widget == _platform.graphics().canvas() &&
                    (_kfc == null || _kfc.unfocusForLocation(new Point(event.x, event.y)))) {
                    clearFocus();
                }
            }
        });
    }

    public void addOverlay (SWTNativeOverlay overlay) {
        _overlays.add(overlay);
    }

    public void removeOverlay (SWTNativeOverlay overlay) {
        _overlays.remove(overlay);
    }

    @Override public boolean hasNativeTextFields () {
        return true;
    }

    @Override public NativeTextField createNativeTextField (Field.Native field) {
        return new SWTNativeTextField(this, field);
    }

    @Override public NativeTextField refresh (NativeTextField previous) {
        ((SWTNativeTextField)previous).refresh();
        return previous;
    }

    @Override public void clearFocus () {
        _platform.graphics().canvas().setFocus();
    }

    @Override public void refreshNativeBounds () {
        if (_pendingRefresh == null) _pendingRefresh = new PendingRefresh();
        _pendingRefresh.request();
    }

    public Display display () {
        return _platform.shell().getDisplay();
    }

    public SWTConvert convert () {
        return _convert;
    }

    /**
     * Gets the OS this JVM is running on.
     */
    public OS os () {
        return _os;
    }

    public Composite overlayParent () {
        return _overlay;
    }

    @Override protected void updateHidden () {
        for (SWTNativeOverlay overlay : _overlays) {
            overlay.ctrl.setVisible(_hidden == null);
        }
    }

    /**
     * Processes a change to the currently focused control.
     */
    public void onFocusChange () {
        // deal with this on the next frame, avoiding platform-specific issues
        _platform.invokeLater(new Runnable() {
            @Override public void run () {
                Control focus = display().getFocusControl();

                // ignore focusing on null, this seems to happen on window deactivation
                if (focus == null) return;

                // find a native text field corresponding to the new focus
                for (SWTNativeOverlay overlay : _overlays) {
                    if (overlay instanceof SWTNativeTextField && overlay.ctrl == focus) {
                        _focus.update(((SWTNativeTextField)overlay).field());
                        return;
                    }
                }

                // any other focus for our purposes is a loss of focus
                _focus.update(null);
            }
        });
    }

    /**
     * Sets the window icon.
     *
     * Takes icons of different sizes, preferring earlier ones in case of duplicate sizes.
     */
    public void setIcon (playn.core.Image... icons) {
        if (icons.length == 0) {
            log.warning("Ignoring empty icon list");
            return;
        }
        if (icons.length > 1) {
            log.info("Ignoring additional icons");
        }
        _platform.shell().setImage(convert().image(icons[0]));
    }

    protected class PendingRefresh {
        public PendingRefresh () {
            _platform.invokeLater(_buddy);
        }
        protected void request () {
            _req = _platform.tick();
        }
        protected Runnable _buddy = new Runnable() {
            @Override public void run () {
                long now = _platform.tick();
                if (now - _req < 75) _platform.invokeLater(this);
                else {
                    _pendingRefresh = null;
                    for (SWTNativeOverlay overlay : _overlays) overlay.refreshBounds();
                }
            }
        };
        protected int _req;
    }

    /** The SWT platform with which this TPPlatform was registered. */
    protected SWTPlatform _platform;
    protected SWTConvert _convert;
    protected Composite _overlay;
    protected PendingRefresh _pendingRefresh;
    protected OS _os = OS.UNKNOWN;

    protected Set<SWTNativeOverlay> _overlays = new HashSet<SWTNativeOverlay>();
}
