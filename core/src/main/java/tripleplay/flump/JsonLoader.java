//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import java.util.ArrayList;
import java.util.List;

import pythagoras.f.IPoint;
import pythagoras.f.Point;

import react.Function;
import react.RFuture;
import react.RPromise;
import react.Slot;
import react.Try;
import react.Value;

import playn.core.Assets;
import playn.core.Image;
import playn.core.Json;
import playn.core.Platform;

public class JsonLoader {

    /**
     * Loads a JSON encoded library synchronously.
     * @param baseDir The base directory, containing library.json and texture atlases.
     */
    public static Library loadLibrarySync (final Platform plat, String baseDir) throws Exception {
        final ImageLoader syncLoader = new ImageLoader() {
            @Override public Image load (String path) { return plat.assets().getImageSync(path); }
        };
        String text = plat.assets().getTextSync(baseDir + "/library.json");
        Try<Library> result = decodeLibrary(plat.json().parse(text), baseDir, syncLoader).result();
        if (result.isSuccess()) return result.get();
        Throwable error = result.getFailure();
        if (error instanceof Exception) throw (Exception)error;
        else throw new RuntimeException(error);
    }

    /**
     * Loads a JSON encoded library.
     * @param baseDir The base directory, containing library.json and texture atlases.
     */
    public static RFuture<Library> loadLibrary (final Platform plat, final String baseDir) {
        final ImageLoader asyncLoader = new ImageLoader() {
            @Override public Image load (String path) { return plat.assets().getImage(path); }
        };
        return plat.assets().getText(baseDir + "/library.json").
            flatMap(new Function<String,RFuture<Library>>() {
                public RFuture<Library> apply (String text) {
                    return decodeLibrary(plat.json().parse(text), baseDir, asyncLoader);
                }
            });
    }

    /** Helper interface to load an image from a path. */
    protected static interface ImageLoader {
        public Image load (String path);
    }

    /**
     * Generic library decoding method.
     */
    protected static RFuture<Library> decodeLibrary (Json.Object json, String baseDir,
                                                     ImageLoader loader) {
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

        // trigger the loading of all of the atlas images
        List<RFuture<Image>> atlasImages = new ArrayList<RFuture<Image>>();
        for (final Json.Object atlasJson : atlases) {
            Image atlas = loader.load(baseDir + "/" + atlasJson.getString("file"));
            atlasImages.add(atlas.state);
            atlas.state.onSuccess(new Slot<Image>() {
                public void onEmit (Image image) {
                    for (Json.Object tjson : atlasJson.getArray("textures", Json.Object.class)) {
                        textures.add(decodeTexture(tjson, image.texture()));
                    }
                }
            });
        }

        // aggregate the futures for all the images into a single future which will succeed if they
        // all succeed, or fail if any of them fail, then wire that up to our library result
        return RFuture.sequence(atlasImages).map(new Function<List<Image>,Library>() {
            public Library apply (List<Image> atlases) {
                return new Library(frameRate, movies, textures);
            }
        });
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

    protected static Texture.Symbol decodeTexture (Json.Object json, playn.core.Texture atlas) {
        Json.TypedArray<Float> rect = json.getArray("rect", Float.class);
        return new Texture.Symbol(
            json.getString("symbol"), getPoint(json, "origin", 0, 0),
            atlas.tile(rect.get(0), rect.get(1), rect.get(2), rect.get(3)));
    }

    protected static IPoint getPoint (Json.Object json, String field, float defX, float defY) {
        Json.TypedArray<Float> array = json.getArray(field, Float.class);
        return (array != null) ? new Point(array.get(0), array.get(1)) : new Point(defX, defY);
    }
}
