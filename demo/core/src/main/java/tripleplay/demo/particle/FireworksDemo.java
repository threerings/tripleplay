//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.particle;

import react.UnitSlot;

import playn.core.Canvas;
import playn.core.Tile;

import tripleplay.particle.Emitter;
import tripleplay.particle.Generator;
import tripleplay.particle.ParticleBatch;
import tripleplay.particle.effect.Alpha;
import tripleplay.particle.effect.Drag;
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
public class FireworksDemo extends ParticleDemo
{
    @Override protected String name () {
        return "Fireworks";
    }
    @Override protected String title () {
        return "Particles: Fireworks";
    }

    @Override protected void createParticles (ParticleBatch batch, final Randoms rando) {
        Canvas image = graphics().createCanvas(2, 2);
        image.setFillColor(0xFFFFFFFF).fillRect(0, 0, 2, 2);
        Tile tile = image.toTexture();

        final Emitter explode1 = createEmitter(batch, rando, tile, 0xFFFFCD82, 0.975f);
        final Emitter explode2 = createEmitter(batch, rando, tile, 0xFFF06969, 0.95f);
        add(explode1);
        add(explode2);

        float tx = 100 + rando.getFloat(size().width()-200);
        float ty = 100 + rando.getFloat(size().height()-200);
        explode1.layer.setTranslation(tx, ty);
        explode2.layer.setTranslation(tx, ty);

        explode1.onEmpty.connect(new UnitSlot() {
          @Override public void onEmit () {
            float tx = 100 + rando.getFloat(size().width()-200);
            float ty = 100 + rando.getFloat(size().height()-200);
            explode1.layer.setTranslation(tx, ty);
            explode2.layer.setTranslation(tx, ty);
            explode1.generator = Generator.impulse(200);
            explode2.generator = Generator.impulse(200);
        }});
    }

    protected Emitter createEmitter (ParticleBatch batch, Randoms rando, Tile tile,
                                     int color, float drag) {
        final Emitter explode = new Emitter(batch, paint, 200, tile);
        explode.generator = Generator.impulse(200);
        explode.initters.add(Lifespan.random(rando, 1, 1.5f));
        explode.initters.add(Color.constant(color));
        // explode.initters.add(Color.constant(0xFFF06969));
        explode.initters.add(Transform.layer(explode.layer));
        explode.initters.add(Velocity.randomNormal(rando, 0, 70));
        explode.initters.add(Velocity.increment(0, 10));
        explode.effectors.add(Alpha.byAge(Interpolator.EASE_IN));
        explode.effectors.add(new Gravity(30));
        // explode.effectors.add(new Drag(0.95f));
        explode.effectors.add(new Drag(drag));
        explode.effectors.add(new Move());
        return explode;
    }
}
