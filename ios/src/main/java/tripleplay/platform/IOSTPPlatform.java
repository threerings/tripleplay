//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import cli.MonoTouch.CoreGraphics.CGAffineTransform;
import cli.MonoTouch.UIKit.UIDeviceOrientation;
import cli.MonoTouch.UIKit.UIScreen;
import cli.System.Drawing.RectangleF;
import playn.core.Image;
import playn.ios.IOSPlatform;
import pythagoras.f.FloatMath;
import tripleplay.ui.Field;

/**
 * Implements iOS-specific TriplePlay services.
 */
public class IOSTPPlatform extends TPPlatform
    implements IOSPlatform.OrientationChangeListener
{
    /** Registers the IOS TriplePlay platform. */
    public static IOSTPPlatform register (IOSPlatform platform) {
        IOSTPPlatform instance = new IOSTPPlatform(platform);
        TPPlatform.register(instance);
        platform.setOrientationChangeListener(instance);
        return instance;
    }

    /** The iOS platform with which this TPPlatform was registered. */
    public final IOSPlatform platform;

    @Override public boolean hasNativeTextFields () {
        return true;
    }

    @Override public NativeTextField createNativeTextField (Field.Native field) {
        return (field.resolveStyle(Field.MULTILINE) ?
            new IOSNativeTextField.MultiLine(_fieldHandler, null, field) :
            new IOSNativeTextField.SingleLine(_fieldHandler, null, field)).refresh();
    }

    @Override public NativeTextField refresh (NativeTextField previous) {
        return ((IOSNativeTextField)previous).refresh();
    }

    @Override public ImageOverlay createImageOverlay (Image image) {
        return new IOSImageOverlay(image);
    }

    @Override public void orientationChanged (int orientationValue) {
        CGAffineTransform trans = CGAffineTransform.MakeIdentity();
        boolean landscape = false;
        switch (orientationValue) {
        default:
        case UIDeviceOrientation.Portrait:
          break;
        case UIDeviceOrientation.PortraitUpsideDown:
          trans.Rotate(FloatMath.PI);
          break;
        case UIDeviceOrientation.LandscapeLeft:
          landscape = true;
          trans.Rotate(FloatMath.PI / 2);
          break;
        case UIDeviceOrientation.LandscapeRight:
          landscape = true;
          trans.Rotate(-FloatMath.PI / 2);
          break;
        }

        _uiOverlay.set_Transform(trans);

        RectangleF overlayBounds = _uiOverlay.get_Bounds();
        if ((overlayBounds.get_Width() > overlayBounds.get_Height()) != landscape) {
          // swap the width and height
          float width = overlayBounds.get_Width();
          overlayBounds.set_Width(overlayBounds.get_Height());
          overlayBounds.set_Height(width);
          _uiOverlay.set_Bounds(overlayBounds);
        }
        // update the overlay's hidden area, if any
        _uiOverlay.setHiddenArea(_hidden);
    }

    @Override public void updateHidden () {
        _uiOverlay.setHiddenArea(_hidden);
    }

    @Override public void refreshNativeBounds () {
        _fieldHandler.refreshNativeBounds();
    }

    IOSUIOverlay overlay () {
        return _uiOverlay;
    }

    private IOSTPPlatform (IOSPlatform platform) {
        this.platform = platform;
        platform.gameView().Add(
            _uiOverlay = new IOSUIOverlay(UIScreen.get_MainScreen().get_Bounds()));
        _fieldHandler = new IOSTextFieldHandler(this);
    }

    protected final IOSTextFieldHandler _fieldHandler;
    protected final IOSUIOverlay _uiOverlay;
}
