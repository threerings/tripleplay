//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import playn.core.PlayN;
import playn.java.JavaPlatform;

public class TripleDemoJava
{
    public static void main (String[] args) {
        JavaPlatform platform = JavaPlatform.register();
        platform.assets().setPathPrefix("tripleplay/rsrc");
        TripleDemo.mainArgs = args;
        PlayN.run(new TripleDemo());
    }
}
