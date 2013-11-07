//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import playn.core.Image;
import playn.core.Keyboard;
import playn.ios.IOSPlatform;
import react.Value;
import tripleplay.ui.Field;

/**
 * Implements iOS-specific TriplePlay services.
 */
public class IOSTPPlatform extends TPPlatform
{
    /** Registers the IOS TriplePlay platform. */
    public static IOSTPPlatform register (IOSPlatform platform) {
        IOSTPPlatform instance = new IOSTPPlatform(platform);
        TPPlatform.register(instance);
        return instance;
    }

    /** The iOS platform with which this TPPlatform was registered. */
    public final IOSPlatform platform;

    @Override public boolean hasNativeTextFields () {
        return true;
    }

    @Override public NativeTextField createNativeTextField (
            Field.Native field, NativeTextField.Mode mode) {
        switch (mode) {
        case MULTI_LINE: return new IOSNativeTextField.MultiLine(_fieldHandler, null, field);
        default: return new IOSNativeTextField.SingleLine(_fieldHandler, null, field).
                refreshMode(mode);
        }
    }

    @Override public NativeTextField refreshNativeTextField (
            NativeTextField previous, NativeTextField.Mode mode) {
        return ((IOSNativeTextField)previous).refreshMode(mode);
    }

    @Override public void setVirtualKeyboardController (VirtualKeyboardController ctrl) {
        _fieldHandler.setVirtualKeyboardController(ctrl);
    }

    @Override public void setVirtualKeyboardListener (Keyboard.Listener listener) {
        _fieldHandler.setKeyboardListener(listener);
    }

    @Override public ImageOverlay createImageOverlay (Image image) {
        return new IOSImageOverlay(image);
    }

    private IOSTPPlatform (IOSPlatform platform) {
        this.platform = platform;
        _fieldHandler = new IOSTextFieldHandler(this);
    }

    protected final IOSTextFieldHandler _fieldHandler;
}
