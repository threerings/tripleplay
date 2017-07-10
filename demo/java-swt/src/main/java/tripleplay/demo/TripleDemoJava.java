//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import java.util.List;

import com.google.common.collect.Lists;

import playn.core.Image;
import playn.java.JavaPlatform;
import playn.java.SWTPlatform;

import tripleplay.platform.SWTTPPlatform;

public class TripleDemoJava
{
    enum Toolkit { NONE, AWT, SWT }

    public static void main (String[] args) {
        JavaPlatform.Config config = new JavaPlatform.Config();
        config.appName = "Tripleplay Demo (SWT)";

        List<String> mainArgs = Lists.newArrayList();
        String size = "--size=";
        for (int ii = 0; ii < args.length; ii++) {
            if (args[ii].startsWith(size)) {
                String[] wh = args[ii].substring(size.length()).split("x");
                config.width = Integer.parseInt(wh[0]);
                config.height = Integer.parseInt(wh[1]);
            }
            // else if (args[ii].equals("--retina")) config.scaleFactor = 2;
            else mainArgs.add(args[ii]);
        }

        TripleDemo.mainArgs = mainArgs.toArray(new String[0]);
        SWTPlatform plat = new SWTPlatform(config);
        SWTTPPlatform tpplat = new SWTTPPlatform(plat, config);
        tpplat.setIcon(loadIcon(plat));
        new TripleDemo(plat);
        plat.start();
    }

    protected static Image loadIcon (JavaPlatform plat) {
        return plat.assets().getImageSync("icon.png");
    }
}
