//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import playn.core.PlayN;
import playn.html.HtmlGame;
import playn.html.HtmlPlatform;

public class TripleDemoHtml extends HtmlGame
{
    @Override public void start () {
        HtmlPlatform platform = HtmlPlatform.register();
        platform.assets().setPathPrefix("tripledemo/");
        PlayN.run(new TripleDemo());
    }
}
