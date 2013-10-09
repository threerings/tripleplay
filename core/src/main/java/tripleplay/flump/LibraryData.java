//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import pythagoras.f.Point;

import playn.core.Json;
import playn.core.PlayN;

/**
 * Captures the data of a flump library. This allows us to convert between Json and other formats,
 *  including raw bytes.
 */
public class LibraryData
{
    public float frameRate;
    public List<MovieData> movies;
    public List<AtlasData> atlases;

    public static class AtlasData
    {
        public List<TextureData> textures;
        public String file;

        public AtlasData (Json.Object json) {
            Json.Array textureArr = json.getArray("textures");
            textures = Lists.newArrayList();
            if (textureArr != null) {
                for (int ii = 0; ii < textureArr.length(); ++ii) {
                    textures.add(new TextureData(textureArr.getObject(ii)));
                }
            }
            file = json.getString("file");
        }

        public Json.Object toJson ()
        {
            Json.Object json = PlayN.json().createObject();
            Json.Array textureArr = PlayN.json().createArray();
            for (TextureData texture : textures) {
                textureArr.add(texture.toJson());
            }
            json.put("textures", textureArr);
            json.put("file", file);

            return json;
        }

        public AtlasData (DataInputStream istream) throws IOException {
            textures = Lists.newArrayList();
            int numTextures = istream.readInt();
            for (int ii = 0; ii < numTextures; ++ii) {
                textures.add(new TextureData(istream));
            }
            file = istream.readUTF();
        }

        public void write (DataOutputStream ostream) throws IOException {
            ostream.writeInt(textures.size());
            for (TextureData texture : textures) {
                texture.write(ostream);
            }
            ostream.writeUTF(file);
        }
    }

    public static class TextureData
    {
        public String symbol;
        public Point origin;
        public float[] rect;

        public TextureData (Json.Object json) {
            symbol = json.getString("symbol");
            origin = getPoint(json, "origin", 0, 0);
            Json.TypedArray<Float> rectArr = json.getArray("rect", Float.class);
            rect = new float[] { rectArr.get(0), rectArr.get(1), rectArr.get(2), rectArr.get(3) };
        }

        public Json.Object toJson () {
            Json.Object json = PlayN.json().createObject();
            json.put("symbol", symbol);
            json.put("origin", fromPoint(origin));
            Json.Array rectArr = PlayN.json().createArray();
            rectArr.add(rect[0]);
            rectArr.add(rect[1]);
            rectArr.add(rect[2]);
            rectArr.add(rect[3]);
            json.put("rect", rectArr);

            return json;
        }

        public TextureData (DataInputStream istream) throws IOException {
            symbol = istream.readUTF();
            origin = new Point(istream.readFloat(), istream.readFloat());
            rect = new float[] { istream.readFloat(), istream.readFloat(), istream.readFloat(),
                istream.readFloat() };
        }

        public void write (DataOutputStream ostream) throws IOException {
            ostream.writeUTF(symbol);
            ostream.writeFloat(origin.x);
            ostream.writeFloat(origin.y);
            ostream.writeFloat(rect[0]);
            ostream.writeFloat(rect[1]);
            ostream.writeFloat(rect[2]);
            ostream.writeFloat(rect[3]);
        }
    }

    public static class MovieData
    {
        public String id;
        public List<LayerData> layers;

        public MovieData (Json.Object json) {
            id = json.getString("id");
            Json.Array layerArr = json.getArray("layers");
            layers = Lists.newArrayList();
            if (layerArr != null) {
                for (int ii = 0; ii < layerArr.length(); ++ii) {
                    layers.add(new LayerData(layerArr.getObject(ii)));
                }
            }
        }

        public Json.Object toJson ()
        {
            Json.Object json = PlayN.json().createObject();
            json.put("id", id);
            Json.Array layerArr = PlayN.json().createArray();
            for (LayerData layer : layers) {
                layerArr.add(layer.toJson());
            }
            json.put("layers", layerArr);

            return json;
        }

        public MovieData (DataInputStream istream) throws IOException {
            id = istream.readUTF();
            layers = Lists.newArrayList();
            int numLayers = istream.readInt();
            for (int ii = 0; ii < numLayers; ++ii) {
                layers.add(new LayerData(istream));
            }
        }

        public void write (DataOutputStream ostream) throws IOException {
            ostream.writeUTF(id);
            ostream.writeInt(layers.size());
            for (LayerData layer : layers) {
                layer.write(ostream);
            }
        }
    }

    public static class LayerData
    {
        public String name;
        public List<KeyframeData> keyframes;

        public LayerData (Json.Object json) {
            name = json.getString("name");
            Json.Array keyframeArr = json.getArray("keyframes");
            keyframes = Lists.newArrayList();
            if (keyframeArr != null) {
                for (int ii = 0; ii < keyframeArr.length(); ++ii) {
                    keyframes.add(new KeyframeData(keyframeArr.getObject(ii)));
                }
            }
        }

        public Json.Object toJson ()
        {
            Json.Object json = PlayN.json().createObject();
            json.put("name", name);
            Json.Array keyframeArr = PlayN.json().createArray();
            for (KeyframeData frame : keyframes) {
                keyframeArr.add(frame.toJson());
            }
            json.put("keyframes", keyframeArr);

            return json;
        }

        public LayerData (DataInputStream istream) throws IOException {
            name = istream.readUTF();
            keyframes = Lists.newArrayList();
            int numKeyframes = istream.readInt();
            for (int ii = 0; ii < numKeyframes; ++ii) {
                keyframes.add(new KeyframeData(istream));
            }
        }

        public void write (DataOutputStream ostream) throws IOException {
            ostream.writeUTF(name);
            ostream.writeInt(keyframes.size());
            for (KeyframeData keyframe : keyframes) {
                keyframe.write(ostream);
            }
        }
    }

