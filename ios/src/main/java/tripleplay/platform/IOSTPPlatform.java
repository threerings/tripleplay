//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import playn.ios.IOSPlatform;
import react.ValueView;

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

    @Override public NativeTextField createNativeTextField () {
        return new IOSNativeTextField.SingleLine(_fieldHandler, null);
    }

    @Override public void setVirtualKeyboardController (VirtualKeyboardController ctrl) {
        _fieldHandler.setVirtualKeyboardController(ctrl);
    }

    @Override public ValueView<Boolean> virtualKeyboardActive () {
        return _fieldHandler.virtualKeyboardActive();
    }

    private IOSTPPlatform (IOSPlatform platform) {
        this.platform = platform;
        _fieldHandler = new IOSTextFieldHandler(this);
    }

    protected final IOSTextFieldHandler _fieldHandler;
}
