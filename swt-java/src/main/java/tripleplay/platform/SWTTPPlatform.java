//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.util.Set;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import playn.java.JavaPlatform;
import playn.java.SWTPlatform;
import tripleplay.ui.Field;

import com.google.common.collect.Sets;

import static tripleplay.platform.Log.log;

/**
 * Implements Java-specific TriplePlay services.
 * TODO: reconcile the main thread use with AWT EDT - there are a bunch of places where we
 * need to synchronize
 */
public class SWTTPPlatform extends TPPlatform
{
    /** Possible operating systems. */
    public enum OS { MAC, WINDOWS, LINUX, UNKNOWN }

    /** Registers the Java TriplePlay platform. */
    public static SWTTPPlatform register (SWTPlatform platform, JavaPlatform.Config config) {
        SWTTPPlatform instance = new SWTTPPlatform(platform, config);
        TPPlatform.register(instance);
        return instance;
    }

    public static SWTTPPlatform instance () {
        return (SWTTPPlatform)TPPlatform.instance();
    }

    public void addOverlay (SWTNativeOverlay overlay) {
        _overlays.add(overlay);
    }

    public void removeOverlay (SWTNativeOverlay overlay) {
        _overlays.remove(overlay);
    }

    protected SWTTPPlatform (SWTPlatform platform, JavaPlatform.Config config) {
        _platform = platform;

        // Figure out the os
        String osname = System.getProperty("os.name");
        osname = (osname == null) ? "" : osname;
        if (osname.indexOf("Windows") != -1) _os = OS.WINDOWS;
        else if (osname.indexOf("Mac OS") != -1 || osname.indexOf("MacOS") != -1) _os = OS.MAC;
        else if (osname.indexOf("Linux") != -1) _os = OS.LINUX;
        else System.err.println("Unmatching os name: " + osname);
    }

    @Override public boolean hasNativeTextFields () {
        // mac doesn't currently support native text :(
        return _os == OS.WINDOWS || _os == OS.LINUX;
    }

    @Override public NativeTextField createNativeTextField (Field.Native field) {
        return new SWTNativeTextField(field);
    }

    @Override public NativeTextField refresh (NativeTextField previous) {
        ((SWTNativeTextField)previous).refresh();
        return previous;
    }

    @Override public void clearFocus () {
        // TODO
    }

    public SWTConvert convert () {
        if (_convert == null) {
            _convert = new SWTConvert();
        }
        // TODO: is display lifetime suitable to avoid this?
        _convert.display = shell().getDisplay();
        return _convert;
    }

    /**
     * Gets the OS this JVM is running on.
     */
    public OS os () {
        return _os;
    }

    public Shell shell () {
        return _platform.shell();
    }

    public Composite shellContent () {
        return _platform.composite();
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
        shell().setImage(convert().image(icons[0]));
            
    }

    /** The SWT platform with which this TPPlatform was registered. */
    protected SWTPlatform _platform;
    protected SWTConvert _convert;

    protected OS _os = OS.UNKNOWN;

    protected Set<SWTNativeOverlay> _overlays = Sets.newHashSet();
}
