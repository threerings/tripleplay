//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.awt.Dimension;
import java.awt.Canvas;
import javax.swing.JFrame;

import org.lwjgl.opengl.Display;

import playn.core.Keyboard;
import playn.java.JavaPlatform;
import react.Value;
import react.ValueView;

/**
 * Implements Java-specific TriplePlay services.
 */
public class JavaTPPlatform extends TPPlatform
{
    /** Registers the IOS TriplePlay platform. */
    public static JavaTPPlatform register (JavaPlatform platform, JavaPlatform.Config config) {
        JavaTPPlatform instance = new JavaTPPlatform(platform, config);
        TPPlatform.register(instance);
        return instance;
    }

    protected JavaTPPlatform (JavaPlatform platform, JavaPlatform.Config config) {
        _platform = platform;

        _frame = new JFrame("Game");
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Canvas canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(config.width, config.height));
        _frame.getContentPane().add(canvas);

        _frame.pack();
        _frame.setResizable(false);
        _frame.setVisible(true);

        try {
            Display.setParent(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public boolean hasNativeTextFields () {
        return true;
    }

    @Override public NativeTextField createNativeTextField () {
        return new JavaNativeTextField(_frame.getLayeredPane());
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

    /**
     * Sets the title of the window.
     *
     * @param title the window title
     */
    public void setTitle (String title) {
        _frame.setTitle(title);
    }

    /** The Java platform with which this TPPlatform was registered. */
    protected JavaPlatform _platform;

    protected JFrame _frame;

    protected final Value<Boolean> _false = Value.create(false);
}