    public static class KeyframeData
    {
        public int duration;
        public String label;
        public Point loc;
        public Point scale;
        public Point skew;
        public Point pivot;
        public float alpha;
        public boolean visible;
        public boolean tweened;
        public float ease;
        public String ref;

        public KeyframeData (Json.Object json) {
            duration = json.getInt("duration");
            label = json.getString("label");

            loc = getPoint(json, "loc", 0, 0);
            scale = getPoint(json, "scale", 1, 1);
            skew = getPoint(json, "skew", 0, 0);
            pivot = getPoint(json, "pivot", 0, 0);
            alpha = json.getNumber("alpha", 1);
            visible = json.getBoolean("visible", true);
            tweened = json.getBoolean("tweened", true);
            ease = json.getNumber("ease", 0);

            ref = json.getString("ref");
        }

        public Json.Object toJson ()
        {
            Json.Object json = PlayN.json().createObject();
            json.put("duration", duration);
            json.put("label", label);
            json.put("loc", fromPoint(loc));
            json.put("scale", fromPoint(scale));
            json.put("skew", fromPoint(skew));
            json.put("pivot", fromPoint(pivot));
            json.put("alpha", alpha);
            json.put("visible", visible);
            json.put("tweened", tweened);
            json.put("ease", ease);
            json.put("ref", ref);

            return json;
        }

        public KeyframeData (DataInputStream istream) throws IOException {
            duration = istream.readInt();
            if (istream.readBoolean()) {
                label = istream.readUTF();
            }

            loc = new Point(istream.readFloat(), istream.readFloat());
            scale = new Point(istream.readFloat(), istream.readFloat());
            skew = new Point(istream.readFloat(), istream.readFloat());
            pivot = new Point(istream.readFloat(), istream.readFloat());
            alpha = istream.readFloat();
            visible = istream.readBoolean();
            tweened = istream.readBoolean();
            ease = istream.readFloat();

            if (istream.readBoolean()) {
                ref = istream.readUTF();
            }
        }

        public void write (DataOutputStream ostream) throws IOException {
            ostream.writeInt(duration);
            ostream.writeBoolean(label != null);
            if (label != null) {
                ostream.writeUTF(label);
            }
            ostream.writeFloat(loc.x);
            ostream.writeFloat(loc.y);
            ostream.writeFloat(scale.x);
            ostream.writeFloat(scale.y);
            ostream.writeFloat(skew.x);
            ostream.writeFloat(skew.y);
            ostream.writeFloat(pivot.x);
            ostream.writeFloat(pivot.y);
            ostream.writeFloat(alpha);
            ostream.writeBoolean(visible);
            ostream.writeBoolean(tweened);
            ostream.writeFloat(ease);
            ostream.writeBoolean(ref != null);
            if (ref != null) {
                ostream.writeUTF(ref);
            }
        }
    }

    public LibraryData (Json.Object json)
    {
        Json.Array movieArr = json.getArray("movies");
        movies = Lists.newArrayList();
        if (movieArr != null) {
            for (int ii = 0; ii < movieArr.length(); ++ii) {
                movies.add(new MovieData(movieArr.getObject(ii)));
            }
        }
        Json.Array atlasArr = json.getArray("textureGroups").getObject(0).getArray("atlases");
        atlases = Lists.newArrayList();
        if (atlasArr != null) {
            for (int ii = 0; ii < atlasArr.length(); ++ii) {
                atlases.add(new AtlasData(atlasArr.getObject(ii)));
            }
        }
        frameRate = json.getNumber("frameRate");
    }

    public Json.Object toJson ()
    {
        Json.Object json = PlayN.json().createObject();
        Json.Array movieArr = PlayN.json().createArray();
        for (MovieData movie : movies) {
            movieArr.add(movie.toJson());
        }
        json.put("movies", movieArr);
        Json.Array atlasArr = PlayN.json().createArray();
        for (AtlasData atlas : atlases) {
            atlasArr.add(atlas.toJson());
        }
        Json.Array textureGroupsArr = PlayN.json().createArray();
        Json.Object textureGroupOne = PlayN.json().createObject();
        textureGroupOne.put("atlases", atlasArr);
        textureGroupsArr.add(textureGroupOne);
        json.put("textureGroups", textureGroupsArr);
        json.put("frameRate", frameRate);

        return json;
    }

    public LibraryData (DataInputStream istream)
        throws IOException
    {
        movies = Lists.newArrayList();
        int numMovies = istream.readInt();
        for (int ii = 0; ii < numMovies; ++ii) {
            movies.add(new MovieData(istream));
        }
        atlases = Lists.newArrayList();
        int numAtlases = istream.readInt();
        for (int ii = 0; ii < numAtlases; ++ii) {
            atlases.add(new AtlasData(istream));
        }
        frameRate = istream.readFloat();
    }

    public void write (DataOutputStream ostream)
        throws IOException
    {
        ostream.writeInt(movies.size());
        for (MovieData movie : movies) {
            movie.write(ostream);
        }
        ostream.writeInt(atlases.size());
        for (AtlasData atlas : atlases) {
            atlas.write(ostream);
        }
        ostream.writeFloat(frameRate);
    }

    protected static Point getPoint (Json.Object json, String field, float defX, float defY)
    {
        Json.TypedArray<Float> array = json.getArray(field, Float.class);
        return (array != null) ? new Point(array.get(0), array.get(1)) : new Point(defX, defY);
    }

    protected static Json.Array fromPoint (Point pt)
    {
        Json.Array arr = PlayN.json().createArray();
        arr.add(pt.x);
        arr.add(pt.y);
        return arr;
    }
}
