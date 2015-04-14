//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.List;

import react.RFuture;
import react.RPromise;
import react.Slot;
import react.Value;

import playn.core.Image;
import playn.core.Platform;

/** Loads our flump library from a binary representation. */
public class BinaryFlumpLoader
{
    /**
     * Loads a binary encoded library synchronously via PlayN assets.
     */
    public static Library loadLibrarySync (Platform plat, String baseDir) throws Exception {
        byte[] bytes = plat.assets().getBytesSync(baseDir + "/library.bin");
        LibraryData data = new LibraryData(new DataInputStream(new ByteArrayInputStream(bytes)));
        return decodeLibrarySync(plat, data, baseDir);
    }

    /**
     * Loads a binary encoded library via PlayN assets.
     * @param baseDir The base directory, containing library.bin and texture atlases.
     */
    public static RFuture<Library> loadLibrary (final Platform plat, final String baseDir) {
        final RPromise<Library> result = RPromise.create();
        plat.assets().getBytes(baseDir + "/library.bin").onSuccess(new Slot<byte[]>() {
            public void onEmit (byte[] bytes) {
                try {
                    LibraryData libData = new LibraryData(
                        new DataInputStream(new ByteArrayInputStream(bytes)));
                    decodeLibraryAsync(plat, libData, baseDir, result);
                } catch (Exception err) {
                    result.fail(err);
                }
            }
        });
        return result;
    }

    /** Helper interface to load an image from a path. */
    protected static interface ImageLoader {
        public Image load (String path);
    }

    /**
     * Decodes and returns a library synchronously.
     */
    protected static Library decodeLibrarySync (final Platform plat, LibraryData libData,
                                                String baseDir) {
        RPromise<Library> result = RPromise.create();
        decodeLibrary(libData, baseDir, result, new ImageLoader() {
            @Override public Image load (String path) {
                return plat.assets().getImageSync(path);
            }
        });

        // this blows, but I don't want to add RPromise.get()
        final Library[] out = new Library[1];
        result.onSuccess(new Slot<Library>() {
            public void onEmit (Library library) { out[0] = library; }
        });
        assert out[0] != null;
        return out[0];
    }

    /**
     * Decodes and returns a library asynchronously.
     */
    protected static void decodeLibraryAsync (final Platform plat, LibraryData libData,
                                              String baseDir, RPromise<Library> result) {
        decodeLibrary(libData, baseDir, result, new ImageLoader() {
            @Override public Image load (String path) {
                return plat.assets().getImage(path);
            }
        });
    }

    /**
     * Generic library decoding method.
     */
    protected static void decodeLibrary (LibraryData libData, String baseDir,
                                         final RPromise<Library> result, ImageLoader imageLoader)
    {
        final float frameRate = libData.frameRate;
        final ArrayList<Movie.Symbol> movies = new ArrayList<Movie.Symbol>();
        for (LibraryData.MovieData movieData : libData.movies) {
            movies.add(decodeMovie(frameRate, movieData));
        }

        final ArrayList<Texture.Symbol> textures = new ArrayList<Texture.Symbol>();

        // trigger the loading of all of the atlas images
        List<RFuture<Image>> atlasImages = new ArrayList<RFuture<Image>>();
        for (final LibraryData.AtlasData atlasData : libData.atlases) {
            Image atlas = imageLoader.load(baseDir + "/" + atlasData.file);
            atlasImages.add(atlas.state);
            atlas.state.onSuccess(new Slot<Image>() {
                public void onEmit (Image image) {
                    for (LibraryData.TextureData textureData : atlasData.textures) {
                        textures.add(decodeTexture(textureData, image.texture()));
                    }
                }
            });
        }

        // aggregate the futures for all the images into a single future which will succeed if they
        // all succeed, or fail if any of them fail, then wire that up to our library result
        RFuture.sequence(atlasImages).onSuccess(new Slot<List<Image>>() {
            public void onEmit (List<Image> atlases) {
                result.succeed(new Library(frameRate, movies, textures));
            }
        }).onFailure(result.failer());
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

    protected static Texture.Symbol decodeTexture (LibraryData.TextureData tdata,
                                                   playn.core.Texture atlas) {
        float[] r = tdata.rect;
        return new Texture.Symbol(tdata.symbol, tdata.origin, atlas.tile(r[0], r[1], r[2], r[3]));
    }
}
