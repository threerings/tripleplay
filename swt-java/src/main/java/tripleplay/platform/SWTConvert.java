//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.platform;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import playn.java.JavaImage;

public class SWTConvert
{
    public SWTConvert (Display display) {
        _display = display;
    }

    public Image image (playn.core.Image image) {
        return new Image(_display, image(((JavaImage)image).bufferedImage()));
    }

    public ImageData image (BufferedImage image) {
        if (image.getColorModel() instanceof DirectColorModel) {
            DirectColorModel cmodel = (DirectColorModel)image.getColorModel();
            PaletteData palette = new PaletteData(
                cmodel.getRedMask(), cmodel.getGreenMask(), cmodel.getBlueMask());
            ImageData data = new ImageData(image.getWidth(), image.getHeight(),
                cmodel.getPixelSize(), palette);
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    int rgb = image.getRGB(x, y);
                    int pixel = palette.getPixel(
                        new RGB((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
                    data.setPixel(x, y, pixel);
                    if (cmodel.hasAlpha()) data.setAlpha(x, y, (rgb >> 24) & 0xFF);
                }
            }
            return data;

        } else if (image.getColorModel() instanceof IndexColorModel) {
            IndexColorModel cmodel = (IndexColorModel)image.getColorModel();
            int size = cmodel.getMapSize();
            byte[] reds = new byte[size];
            byte[] greens = new byte[size];
            byte[] blues = new byte[size];
            cmodel.getReds(reds);
            cmodel.getGreens(greens);
            cmodel.getBlues(blues);
            RGB[] rgbs = new RGB[size];
            for (int ii = 0; ii < rgbs.length; ii++) {
                rgbs[ii] = new RGB(reds[ii] & 0xFF, greens[ii] & 0xFF, blues[ii] & 0xFF);
            }
            PaletteData palette = new PaletteData(rgbs);
            ImageData data = new ImageData(
                image.getWidth(), image.getHeight(), cmodel.getPixelSize(), palette);
            data.transparentPixel = cmodel.getTransparentPixel();
            WritableRaster raster = image.getRaster();
            int[] pixelArray = new int[1];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y, pixelArray[0]);
                }
            }
            return data;
        } else if (image.getColorModel() instanceof ComponentColorModel) {
            ComponentColorModel cmodel = (ComponentColorModel)image.getColorModel();
            PaletteData palette = new PaletteData(0x0000FF, 0x00FF00, 0xFF0000); // BGR
            ImageData data = new ImageData(image.getWidth(), image.getHeight(), 24, palette);
            if (cmodel.hasAlpha()) data.alphaData = new byte[image.getWidth() * image.getHeight()];
            WritableRaster raster = image.getRaster();
            int[] pixelArray = new int[4];
            for (int y = 0; y < data.height; y++) {
                for (int x = 0; x < data.width; x++) {
                    raster.getPixel(x, y, pixelArray);
                    data.setPixel(x, y,
                        (pixelArray[2] << 16) | (pixelArray[1] << 8) | (pixelArray[0]));
                    if (data.alphaData != null)
                        data.alphaData[y*data.width + x] = (byte)pixelArray[3];
                }
            }
            return data;
        }
        return null;
    }

    public Font font (playn.core.Font font)
    {
        int style;
        switch (font.style()) {
        default:
            style = SWT.NORMAL;
            break;
        case BOLD:
            style = SWT.BOLD;
            break;
        case ITALIC:
            style = SWT.ITALIC;
            break;
        case BOLD_ITALIC:
            style = SWT.BOLD | SWT.ITALIC;
            break;
        }

        int height = (int)Math.round(font.size() * 72.0 / _display.getDPI().y);
        Font swt = new Font(_display, font.name(), height, style);
        return swt;
    }

    public Color color (int color)
    {
        class Pl extends playn.core.Color {};
        return new Color(_display, Pl.red(color), Pl.green(color), Pl.blue(color));
    }

    protected final Display _display;
}
