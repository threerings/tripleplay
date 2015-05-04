//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo;

import android.util.DisplayMetrics;

import playn.android.GameActivity;

public class TripleDemoActivity extends GameActivity
{
    @Override public void main () {
        new TripleDemo(platform());
    }

    protected float scaleFactor () {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return Math.min(2, dm.density);
    }
}
