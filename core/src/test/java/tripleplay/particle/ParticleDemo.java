//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pythagoras.f.Vector;

import react.UnitSlot;

import playn.core.CanvasImage;
import playn.core.Game;
import playn.core.Mouse;
import playn.core.PlayN;
import playn.java.JavaPlatform;
import static playn.core.PlayN.*;

import tripleplay.particle.effect.Alpha;
import tripleplay.particle.effect.Gravity;
import tripleplay.particle.effect.Move;
import tripleplay.particle.init.Color;
import tripleplay.particle.init.Lifespan;
import tripleplay.particle.init.Transform;
import tripleplay.particle.init.Velocity;
import tripleplay.util.Interpolator;
import tripleplay.util.Randoms;

public class ParticleDemo implements Game
{
    public interface Test {
        void create (Particles parts, Randoms rando, List<Emitter> emitters);
    }

    public static Test[] TESTS = {
        // a standard continuous fountain of particles; spew!
        new Test() { public void create (Particles parts, Randoms rando, List<Emitter> emitters) {
            CanvasImage image = graphics().createImage(7, 7);
            image.canvas().setFillColor(0xFFFFFFFF);
            image.canvas().fillCircle(3, 3, 3);

            Emitter emitter = parts.createEmitter(5000, image);
            emitter.generator = Generator.constant(100);
            emitter.initters.add(Lifespan.constant(5));
            emitter.initters.add(Color.constant(0xFF99CCFF));
            emitter.initters.add(Transform.layer(emitter.layer));
            emitter.initters.add(Velocity.random(rando, -20, 20, -100, 0));
            emitter.effectors.add(new Gravity(30));
            emitter.effectors.add(new Move());
            emitter.effectors.add(Alpha.byAge(Interpolator.EASE_OUT, 1, 0));
            emitter.layer.setTranslation(graphics().width()/2, graphics().height()/2);
            emitters.add(emitter);
        }},

        new Test() { public void create (Particles parts, final Randoms rando,
                                         List<Emitter> emitters) {
            CanvasImage image = graphics().createImage(5, 5);
            image.canvas().setFillColor(0xFFFFCC99);
            image.canvas().fillRect(0, 0, 5, 5);

            final Emitter explode = parts.createEmitter(500, image);
            explode.generator = Generator.impulse(500);
            explode.initters.add(Lifespan.random(rando, 1, 2f));
            explode.initters.add(Color.constant(0xFFFFFFFF));
            explode.initters.add(Transform.layer(explode.layer));
            explode.initters.add(Velocity.random(rando, 100));
            explode.initters.add(Velocity.increment(0, -50));
            explode.effectors.add(new Gravity(30));
            explode.effectors.add(new Move());
            explode.layer.setTranslation(100 + rando.getFloat(graphics().width()-200),
                                         100 + rando.getFloat(graphics().height()-200));

            explode.onEmpty.connect(new UnitSlot() { public void onEmit () {
                explode.layer.setTranslation(100 + rando.getFloat(graphics().width()-200),
                                             100 + rando.getFloat(graphics().height()-200));
                explode.generator = Generator.impulse(500);
            }});
            emitters.add(explode);
        }},
    };

    public static void main (String[] args) {
        JavaPlatform.register();
        PlayN.run(new ParticleDemo());
    }

    @Override // from interface Game
    public void init () {
        mouse().setListener(new Mouse.Adapter() {
            @Override public void onMouseDown(Mouse.ButtonEvent event) {
                if      (event.button() == Mouse.BUTTON_RIGHT) advanceTest(1);
                else if (event.button() == Mouse.BUTTON_MIDDLE) advanceTest(-1);
            }
        });
        advanceTest(0);
    }

    @Override // from interface Game
    public void update (float delta) {
        _parts.update(delta);
    }

    @Override // from interface Game
    public void paint (float alpha) {
        // nada
    }

    @Override // from interface Game
    public int updateRate () {
        return 16;
    }

    protected void advanceTest (int dt) {
        for (Emitter emitter : _emitters) emitter.destroy();
        _emitters.clear();
        _testIdx = (_testIdx + TESTS.length + dt) % TESTS.length;
        graphics().rootLayer().clear();
        TESTS[_testIdx].create(_parts, _rando, _emitters);
    }

    protected final Randoms _rando = Randoms.with(new Random());
    protected final Particles _parts = new Particles();

    protected int _testIdx;
    protected List<Emitter> _emitters = new ArrayList<Emitter>();
}
