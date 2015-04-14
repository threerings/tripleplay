//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import tripleplay.particle.Emitter;
import tripleplay.particle.ParticleBatch;
import tripleplay.ui.Background;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Root;
import tripleplay.ui.Style;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.util.Randoms;

import tripleplay.demo.DemoScreen;

public abstract class ParticleDemo extends DemoScreen
{
    @Override public void showTransitionCompleted () {
        super.showTransitionCompleted();
        ParticleBatch batch = new ParticleBatch(graphics().gl);
        closeOnHide(batch);
        createParticles(batch, _rando);
    }

    @Override public void hideTransitionStarted () {
        super.hideTransitionStarted();
        for (Emitter emitter : _emitters) emitter.layer.close();
        _emitters.clear();
    }

    @Override protected Group createIface (Root root) {
        return new Group(AxisLayout.vertical(), Style.BACKGROUND.is(Background.solid(0xFF000000)));
    }

    protected abstract void createParticles (ParticleBatch batch, Randoms rando);

    protected void add (Emitter emitter) {
        layer.add(emitter.layer.setDepth(1));
        _emitters.add(emitter);
    }

    protected final Randoms _rando = Randoms.with(new Random());

    protected int _testIdx;
    protected List<Emitter> _emitters = new ArrayList<Emitter>();
}
