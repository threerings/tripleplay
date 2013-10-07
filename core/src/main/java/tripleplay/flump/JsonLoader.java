//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import java.util.ArrayList;

import playn.core.Asserts;
import playn.core.Image;
import playn.core.Json;
import playn.core.util.Callback;
import static playn.core.PlayN.assets;
import static playn.core.PlayN.json;

import pythagoras.f.IPoint;
import pythagoras.f.Point;

import react.Value;

public class JsonLoader {

    /**
     * Loads a JSON encoded library via PlayN assets.
     * @param baseDir The base directory, containing library.json and texture atlases.
     */
    public static void loadLibrary (final String baseDir, final Callback<Library> callback) {
        Asserts.checkNotNull(callback);
        assets().getText(baseDir + "/library.json", new Callback.Chain<String>(callback) {
            public void onSuccess (String text) {
                try {
                    decodeLibrary(json().parse(text), baseDir, callback);
                } catch (Exception err) {
                    callback.onFailure(err);
                }
            }
        });
    }

    protected static void decodeLibrary (Json.Object json, String baseDir,
                                         final Callback<Library> callback) {
        final float frameRate = json.getNumber("frameRate");
        final ArrayList<Movie.Symbol> movies = new ArrayList<Movie.Symbol>();
        for (Json.Object movieJson : json.getArray("movies", Json.Object.class)) {
            movies.add(decodeMovie(frameRate, movieJson));
        }

        final ArrayList<Texture.Symbol> textures = new ArrayList<Texture.Symbol>();
        Json.TypedArray<Json.Object> textureGroups =
            json.getArray("textureGroups", Json.Object.class);
        // TODO(bruno): Support multiple scaleFactors?
        Json.TypedArray<Json.Object> atlases =
            textureGroups.get(0).getArray("atlases", Json.Object.class);

        final Value<Integer> remainingAtlases = Value.create(atlases.length());
        remainingAtlases.connectNotify(new Value.Listener<Integer>() {
            @Override public void onChange (Integer remaining, Integer _) {
                if (remaining == 0) callback.onSuccess(new Library(frameRate, movies, textures));
            }
        });

        for (final Json.Object atlasJson : atlases) {
            Image atlas = assets().getImage(baseDir + "/" + atlasJson.getString("file"));
            atlas.addCallback(new Callback.Chain<Image>(callback) {
                public void onSuccess (Image atlas) {
                    for (Json.Object tjson : atlasJson.getArray("textures", Json.Object.class)) {
                        textures.add(decodeTexture(tjson, atlas));
                    }
                    remainingAtlases.update(remainingAtlases.get() - 1);
                }
            });
        }
    }

    protected static Movie.Symbol decodeMovie (float frameRate, Json.Object json) {
        String name = json.getString("id");
        ArrayList<LayerData> layers = new ArrayList<LayerData>();
        for (Json.Object layerJson : json.getArray("layers", Json.Object.class)) {
            layers.add(decodeLayerData(layerJson));
        }
        return new Movie.Symbol(frameRate, name, layers);
    }

    protected static LayerData decodeLayerData (Json.Object json) {
        String name = json.getString("name");
        ArrayList<KeyframeData> keyframes = new ArrayList<KeyframeData>();
        KeyframeData prevKf = null;
        for (Json.Object kfJson : json.getArray("keyframes", Json.Object.class)) {
            prevKf = decodeKeyframeData(kfJson, prevKf);
            keyframes.add(prevKf);
        }
        return new LayerData(name, keyframes);
    }

    protected static KeyframeData decodeKeyframeData (Json.Object json, KeyframeData prevKf) {
        return new KeyframeData((prevKf != null) ? prevKf.index + prevKf.duration : 0,
                                json.getInt("duration"), json.getString("label"),
                                getPoint(json, "loc", 0, 0), getPoint(json, "scale", 1, 1),
                                getPoint(json, "skew", 0, 0), getPoint(json, "pivot", 0, 0),
                                json.getBoolean("visible", true), json.getNumber("alpha", 1),
                                json.getBoolean("tweened", true), json.getNumber("ease", 0),
                                json.getString("ref"));
    }

    protected static Texture.Symbol decodeTexture (Json.Object json, Image atlas) {
        Json.TypedArray<Float> rect = json.getArray("rect", Float.class);
        return new Texture.Symbol(
            json.getString("symbol"), getPoint(json, "origin", 0, 0),
            atlas.subImage(rect.get(0), rect.get(1), rect.get(2), rect.get(3)));
    }

    protected static IPoint getPoint (Json.Object json, String field, float defX, float defY) {
        Json.TypedArray<Float> array = json.getArray(field, Float.class);
        return (array != null) ? new Point(array.get(0), array.get(1)) : new Point(defX, defY);
    }
}
