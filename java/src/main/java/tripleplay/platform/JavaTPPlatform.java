//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.awt.Canvas;
import javax.swing.JFrame;

import org.lwjgl.opengl.Display;

import playn.core.PlayN;
import playn.java.JavaPlatform;

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
        _frame.setSize(config.width, config.height);

        Canvas canvas = new Canvas();
        _frame.getContentPane().add(canvas);

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

    /** The Java platform with which this TPPlatform was registered. */
    protected JavaPlatform _platform;

    protected JFrame _frame;
}
