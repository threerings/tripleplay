//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import com.google.gwt.core.client.EntryPoint;

import playn.html.HtmlPlatform;

public class TripleDemoHtml implements EntryPoint {

    @Override public void onModuleLoad () {
        HtmlPlatform.Config config = new HtmlPlatform.Config();
        HtmlPlatform plat = new HtmlPlatform(config);
        plat.setTitle("TriplePlay Demo");
        plat.assets().setPathPrefix("tripledemo/");
        // plat.disableRightClickContextMenu();
        new TripleDemo(plat);
        plat.start();
    }
}
