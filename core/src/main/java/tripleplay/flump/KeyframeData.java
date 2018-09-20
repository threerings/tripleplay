//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import pythagoras.f.IPoint;

public class KeyframeData
{
    public final int index;
    public final int duration;
    public final String label;

    public final IPoint loc;
    public final IPoint scale;
    public final IPoint skew;
    public final IPoint pivot;

    public final boolean visible;
    public final float alpha;
    public final boolean tweened;
    public final float ease;

    public KeyframeData (int index, int duration, String label,
                         IPoint loc, IPoint scale, IPoint skew, IPoint pivot,
                         boolean visible, float alpha, boolean tweened, float ease,
                         String ref) {
        this.index = index;
        this.duration = duration;
        this.label = label;
        this.loc = loc;
        this.scale = scale;
        this.skew = skew;
        this.pivot = pivot;
        this.visible = visible;
        this.alpha = alpha;
        this.tweened = tweened;
        this.ease = ease;
        // Library resolves _symbol once everything has been loaded
        _symbolName = ref;
    }

    /**
     * The symbol on this keyframe, if any.
     */
    public Symbol symbol () {
        return _symbol;
    }

    protected Symbol _symbol;
    protected String _symbolName;
}
