//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.util.HashMap;
import java.util.Map;

import cli.MonoTouch.CoreGraphics.CGAffineTransform;
import cli.MonoTouch.Foundation.NSNotification;
import cli.MonoTouch.Foundation.NSNotificationCenter;
import cli.MonoTouch.Foundation.NSSet;
import cli.MonoTouch.Foundation.NSValue;
import cli.MonoTouch.UIKit.UIDevice;
import cli.MonoTouch.UIKit.UIDeviceOrientation;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UIFont;
import cli.MonoTouch.UIKit.UIKeyboard;
import cli.MonoTouch.UIKit.UITextField;
import cli.MonoTouch.UIKit.UITextView;
import cli.MonoTouch.UIKit.UIView;
import cli.System.Drawing.PointF;
import cli.System.Drawing.RectangleF;
import cli.System.Drawing.SizeF;

import playn.core.Font;
import playn.core.PlayN;
import playn.ios.IOSFont;
import playn.ios.IOSPlatform;
import pythagoras.f.Point;

import static tripleplay.platform.Log.log;

/**
 * Handles shared bits for native text fields.
 */
public class IOSTextFieldHandler
{
    public IOSTextFieldHandler (IOSTPPlatform platform) {
        _platform = platform;
        _overlay = platform.overlay();
        _touchDetector = new TouchDetector(_overlay.get_Bounds());

        cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_
            // dispatches text changes
            change = new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_(new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_.Method() {
                @Override public void Invoke (NSNotification nf) {
                    // we get notifications about all text fields, whether they're under our
                    // control or not
                    IOSNativeTextField field = _activeFields.get(nf.get_Object());
                    if (field != null) field.handleNewValue();
                }}),
            // dispatches text starting edit
            didBegin = new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_(new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_.Method() {
                @Override public void Invoke (NSNotification nf) {
                    IOSNativeTextField field = _activeFields.get(nf.get_Object());
                    if (field != null) field.didStart();
                }}),
            // dispatches text end notifications
            didEnd = new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_(new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_.Method() {
                @Override public void Invoke (NSNotification nf) {
                    IOSNativeTextField field = _activeFields.get(nf.get_Object());
                    if (field != null) field.didFinish();
                }});

        NSNotificationCenter center = NSNotificationCenter.get_DefaultCenter();

        // observe UITextField
        center.AddObserver(UITextField.get_TextDidBeginEditingNotification(), didBegin);
        center.AddObserver(UITextField.get_TextFieldTextDidChangeNotification(), change);
        center.AddObserver(UITextField.get_TextDidEndEditingNotification(), didEnd);

        // observe UITextView
        center.AddObserver(UITextView.get_TextDidBeginEditingNotification(), didBegin);
        center.AddObserver(UITextView.get_TextDidChangeNotification(), change);
        center.AddObserver(UITextView.get_TextDidEndEditingNotification(), didEnd);

        // slide the game view up when the keyboard is displayed
        center.AddObserver(UIKeyboard.get_DidShowNotification(),
            new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_(new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_.Method() {
                @Override public void Invoke (NSNotification nf) {
                    if (_gameViewTransformed) {
                        // already transformed, bail with a warning
                        log.warning("Keyboard shown when already showing?", "viewTransform",
                                    _gameViewTransform);
                        return;
                    }

                    // find the first responder
                    IOSNativeTextField firstResponder = findFirstResponder();
                    if (firstResponder == null) return; // it's not a field we're managing, bail

                    // figure out how we need to transform the game view
                    SizeF size = ((NSValue) nf.get_UserInfo().get_Item(
                        UIKeyboard.get_FrameBeginUserInfoKey())).get_RectangleFValue().get_Size();
                    RectangleF fieldFrame = firstResponder.getView().get_Frame();
                    // oddly, the size given for keyboard dimensions is portrait, always.
                    float targetOffset = -size.get_Width() +
                        _overlay.get_Bounds().get_Height() - fieldFrame.get_Bottom();
                    // give it a little padding, and make sure we never move the game view down,
                    // also make sure we never move the bottom of the game view past the top of the
                    // keyboard
                    targetOffset = Math.max(Math.min(targetOffset - 10, 0), -size.get_Width());
                    PointF target = new PointF(0, targetOffset);
                    target = _overlay.get_Transform().TransformPoint(target);

                    // update and set the transform on the game view
                    UIView gameView = _overlay.get_Superview();
                    CGAffineTransform trans = gameView.get_Transform();
                    _gameViewTransform = trans.Invert().Invert(); // clone
                    trans.Translate(target.get_X(), target.get_Y());
                    gameView.set_Transform(trans);
                    _gameViewTransformed = true;

                    // touches outside of the keyboard will close the keyboard
                    _overlay.Add(_touchDetector);
                }}));

        center.AddObserver(UIKeyboard.get_WillHideNotification(),
            new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_(new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_.Method() {
                @Override public void Invoke (NSNotification nf) {
                    // bail if not transformed; this might be ok, if the keyboard was shown outside
                    // of our purview
                    if (!_gameViewTransformed) return;

                    UIView gameView = _overlay.get_Superview();
                    gameView.set_Transform(_gameViewTransform);
                    _gameViewTransform = null;
                    _gameViewTransformed = false;
                    _touchDetector.RemoveFromSuperview();
                    _platform._focus.update(null);
                }}));

        _currentOrientation = UIDevice.get_CurrentDevice().get_Orientation().Value;
        center.AddObserver(UIDevice.get_OrientationDidChangeNotification(),
            new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_(new cli.System.Action$$00601_$$$_Lcli__MonoTouch__Foundation__NSNotification_$$$$_.Method() {
                @Override
                public void Invoke (NSNotification nf) {
                    UIDeviceOrientation orient = UIDevice.get_CurrentDevice().get_Orientation();
                    if (orient.Value == _currentOrientation) return; // NOOP
                    if (!((IOSPlatform)PlayN.platform()).supportedOrients().isSupported(orient)) {
                        return; // unsupported orientation, no rotation
                    }
                    _currentOrientation = orient.Value;

                    if (_gameViewTransformed) {
                        // the game rotated, and we've transformed it, kill the keyboard so it can
                        // get back to normal
                        IOSNativeTextField firstResponder = findFirstResponder();
                        if (firstResponder != null) firstResponder.getView().ResignFirstResponder();
                    }
                }}));
    }

