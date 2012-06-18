//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.particle;

import react.UnitSlot;

import playn.core.CanvasImage;
import static playn.core.PlayN.graphics;

import tripleplay.particle.Emitter;
import tripleplay.particle.Generator;
import tripleplay.particle.Particles;
import tripleplay.particle.effect.Gravity;
import tripleplay.particle.effect.Move;
import tripleplay.particle.init.Color;
import tripleplay.particle.init.Lifespan;
import tripleplay.particle.init.Transform;
import tripleplay.particle.init.Velocity;
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

    @Override protected void createParticles (Particles parts, final Randoms rando) {
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
        note(explode);
    }
}
