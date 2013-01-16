//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import playn.android.GameActivity;
import playn.core.PlayN;

public class TripleDemoActivity extends GameActivity
{
    @Override public void main () {
        platform().assets().setPathPrefix("tripleplay/rsrc");
        PlayN.run(new TripleDemo());
    }
}
