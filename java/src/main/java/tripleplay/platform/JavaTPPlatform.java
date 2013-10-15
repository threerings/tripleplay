//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.JFrame;

import org.lwjgl.opengl.Display;

import playn.core.Asserts;
import playn.core.Keyboard;
import playn.java.JavaImage;
import playn.java.JavaPlatform;
import react.Value;
import react.ValueView;
import tripleplay.ui.Field;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Implements Java-specific TriplePlay services.
 */
public class JavaTPPlatform extends TPPlatform
{
    /** Possible operating systems. */
    public enum OS { MAC, WINDOWS, LINUX, UNKNOWN }

    /** Registers the Java TriplePlay platform. */
    public static JavaTPPlatform register (JavaPlatform platform, JavaPlatform.Config config) {
        JavaTPPlatform instance = new JavaTPPlatform(platform, config);
        TPPlatform.register(instance);
        return instance;
    }

    protected JavaTPPlatform (JavaPlatform platform, JavaPlatform.Config config) {
        _platform = platform;

        _frame = new JFrame("");
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setName("GLCanvas");
        canvas.setPreferredSize(new Dimension(config.width, config.height));
        canvas.setFocusable(false);
        _frame.getContentPane().add(canvas);

        // NOTE: This order is important. Resizability changes window decorations on some
        // platforms/themes and we need the packing to happen last to take that into account.
        _frame.setResizable(false);
        _frame.setVisible(true);
        _frame.pack();

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

    @Override public boolean hasNativeTextFields () {
        // mac doesn't currently support native text :(
        return _os == OS.WINDOWS || _os == OS.LINUX;
    }

    @Override public NativeTextField createNativeTextField (
            Field.Native field, NativeTextField.Mode mode) {
        return new JavaNativeTextField(field, mode, null);
    }

    @Override public NativeTextField refreshNativeTextField (
            NativeTextField previous, NativeTextField.Mode mode) {
        return ((JavaNativeTextField)previous).refreshMode(mode);
    }

    @Override public void setVirtualKeyboardController (VirtualKeyboardController ctrl) {
        // nada, no virtual keyboard
    }

    @Override public void setVirtualKeyboardListener (Keyboard.Listener listener) {
        // nada, no virtual keyboard
    }

    @Override public ValueView<Boolean> virtualKeyboardActive () {
        return _false;
    }

    @Override public ImageOverlay createImageOverlay (playn.core.Image image) {
        return new JavaImageOverlay(image);
    }

    /**
     * Gets the top-level window used by the tripleplay platform.
     */
    public JFrame frame () {
        return _frame;
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
        Asserts.check(icons.length > 0);
        _frame.setIconImages(Lists.transform(Lists.newArrayList(icons),
            new Function<playn.core.Image, java.awt.Image>() {
                public Image apply (playn.core.Image input) {
                    return ((JavaImage)input).bufferedImage();
                }
            }));
    }

    /** The Java platform with which this TPPlatform was registered. */
    protected JavaPlatform _platform;

    protected JFrame _frame;

    protected OS _os = OS.UNKNOWN;

    protected final Value<Boolean> _false = Value.create(false);
}
