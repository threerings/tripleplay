//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import react.Functions;
import react.RFuture;

import playn.core.Surface;
import playn.core.TileSource;
import playn.scene.GroupLayer;
import playn.scene.ImageLayer;
import playn.scene.Layer;

/**
 * Contains icon related utility classes and methods, mostly basic icon factories.
 */
public class Icons
{
    /**
     * Defers to another icon. Subclasses decide how to modify the width and height and how to use
     * the rendered layer. The base takes care of the callback. By default, returns the size and
     * layer without modification.
     */
    public abstract static class Aggregated implements Icon
    {
        /** Icon that is deferred to. */
        public final Icon icon;

        /** Creates a new aggregated icon that defers to the given one. */
        public Aggregated (Icon icon) {
            this.icon = icon;
        }

        @Override public float width () { return icon.width(); }
        @Override public float height () { return icon.height(); }
        @Override public Layer render () { return icon.render(); }
        @Override public RFuture<Icon> state () { return icon.state(); }
    }

    /** Creates an icon using the supplied texture tile {@code source}. */
    public static Icon image (final TileSource source) {
        return new Icon() {
            @Override public float width () {
                return source.isLoaded() ? source.tile().width() : 0;
            }
            @Override public float height () {
                return source.isLoaded() ? source.tile().height() : 0;
            }
            @Override public Layer render () { return new ImageLayer(source); }
            @Override public RFuture<Icon> state () {
                return source.tileAsync().map(Functions.constant((Icon)this));
            }
        };
    }

    /**
     * Creates an icon that applies the given scale to the given icon.
     */
    public static Icon scaled (Icon icon, final float scale) {
        return new Aggregated(icon) {
            @Override public float width () { return super.width() * scale; }
            @Override public float height () { return super.height() * scale; }
            @Override public Layer render () { return super.render().setScale(scale); }
        };
    }

    /**
     * Creates an icon that nests and offsets the given icon by the given translation.
     */
    public static Icon offset (Icon icon, final float tx, final float ty) {
        return new Aggregated(icon) {
            @Override public Layer render () {
                GroupLayer layer = new GroupLayer();
                layer.addAt(super.render(), tx, ty);
                return layer;
            }
        };
    }

    /**
     * Creates a solid square icon of the given size.
     */
    public static Icon solid (final int color, final float size) {
        return new Icon() {
            @Override public float width () { return size; }
            @Override public float height () { return size; }
            @Override public RFuture<Icon> state () { return RFuture.<Icon>success(this); }
            @Override public Layer render () {
                return new Layer() {
                    @Override protected void paintImpl (Surface surf) {
                        surf.setFillColor(color).fillRect(0, 0, size, size);
                    }
                };
            }
        };
    }
}
