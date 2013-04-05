//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import playn.core.PlayN;
import playn.java.JavaPlatform;
import tripleplay.platform.JavaTPPlatform;

public class TripleDemoJava
{
    public static void main (String[] args) {
        JavaPlatform.Config config = new JavaPlatform.Config();
        JavaPlatform platform = JavaPlatform.register(config);
        platform.assets().setPathPrefix("tripleplay/rsrc");
        TripleDemo.mainArgs = args;

        // TODO: upgrade to include other systems
        if (System.getProperty("os.name").contains("Linux")) {
            JavaTPPlatform.register(platform, config);
        }

        PlayN.run(new TripleDemo());
    }
}
