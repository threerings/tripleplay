//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import playn.core.Image;
import playn.ios.IOSAbstractImage;
import cli.MonoTouch.CoreGraphics.CGContext;
import cli.MonoTouch.UIKit.UIGraphics;
import cli.MonoTouch.UIKit.UIView;
import cli.System.Drawing.RectangleF;

public class IOSImageOverlay extends IOSNativeOverlay
    implements ImageOverlay
{
    public IOSImageOverlay (Image image) {
        super(new ImageView(((IOSAbstractImage)image)));
    }

    @Override public Image image () {
        return ((ImageView)view).image;
    }

    @Override public void repaint () {
        root().SetNeedsDisplay();
    }

    protected static class ImageView extends UIView
    {
        public final IOSAbstractImage image;

        public ImageView (IOSAbstractImage image) {
            this.image = image;
        }

        @Override public void Draw (RectangleF frame) {
            super.Draw(frame);
            CGContext ctx = UIGraphics.GetCurrentContext();
            ctx.DrawImage(frame, image.cgImage());
        }
    }
}
