//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import pythagoras.f.IPoint;
import pythagoras.f.Point;

import playn.core.Json;

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

    protected KeyframeData (Json.Object json, KeyframeData prevKf) {
        index = (prevKf != null) ? prevKf.index + prevKf.duration : 0;
        duration = json.getInt("duration");
        label = json.getString("label");

        loc = getPoint(json, "loc", 0, 0);
        scale = getPoint(json, "scale", 1, 1);
        skew = getPoint(json, "skew", 0, 0);
        pivot = getPoint(json, "pivot", 0, 0);
        alpha = json.getNumber("alpha", 1);
        visible = json.getBoolean("visible", true);
        tweened = json.getBoolean("tweened", true);
        ease = json.getNumber("ease", 0);

        _symbolName = json.getString("ref");
        // Library resolves _symbol once everything has been loaded
    }

    /**
     * The symbol on this keyframe, if any.
     */
    public Symbol symbol () {
        return _symbol;
    }

    protected static IPoint getPoint (Json.Object json, String field, float defX, float defY) {
        Json.TypedArray<Float> array = json.getArray(field, Float.class);
        return (array != null) ? new Point(array.get(0), array.get(1)) : new Point(defX, defY);
    }

    protected Symbol _symbol;
    protected String _symbolName;
}
