//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import java.util.ArrayList;

import playn.core.Assets;
import playn.core.Image;
import playn.core.Json;
import playn.core.util.Callback;

import static playn.core.PlayN.assets;
import static playn.core.PlayN.json;

import pythagoras.f.IPoint;
import pythagoras.f.Point;

import react.Value;

public class JsonLoader
{
    /**
     * Loads a JSON encoded library synchronously via PlayN assets.
     */
    public static Library loadLibrarySync (String baseDir) throws Exception {
        return loadLibrarySync(baseDir, assets());
    }

    /**
     * Loads a JSON encoded library synchronously via PlayN assets.
     */
    public static Library loadLibrarySync (String baseDir, Assets assets) throws Exception {
        String text = assets.getTextSync(baseDir + "/library.json");
        return decodeLibrarySync(json().parse(text), assets, baseDir);
    }

    /**
     * Loads a JSON encoded library via PlayN assets.
     * @param baseDir The base directory, containing library.json and texture atlases.
     */
    public static void loadLibrary (String baseDir, Callback<Library> callback) {
        loadLibrary(assets(), baseDir, callback);
    }

    /**
     * Loads a JSON encoded library via the specified PlayN assets.
     */
    public static void loadLibrary (final Assets assets, final String baseDir,
                                    final Callback<Library> callback) {
        assets.getText(baseDir + "/library.json", new Callback.Chain<String>(callback) {
            public void onSuccess (String text) {
                try {
                    decodeLibraryAsync(json().parse(text), assets, baseDir, callback);
                } catch (Exception err) {
                    callback.onFailure(err);
                }
            }
        });
    }

    /** Helper interface to load an image from a path. */
    protected static interface ImageLoader {
        public Image load (String path);
    }

    /**
     * Decodes and returns a library synchronously.
     */
    protected static Library decodeLibrarySync (Json.Object json, final Assets assets,
                                                String baseDir)
    {
        final Library[] libs = new Library[]{null};
        decodeLibrary(json, baseDir, new Callback<Library>() {
            public void onSuccess (Library result) {libs[0] = result;}
            public void onFailure(Throwable cause) {}
        }, new ImageLoader() {
            @Override public Image load (String path) {
                return assets.getImageSync(path);
            }
        });
        return libs[0];
    }

    /**
     * Decodes and returns a library asynchronously.
     */
    protected static void decodeLibraryAsync (Json.Object json, final Assets assets, String baseDir,
                                              Callback<Library> callback)
    {
        decodeLibrary(json, baseDir, callback, new ImageLoader() {
            @Override public Image load (String path) {
                return assets.getImage(path);
            }
        });
    }

    /**
     * Generic library decoding method.
     */
    protected static void decodeLibrary (Json.Object json, String baseDir,
                                         final Callback<Library> callback,
                                         final ImageLoader loader)
    {
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
            @Override public void onChange (Integer remaining, Integer unused) {
                if (remaining == 0) callback.onSuccess(new Library(frameRate, movies, textures));
            }
        });

        for (final Json.Object atlasJson : atlases) {
            Image atlas = loader.load(baseDir + "/" + atlasJson.getString("file"));
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
