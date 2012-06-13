package tripleplay.flump;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import playn.core.Image;
import playn.core.Json;
import playn.core.Layer;
import playn.core.PlayN;
import playn.core.ResourceCallback;

public class Library
{
    /**
     * The original frame rate of movies in this library.
     */
    public final float frameRate;

    /**
     * The symbols defined in this library.
     */
    public final Map<String,Symbol> symbols;

    protected Library (Json.Object json, String baseDir) {
        frameRate = json.getNumber("frameRate");

        Map<String,Symbol> symbols = new HashMap<String,Symbol>();
        this.symbols = Collections.unmodifiableMap(symbols);

        ArrayList<Movie.Symbol> movies = new ArrayList<Movie.Symbol>();
        for (Json.Object movieJson : json.getArray("movies", Json.Object.class)) {
            Movie.Symbol movie = new Movie.Symbol(this, movieJson);
            movies.add(movie);
            symbols.put(movie.name(), movie);
        }

        for (Json.Object atlasJson : json.getArray("atlases", Json.Object.class)) {
            Image atlas = PlayN.assets().getImage(
                baseDir + "/" + atlasJson.getString("file"));
            for (Json.Object textureJson : atlasJson.getArray(
                    "textures", Json.Object.class)) {
                Texture.Symbol texture = new Texture.Symbol(textureJson, atlas);
                symbols.put(texture.name(), texture);
            }
        }

        // Now that all symbols have been parsed, go through and resolve references
        for (Movie.Symbol movie : movies) {
            for (LayerData layer : movie.layers) {
                for (KeyframeData kf : layer.keyframes) {
                    Symbol symbol = symbols.get(kf._symbolName);
                    if (symbol != null) {
                        if (layer._lastSymbol == null) {
                            layer._lastSymbol = symbol;
                        } else if (layer._lastSymbol != symbol) {
                            layer._multipleSymbols = true;
                        }
                        kf._symbol = symbol;
                    }
                }
            }
        }
    }

    public static void fromAssets (final String baseDir, final ResourceCallback<Library> callback) {
        PlayN.assets().getText(baseDir + "/library.json", new ResourceCallback<String>() {
            public void done (String text) {
                try {
                    callback.done(new Library(PlayN.json().parse(text), baseDir));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            public void error (Throwable cause) {
                callback.error(cause);
            }
        });
    }

    public Instance createInstance (String symbolName) {
        Symbol symbol = symbols.get(symbolName);
        if (symbol == null) {
            throw new RuntimeException("Missing required symbol [name=" + symbolName + "]");
        }
        return symbol.createInstance();
    }

    public Movie createMovie (String symbolName) {
        return (Movie)createInstance(symbolName);
    }
}
