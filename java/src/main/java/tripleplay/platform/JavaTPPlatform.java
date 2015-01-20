//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.awt.Canvas;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JFrame;

import org.lwjgl.opengl.Display;

import pythagoras.f.Point;
import react.Signal;
import react.Slot;
import react.Value;

import playn.core.Platform;
import playn.java.JavaImage;
import playn.java.JavaPlatform;
import tripleplay.ui.Field;

import static tripleplay.platform.Log.log;

/**
 * Implements Java-specific TriplePlay services.
 *
 * <p>TODO: reconcile the main thread use with AWT EDT - there are a bunch of places where we need
 * to synchronize.
 */
public class JavaTPPlatform extends TPPlatform
{
    /** Possible operating systems. */
    public enum OS { MAC, WINDOWS, LINUX, UNKNOWN }

    /** The PlayN platform with which this platform interoperates. */
    public final JavaPlatform plat;

    public JavaTPPlatform (JavaPlatform plat, JavaPlatform.Config config) {
        this.plat = plat;

        _frame = new JFrame(config.appName);
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setName("GLCanvas");
        int width = plat.graphics().scale.scaledCeil(config.width);
        int height = plat.graphics().scale.scaledCeil(config.height);
        canvas.setPreferredSize(new Dimension(width, height));
        _frame.getContentPane().add(canvas);

        canvas.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed (MouseEvent e) {
                for (JavaNativeOverlay overlay : _overlays) {
                    final Component comp = overlay.component;
                    if (comp.contains(e.getX() - comp.getX(), e.getY() - comp.getY())) {
                        EventQueue.invokeLater(new Runnable() {
                            @Override public void run () { comp.requestFocusInWindow(); }
                        });
                        log.debug("Dispatched focus from misdirected mouse press", "event", e);
                        return;
                    }
                }

                // by default, we lose focus, so test if we need to try and preempt it
                if (_focus.get() != null && _kfc != null &&
                        !_kfc.unfocusForLocation(new Point(e.getX(), e.getY()))) {
                    final Component comp =
                        ((JavaNativeTextField)_focus.get().exposeNativeField()).component;
                    EventQueue.invokeLater(new Runnable() {
                        @Override public void run () { comp.requestFocusInWindow(); }
                    });
                }
            }
        });

        // NOTE: This order is important. Resizability changes window decorations on some
        // platforms/themes and we need the packing afterwards to take that into account.
        _frame.setResizable(false);
        _frame.pack();
        _frame.setVisible(true);

        try {
            Display.setParent(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Figure out the os
        String osname = System.getProperty("os.name");
        osname = (osname == null) ? "" : osname;
        if (osname.indexOf("Windows") != -1) _os = OS.WINDOWS;
        else if (osname.indexOf("Mac OS") != -1 || osname.indexOf("MacOS") != -1) _os = OS.MAC;
        else if (osname.indexOf("Linux") != -1) _os = OS.LINUX;
        else System.err.println("Unmatching os name: " + osname);
    }

    public void addOverlay (JavaNativeOverlay overlay) {
        _overlays.add(overlay);
        _frame.getLayeredPane().add(overlay.component);
    }

    public void removeOverlay (JavaNativeOverlay overlay) {
        if (_overlays.remove(overlay)) {
            _frame.getLayeredPane().remove(overlay.component);
        }
    }

    @Override public boolean hasNativeTextFields () {
        // mac doesn't currently support native text :(
        return _os == OS.WINDOWS || _os == OS.LINUX;
    }

    @Override public NativeTextField createNativeTextField (Field.Native field) {
        return refresh(new JavaNativeTextField(this, field));
    }

    @Override public NativeTextField refresh (NativeTextField previous) {
        JavaNativeTextField jfield = ((JavaNativeTextField)previous).refresh();
        jfield.validateStyles();
        return jfield;
    }

    @Override public ImageOverlay createImageOverlay (playn.core.Image image) {
        return new JavaImageOverlay(this, image);
    }

    @Override public void clearFocus () {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run () {
                _frame.getContentPane().getComponent(0).requestFocusInWindow();
            }
        });
    }

    /**
     * Gets the OS this JVM is running on.
     */
    public OS os () {
        return _os;
    }

    /**
     * Sets the title of the window.
     *
     * @param title the window title
     */
    public void setTitle (String title) {
        _frame.setTitle(title);
    }

    /**
     * Sets the window icon.
     *
     * Takes icons of different sizes, preferring earlier ones in case of duplicate sizes.
     */
    public void setIcon (playn.core.Image... icons) {
        assert icons.length > 0;
        List<BufferedImage> images = new ArrayList<BufferedImage>();
        for (playn.core.Image icon : icons) images.add(((JavaImage)icon).bufferedImage());
        _frame.setIconImages(images);
    }

    <T> void updateOnMainThread (final Value<T> value, final T nvalue) {
        plat.invokeLater(new Slot<Platform>() {
            @Override public void onEmit (Platform plat) { value.update(nvalue); }
        });
    }

    <T> void emitOnMainThread (final Signal<T> signal, final T emission) {
        plat.invokeLater(new Slot<Platform>() {
            @Override public void onEmit (Platform plat) { signal.emit(emission); }
        });
    }

    JavaNativeOverlay findOverlayFor (Component comp) {
        for (JavaNativeOverlay overlay : _overlays) {
            if (overlay.component.isAncestorOf(comp)) return overlay;
        }
        return null;
    }

    boolean hasOverlayFor (Component comp) {
        return findOverlayFor(comp) != null;
    }

    protected JFrame _frame;
    protected OS _os = OS.UNKNOWN;
    protected Set<JavaNativeOverlay> _overlays = new HashSet<JavaNativeOverlay>();
}
