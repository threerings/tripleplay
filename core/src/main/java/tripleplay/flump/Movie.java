package tripleplay.flump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import playn.core.GroupLayer;
import playn.core.Json;
import playn.core.Layer;
import playn.core.PlayN;

public class Movie
    implements Instance
{
    protected Movie (Symbol symbol) {
        // TODO(bruno): Implement movies
        throw new RuntimeException(
            "Movies aren't implemented yet [symbol=" + symbol.name() + "]");
    }

    @Override public GroupLayer layer () {
        return null;
    }

    public void update (float delta) {
    }

    public static class Symbol
        implements tripleplay.flump.Symbol
    {
        /**
         * The number of frames in this movie.
         */
        public final int frames;

        /**
         * The layers in this movie.
         */
        public final List<LayerData> layers;

        protected Symbol (Library lib, Json.Object json) {
            _name = json.getString("id");

            ArrayList<LayerData> layers = new ArrayList<LayerData>();
            this.layers = Collections.unmodifiableList(layers);

            int frames = 0;
            for (Json.Object layerJson : json.getArray("layers", Json.Object.class)) {
                LayerData layer = new LayerData(layerJson);
                frames = Math.max(layer.frames(), frames);
                layers.add(layer);
            }
            this.frames = frames;

            _frameRate = lib.frameRate;
            _duration = frames/_frameRate;
        }

        @Override public String name () {
            return _name;
        }

        @Override public Movie createInstance () {
            return new Movie(this);
        }

        protected String _name;
        protected float _frameRate;
        protected float _duration;
    }
}
