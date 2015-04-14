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

import tripleplay.platform.JavaTPPlatform;
import tripleplay.platform.SWTTPPlatform;

public class TripleDemoJava
{
    enum Toolkit { NONE, AWT, SWT }

    public static void main (String[] args) {
        JavaPlatform.Config config = new JavaPlatform.Config();
        config.appName = "Tripleplay Demo";

        Toolkit tk = Toolkit.NONE;
        List<String> mainArgs = Lists.newArrayList();
        String size = "--size=";
        for (int ii = 0; ii < args.length; ii++) {
            if (args[ii].startsWith(size)) {
                String[] wh = args[ii].substring(size.length()).split("x");
                config.width = Integer.parseInt(wh[0]);
                config.height = Integer.parseInt(wh[1]);
            } else if (args[ii].equals("--swt")) tk = Toolkit.SWT;
            else if (args[ii].equals("--awt")) tk = Toolkit.AWT;
            else if (args[ii].equals("--retina")) config.scaleFactor = 2;
            else mainArgs.add(args[ii]);
        }

        TripleDemo.mainArgs = mainArgs.toArray(new String[0]);
        JavaPlatform plat;
        switch (tk) {
        case SWT: {
            config.appName += " (SWT)";
            SWTPlatform splat = new SWTPlatform(config);
            SWTTPPlatform tpplat = new SWTTPPlatform(splat, config);
            tpplat.setIcon(loadIcon(splat));
            plat = splat;
            break;
        }
        case AWT: {
            JavaPlatform jplat = new JavaPlatform(config);
            JavaTPPlatform tpplat = new JavaTPPlatform(jplat, config);
            tpplat.setIcon(loadIcon(jplat));
            plat = jplat;
            break;
        }
        default:
            // no native integration
            plat = new JavaPlatform(config);
            break;
        }
        new TripleDemo(plat);
        plat.start();
    }

    protected static Image loadIcon (JavaPlatform plat) {
        return plat.assets().getImageSync("icon.png");
    }
}
