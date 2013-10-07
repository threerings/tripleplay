//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import java.util.Collections;
import java.util.List;

public class LayerData
{
    /** The authored name of this layer. */
    public final String name;

    /** The keyframes in this layer. */
    public final List<KeyframeData> keyframes;

    public LayerData (String name, List<KeyframeData> keyframes) {
        this.name = name;
        this.keyframes = Collections.unmodifiableList(keyframes);
    }

    /** The number of frames in this layer. */
    public int frames () {
        KeyframeData lastKf = keyframes.get(keyframes.size() - 1);
        return lastKf.index + lastKf.duration;
    }

    // these are filled in by Library after the library is loaded
    protected boolean _multipleSymbols;
    protected Symbol _lastSymbol;
}
