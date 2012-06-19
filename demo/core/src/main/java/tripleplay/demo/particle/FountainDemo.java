//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.particle;

import playn.core.CanvasImage;
import static playn.core.PlayN.graphics;

import tripleplay.particle.Emitter;
import tripleplay.particle.Generator;
import tripleplay.particle.Particles;
import tripleplay.particle.effect.Alpha;
import tripleplay.particle.effect.Gravity;
import tripleplay.particle.effect.Move;
import tripleplay.particle.init.Color;
import tripleplay.particle.init.Lifespan;
import tripleplay.particle.init.Transform;
import tripleplay.particle.init.Velocity;
import tripleplay.util.Interpolator;
import tripleplay.util.Randoms;

/**
 * Does something extraordinary.
 */
public class FountainDemo extends ParticleDemo
{
    @Override protected String name () {
        return "Fountain";
    }
    @Override protected String title () {
        return "Particles: Fountain";
    }

    @Override protected void createParticles (Particles parts, Randoms rando) {
        CanvasImage image = graphics().createImage(7, 7);
        image.canvas().setFillColor(0xFFFFFFFF);
        image.canvas().fillCircle(3, 3, 3);

        Emitter emitter = parts.createEmitter(5000, image);
        emitter.generator = Generator.constant(100);
        emitter.initters.add(Lifespan.constant(5));
        emitter.initters.add(Color.constant(0xFF99CCFF));
        emitter.initters.add(Transform.layer(emitter.layer));
        emitter.initters.add(Velocity.randomSquare(rando, -20, 20, -100, 0));
        emitter.effectors.add(new Gravity(30));
        emitter.effectors.add(new Move());
        emitter.effectors.add(Alpha.byAge(Interpolator.EASE_OUT, 1, 0));
        emitter.layer.setTranslation(width()/2, height()/2);
        note(emitter);
    }
}
