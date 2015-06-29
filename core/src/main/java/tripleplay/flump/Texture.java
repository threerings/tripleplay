//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import pythagoras.f.IPoint;

import playn.core.Tile;
import playn.scene.ImageLayer;

public class Texture implements Instance
{
    public static class Symbol implements tripleplay.flump.Symbol
    {
        public Tile tile;
        public final IPoint origin;

        public Symbol (String name, IPoint origin, Tile tile) {
            _name = name;
            this.origin = origin;
            setSource(tile);
        }

        public void setSource (Tile tile) {
            this.tile = tile;
        }

        @Override public String name () { return _name; }
        @Override public Texture createInstance () { return new Texture(this); }

        protected final String _name;
    }

    protected Texture (Symbol symbol) {
        _layer = new ImageLayer(symbol.tile);
        _layer.setOrigin(symbol.origin.x(), symbol.origin.y());
        _symbol = symbol;
    }

    public Symbol symbol () { return _symbol; }

    @Override public ImageLayer layer () { return _layer; }
    @Override public void paint (float dt) {} // nada
    @Override public void close () { _layer.close(); }

    protected final ImageLayer _layer;
    protected final Symbol _symbol;
}
