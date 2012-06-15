//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import java.util.Random;

import playn.core.CanvasImage;
import playn.core.Game;
import playn.core.Mouse;
import playn.core.PlayN;
import playn.java.JavaPlatform;
import static playn.core.PlayN.*;

import pythagoras.f.Vector;

import tripleplay.particle.effect.Gravity;
import tripleplay.particle.effect.Move;
import tripleplay.particle.init.Lifespan;
import tripleplay.particle.init.Transform;
import tripleplay.particle.init.Velocity;
import tripleplay.util.Randoms;

public class ParticleDemo implements Game
{
    public interface Test {
        Emitter create (Particles parts, Randoms rando);
    }

    public static Test[] TESTS = {
        new Test() { public Emitter create (Particles parts, Randoms rando) {
            CanvasImage image = graphics().createImage(5, 5);
            image.canvas().setFillColor(0xFFFFCC99);
            image.canvas().fillRect(0, 0, 5, 5);

            Emitter emitter = parts.createEmitter(5000, image);
            emitter.generator = Generator.constant(1000);
            emitter.initters.add(Lifespan.constant(5));
            emitter.initters.add(Transform.emitter(emitter));
            emitter.initters.add(Velocity.random(rando, -20, 20, -100, 0));
            emitter.effectors.add(new Gravity(30));
            emitter.effectors.add(new Move());
            emitter.layer.setTranslation(graphics().width()/2, graphics().height()/2);
            return emitter;
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
        if (_emitter != null) _emitter.destroy();
        _testIdx = (_testIdx + TESTS.length + dt) % TESTS.length;
        graphics().rootLayer().clear();
        _emitter = TESTS[_testIdx].create(_parts, _rando);
        graphics().rootLayer().add(_emitter.layer);
    }

    protected final Randoms _rando = Randoms.with(new Random());
    protected final Particles _parts = new Particles();

    protected int _testIdx;
    protected Emitter _emitter;
}
