package tripleplay.flump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import playn.core.Json;

public class LayerData
{
    /**
     * The authored name of this layer.
     */
    public final String name;

    /**
     * The keyframes in this layer.
     */
    public List<KeyframeData> keyframes;

    protected LayerData (Json.Object json) {
        name = json.getString("name");

        ArrayList<KeyframeData> keyframes = new ArrayList<KeyframeData>();
        this.keyframes = Collections.unmodifiableList(keyframes);

        KeyframeData prevKf = null;
        for (Json.Object kfJson : json.getArray("keyframes", Json.Object.class)) {
            prevKf = new KeyframeData(kfJson, prevKf);
            keyframes.add(prevKf);
        }
    }

    /**
     * The number of frames in this layer.
     */
    public int frames () {
        KeyframeData lastKf = keyframes.get(keyframes.size() - 1);
        return lastKf.index + lastKf.duration;
    }

    protected boolean _multipleSymbols;
    protected Symbol _lastSymbol;
}
