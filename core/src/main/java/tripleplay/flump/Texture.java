//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import pythagoras.f.IPoint;

import playn.core.Image;
import playn.core.ImageLayer;
import playn.core.util.Clock;
import static playn.core.PlayN.*;

public class Texture
    implements Instance
{
    public static class Symbol
        implements tripleplay.flump.Symbol
    {
        public Image.Region region;
        public final IPoint origin;

        public Symbol (String name, IPoint origin, Image.Region region) {
            _name = name;
            this.origin = origin;
            this.region = region;
        }

        @Override public String name () {
            return _name;
        }

        @Override public Texture createInstance () {
            return new Texture(this);
        }

        protected String _name;
    }

    protected Texture (Symbol symbol) {
        _layer = graphics().createImageLayer(symbol.region);
        _layer.setOrigin(symbol.origin.x(), symbol.origin.y());
        _symbol = symbol;
    }

    @Override public ImageLayer layer () {
        return _layer;
    }

    public Symbol symbol () {
        return _symbol;
    }

    @Override public void paint (Clock clock) {
    }

    @Override public void paint (float dt) {
    }

    @Override public void destroy () {
        _layer.destroy();
    }

    protected ImageLayer _layer;

    protected Symbol _symbol;
}
