//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import playn.core.Asserts;
import playn.core.Image;

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
        for (Movie.Symbol movie : movies) {
            symbols.put(movie.name(), movie);
        }
        for (Texture.Symbol texture : textures) {
            symbols.put(texture.name(), texture);
        }

        // go through and resolve references
        for (Movie.Symbol movie : movies) {
            for (LayerData layer : movie.layers) {
                for (KeyframeData kf : layer.keyframes) {
                    if (kf._symbolName != null) {
                        Symbol symbol = symbols.get(kf._symbolName);
                        Asserts.checkNotNull(symbol);

                        if (layer._lastSymbol == null) layer._lastSymbol = symbol;
                        else if (layer._lastSymbol != symbol) layer._multipleSymbols = true;
                        kf._symbol = symbol;
                    }
                }
            }
        }
    }

    /** Pack multiple libraries into a single group of atlases. The libraries will be modified so
     * that their symbols point at the new atlases. */
    public static void pack (Collection<Library> libs) {
        List<Library> list = new ArrayList<Library>(libs);

        // Add all texture symbols to the packer
        TexturePacker packer = new TexturePacker();
        for (int ii = 0, ll = list.size(); ii < ll; ++ii) {
            Library lib = list.get(ii);
            for (Symbol symbol : lib.symbols.values()) {
                if (symbol instanceof Texture.Symbol) {
                    packer.add(ii+":"+symbol.name(), ((Texture.Symbol)symbol).region);
                }
            }
        }

        // Pack and update all texture symbols to the new regions
        Map<String,Image.Region> images = packer.pack();
        for (int ii = 0, ll = list.size(); ii < ll; ++ii) {
            Library lib = list.get(ii);
            for (Symbol symbol : lib.symbols.values()) {
                if (symbol instanceof Texture.Symbol) {
                    ((Texture.Symbol)symbol).region = images.get(ii+":"+symbol.name());
                }
            }
        }
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
