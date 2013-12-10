//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import cli.MonoTouch.CoreAnimation.CAShapeLayer;
import cli.MonoTouch.CoreGraphics.CGPath;
import cli.MonoTouch.UIKit.UIEvent;
import cli.MonoTouch.UIKit.UIView;
import cli.System.Drawing.PointF;
import cli.System.Drawing.RectangleF;

import pythagoras.f.IRectangle;

public class IOSUIOverlay extends UIView
{
    public IOSUIOverlay (RectangleF bounds) {
        super(bounds);
        set_MultipleTouchEnabled(true);
    }

    @Override public boolean PointInside (PointF pointF, UIEvent uiEvent) {
        // if it's masked, we don't want it
        if (_hidden != null && _hidden.Contains(pointF)) return false;

        // only accept the touch if it is hitting one of our native widgets
        UIView[] subs = get_Subviews();
        if (subs == null) return false;

        for (UIView view : subs)
            if (view.PointInside(ConvertPointToView(pointF, view), uiEvent))
                return true;

        return false;
    }

    public void setHiddenArea (IRectangle area) {
        _hidden = area == null ? null : new RectangleF(area.x(), area.y(),
            area.width(), area.height());

        if (_hidden == null) {
            get_Layer().set_Mask(null);
            return;
        }

        RectangleF bounds = get_Bounds();
        CAShapeLayer maskLayer = new CAShapeLayer();
        // draw four rectangles surrounding the area we want to hide, and create a mask out of it.
        CGPath path = new CGPath();
        // top
        path.AddRect(new RectangleF(0, 0, bounds.get_Width(), _hidden.get_Top()));
        // bottom
        path.AddRect(new RectangleF(0, _hidden.get_Bottom(), bounds.get_Width(),
            bounds.get_Bottom() - _hidden.get_Bottom()));
        // left
        path.AddRect(new RectangleF(0, _hidden.get_Top(), _hidden.get_Left(), _hidden.get_Height()));
        // right
        path.AddRect(new RectangleF(_hidden.get_Right(), _hidden.get_Top(), bounds.get_Right()
            - _hidden.get_Right(), _hidden.get_Height()));
        maskLayer.set_Path(path);
        get_Layer().set_Mask(maskLayer);
    }

    protected RectangleF _hidden;
}
