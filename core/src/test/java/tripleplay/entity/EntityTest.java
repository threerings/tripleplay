//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.entity;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import playn.core.Clock;

import tripleplay.entity.Component;
import tripleplay.entity.Entity;
import tripleplay.entity.World;

public class EntityTest {

    class TestWorld extends World {
        public final Component.IScalar comp = new Component.IScalar(this);
        public final TestSystem sys = new TestSystem(this);
        class TestSystem extends System {
            public int entitiesUpdated = 0;
            public TestSystem (World world) { super(world, 0); }
            @Override protected boolean isInterested (Entity entity) {
                return entity.has(comp);
            }
            @Override protected void update (Clock clock, Entities entities) {
                entitiesUpdated = entities.size();
            }
        }
    }

    @Test public void testUpdate () {
        TestWorld world = new TestWorld();
        Entity e = world.create(true).add(world.comp);
        world.update(null);
        assertEquals(1, world.sys.entitiesUpdated);
    }
}
