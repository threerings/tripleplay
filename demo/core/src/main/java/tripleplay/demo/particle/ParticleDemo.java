//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static playn.core.PlayN.graphics;

import tripleplay.particle.Emitter;
import tripleplay.particle.Particles;
import tripleplay.ui.Background;
import tripleplay.ui.Group;
import tripleplay.ui.Label;
import tripleplay.ui.Style;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.util.Randoms;

import tripleplay.demo.DemoScreen;

public abstract class ParticleDemo extends DemoScreen
{
    @Override public void showTransitionCompleted () {
        super.showTransitionCompleted();
        if (graphics().ctx() != null) {
            createParticles(_parts, _rando);
        }
    }

    @Override public void hideTransitionStarted () {
        super.hideTransitionStarted();
        for (Emitter emitter : _emitters) emitter.destroy();
        _emitters.clear();
    }

    @Override public void update (float delta) {
        super.update(delta);
        _parts.update(delta);
    }

    @Override protected Group createIface () {
        Group group = new Group(AxisLayout.vertical(),
                                Style.BACKGROUND.is(Background.solid(0xFF000000)));
        if (graphics().ctx() == null) {
            group.add(new Label("Particles are only supported with GL/WebGL.").
                      addStyles(Style.COLOR.is(0xFFFFFFFF)));
        }
        return group;
    }

    protected abstract void createParticles (Particles parts, Randoms rando);

    protected void note (Emitter emitter) {
        emitter.layer.setDepth(1);
        _emitters.add(emitter);
    }

    protected final Randoms _rando = Randoms.with(new Random());
    protected final Particles _parts = new Particles();

    protected int _testIdx;
    protected List<Emitter> _emitters = new ArrayList<Emitter>();
}
