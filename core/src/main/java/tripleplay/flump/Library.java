//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import playn.core.Disposable;
import playn.core.Graphics;
import playn.core.Image;
import playn.core.QuadBatch;
import playn.core.Tile;

import tripleplay.util.TexturePacker;

public class Library
{
    /** The original frame rate of movies in this library. */
    public final float frameRate;

    /** The symbols defined in this library. */
    public final Map<String,Symbol> symbols;

    public Library (
        float frameRate, Iterable<Movie.Symbol> movies, Iterable<Texture.Symbol> textures) {
        this.frameRate = frameRate;

        // map all of our movies and textures by symbol name
        final Map<String,Symbol> symbols = new HashMap<String,Symbol>();
        this.symbols = Collections.unmodifiableMap(symbols);
        for (Movie.Symbol movie : movies) symbols.put(movie.name(), movie);
        for (Texture.Symbol texture : textures) symbols.put(texture.name(), texture);

        // go through and resolve references
        for (Movie.Symbol movie : movies) {
            for (LayerData layer : movie.layers) {
                for (KeyframeData kf : layer.keyframes) {
                    if (kf._symbolName != null) {
                        Symbol symbol = symbols.get(kf._symbolName);
                        assert symbol != null;
                        if (layer._lastSymbol == null) layer._lastSymbol = symbol;
                        else if (layer._lastSymbol != symbol) layer._multipleSymbols = true;
                        kf._symbol = symbol;
                    }
                }
            }
        }
    }

    /** Pack multiple libraries into a single group of atlases.
      * The libraries will be modified so that their symbols point at the new atlases.
      *
      * @param batch the quad batch to use to render into new atlas textures. This will usually be
      * your game's default batch unless you're doing something fancy.
      */
    public static void pack (Graphics gfx, QuadBatch batch, Collection<Library> libs) {
        List<Library> list = new ArrayList<Library>(libs);

        // Add all texture symbols to the packer and note them for destruction
        TexturePacker packer = new TexturePacker();
        Set<Disposable> originals = new HashSet<>();
        for (int ii = 0, ll = list.size(); ii < ll; ++ii) {
            Library lib = list.get(ii);
            for (Symbol symbol : lib.symbols.values()) {
                if (symbol instanceof Texture.Symbol) {
                    Tile tile = ((Texture.Symbol)symbol).tile;
                    packer.add(ii+":"+symbol.name(), tile);
                    originals.add(tile.texture());
                }
            }
        }

        // Pack and update all texture symbols to the new regions
        Map<String,Tile> tiles = packer.pack(gfx, batch);
        for (int ii = 0, ll = list.size(); ii < ll; ++ii) {
            Library lib = list.get(ii);
            for (Symbol symbol : lib.symbols.values()) {
                if (symbol instanceof Texture.Symbol) {
                    ((Texture.Symbol)symbol).tile = tiles.get(ii+":"+symbol.name());
                }
            }
        }

        // Finally dispose all of the original textures, they're no longer needed
        for (Disposable tex : originals) tex.close();
    }

    /** Creates an instance of a symbol, or throws if the symbol name is not in this library. */
    public Instance createInstance (String symbolName) {
        Symbol symbol = symbols.get(symbolName);
        if (symbol == null) {
            throw new IllegalArgumentException("Missing required symbol [name=" + symbolName + "]");
        }
        return symbol.createInstance();
    }

    public Movie createMovie (String symbolName) {
        return (Movie)createInstance(symbolName);
    }

    public Texture createTexture (String symbolName) {
        return (Texture)createInstance(symbolName);
    }
}