    public UIFont getUIFont (Font font) {
        if (font == null) font = IOSFont.defaultFont();

        String iosName = ((IOSFont)font).iosName();
        UIFont uiFont = UIFont.FromName(iosName, font.size());
        if (uiFont != null) return uiFont;

        if (iosName.equals(IOSFont.defaultFont().iosName())) {
            log.warning("Font shenanigans, default font not found!", "font", font);
            return null;
        }

        // font not found, use the default font at the given size, and style
        return getUIFont(new IOSFont(null, IOSFont.defaultFont().iosName(),
                                     font.style(), font.size()));
    }

    public void activate (IOSNativeTextField field) {
        _activeFields.put(field.getView(), field);
        _overlay.Add(field.getView());
    }

    public void deactivate (IOSNativeTextField field) {
        _activeFields.remove(field.getView());
        field.getView().RemoveFromSuperview();
    }

    protected IOSNativeTextField findFirstResponder () {
        for (Map.Entry<UIView, IOSNativeTextField> entry : _activeFields.entrySet()) {
            if (entry.getKey().get_IsFirstResponder()) return entry.getValue();
        }
        return null;
    }

    protected class TouchDetector extends UIView {
        public TouchDetector (RectangleF bounds) {
            super(bounds);
        }

        @Override public void TouchesBegan (NSSet touches, UIEvent uiEvent) {
            IOSNativeTextField firstResponder = findFirstResponder();
            if (firstResponder != null) firstResponder.getView().ResignFirstResponder();
            // call super, otherwise the TouchesEnded event for this touch are never dispatched
            super.TouchesBegan(touches, uiEvent);
        }

        @Override public boolean PointInside (PointF pointF, UIEvent uiEvent) {
            // let through any touch that the virtual keyboard controller wants to allow.
            if (!hideVirtualKeyboardAt(pointF)) return false;
            // allow through touches that hit text fields we manage
            for (IOSNativeTextField field : _activeFields.values()) {
                if (field.getView().PointInside(
                        ConvertPointToView(pointF, field.getView()), uiEvent)) {
                    return false;
                }
            }
            // else absorb the hit at this point so that we can hide the keyboard in TouchesBegan
            return true;
        }

        protected boolean hideVirtualKeyboardAt (PointF pointF) {
            PointF overlay = ConvertPointToView(pointF, _overlay);
            Point pythagOverlay = new Point(overlay.get_X(), overlay.get_Y());
            return _platform._kfc == null ||
                    _platform._kfc.unfocusForLocation(pythagOverlay);
        }
    }

    protected IOSTPPlatform _platform;
    protected final UIView _overlay;
    protected final Map<UIView, IOSNativeTextField> _activeFields =
        new HashMap<UIView, IOSNativeTextField>();

    // we specifically track whether we've transformed the game view in a boolean because
    // CGAffineTransform is a value class and cannot be null
    protected boolean _gameViewTransformed;
    protected CGAffineTransform _gameViewTransform;
    protected int _currentOrientation;

    protected TouchDetector _touchDetector;
}
