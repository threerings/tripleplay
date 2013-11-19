//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import java.util.List;

import com.google.common.collect.Lists;

import playn.core.PlayN;
import playn.java.JavaPlatform;
import playn.java.SWTPlatform;
import tripleplay.platform.JavaTPPlatform;
import tripleplay.platform.SWTTPPlatform;

public class TripleDemoJava
{
    public static void main (String[] args) {
        JavaPlatform.Config config = new JavaPlatform.Config();

        boolean swt = false;
        List<String> mainArgs = Lists.newArrayList();
        for (int ii = 0; ii < args.length; ii++) {
            String size = "--size=";
            if (args[ii].startsWith(size)) {
                String[] wh = args[ii].substring(size.length()).split("x");
                config.width = Integer.parseInt(wh[0]);
                config.height = Integer.parseInt(wh[1]);
                continue;
            }
            if (args[ii].equals("--swt")) {
                swt = true;
                continue;
            }
            mainArgs.add(args[ii]);
        }

        TripleDemo.mainArgs = mainArgs.toArray(new String[0]);

        if (swt) {
            SWTPlatform platform = SWTPlatform.register(config);
            SWTTPPlatform.register(platform, config);
            platform.setTitle("Tripleplay Demo (SWT)");
        } else {
            JavaPlatform platform = JavaPlatform.register(config);
            JavaTPPlatform.register(platform, config);
            JavaTPPlatform.instance().setTitle("Tripleplay Demo (Java)");
        }
        PlayN.run(new TripleDemo());
    }
}
