//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

import playn.core.Image;
import playn.core.PlayN;
import playn.core.util.Callback;

import react.Value;


/** Loads our flump library from a binary representation. */
public class BinaryFlumpLoader
{
    /**
     * Loads a binary encoded library synchronously via PlayN assets.
     */
    public static Library loadLibrarySync (String baseDir) throws Exception {
        byte[] bytes = PlayN.assets().getBytesSync(baseDir + "/library.bin");
        LibraryData data = new LibraryData(new DataInputStream(new ByteArrayInputStream(bytes)));
        return decodeLibrarySync(data, baseDir);
    }

    /**
     * Loads a binary encoded library via PlayN assets.
     * @param baseDir The base directory, containing library.bin and texture atlases.
     */
    public static void loadLibrary (final String baseDir, final Callback<Library> callback) {
        assert callback != null;
        PlayN.assets().getBytes(baseDir + "/library.bin", new Callback.Chain<byte[]>(callback) {
            public void onSuccess (byte[] bytes) {
                try {
                    LibraryData libData =
                        new LibraryData(new DataInputStream(new ByteArrayInputStream(bytes)));
                    decodeLibraryAsync(libData, baseDir, callback);
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
    protected static Library decodeLibrarySync (LibraryData libData, String baseDir)
    {
        final Library[] libs = new Library[]{null};
        decodeLibrary(libData, baseDir, new Callback<Library>() {
            public void onSuccess (Library result) {libs[0] = result;}
            public void onFailure(Throwable cause) {}
        }, new ImageLoader() {
            @Override public Image load (String path) {
                return PlayN.assets().getImageSync(path);
            }
        });
        return libs[0];
    }

    /**
     * Decodes and returns a library asynchronously.
     */
    protected static void decodeLibraryAsync (LibraryData libData, String baseDir,
                                              Callback<Library> callback)
    {
        decodeLibrary(libData, baseDir, callback, new ImageLoader() {
            @Override public Image load (String path) {
                return PlayN.assets().getImage(path);
            }
        });
    }

    /**
     * Generic library decoding method.
     */
    protected static void decodeLibrary (LibraryData libData, String baseDir,
                                         final Callback<Library> callback,
                                         final ImageLoader imageLoader)
    {
        final float frameRate = libData.frameRate;
        final ArrayList<Movie.Symbol> movies = new ArrayList<Movie.Symbol>();
        for (LibraryData.MovieData movieData : libData.movies) {
            movies.add(decodeMovie(frameRate, movieData));
        }

        final ArrayList<Texture.Symbol> textures = new ArrayList<Texture.Symbol>();

        List<LibraryData.AtlasData> atlases = libData.atlases;

        final Value<Integer> remainingAtlases = Value.create(atlases.size());
        remainingAtlases.connectNotify(new Value.Listener<Integer>() {
            @Override public void onChange (Integer remaining, Integer unused) {
                if (remaining == 0) callback.onSuccess(new Library(frameRate, movies, textures));
            }
        });

        for (final LibraryData.AtlasData atlasData : atlases) {
            Image atlas = imageLoader.load(baseDir + "/" + atlasData.file);
            atlas.addCallback(new Callback.Chain<Image>(callback) {
                public void onSuccess (Image atlas) {
                    for (LibraryData.TextureData textureData : atlasData.textures) {
                        textures.add(decodeTexture(textureData, atlas));
                    }
                    remainingAtlases.update(remainingAtlases.get() - 1);
                }
            });
        }
    }

    protected static Movie.Symbol decodeMovie (float frameRate, LibraryData.MovieData movieData) {
        String name = movieData.id;
        ArrayList<LayerData> layers = new ArrayList<LayerData>();
        for (LibraryData.LayerData layerData : movieData.layers) {
            layers.add(decodeLayerData(layerData));
        }
        return new Movie.Symbol(frameRate, name, layers);
    }

    protected static LayerData decodeLayerData (LibraryData.LayerData layerData) {
        String name = layerData.name;
        ArrayList<KeyframeData> keyframes = new ArrayList<KeyframeData>();
        KeyframeData prevKf = null;
        for (LibraryData.KeyframeData keyframeData : layerData.keyframes) {
            prevKf = decodeKeyframeData(keyframeData, prevKf);
            keyframes.add(prevKf);
        }
        return new LayerData(name, keyframes);
    }

    protected static KeyframeData decodeKeyframeData (LibraryData.KeyframeData kfData,
            KeyframeData prevKf) {
        return new KeyframeData((prevKf != null) ? prevKf.index + prevKf.duration : 0,
                                kfData.duration, kfData.label,
                                kfData.loc, kfData.scale,
                                kfData.skew, kfData.pivot,
                                kfData.visible, kfData.alpha,
                                kfData.tweened, kfData.ease,
                                kfData.ref);
    }

    protected static Texture.Symbol decodeTexture (LibraryData.TextureData textureData,
            Image atlas) {
        float[] rect = textureData.rect;
        return new Texture.Symbol(
            textureData.symbol, textureData.origin,
            atlas.subImage(rect[0], rect[1], rect[2], rect[3]));
    }
}
