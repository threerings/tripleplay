//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.particle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.*;
import static org.junit.Assert.*;

import tripleplay.util.Randoms;

public class ParticleBufferTest
{
    @Test
    public void testSparseApply () {
        int maxParts = 4096;
        ParticleBuffer buffer = new ParticleBuffer(maxParts);
        Randoms rando = Randoms.with(new Random());
        final Set<Integer> lives = new HashSet<Integer>();
        for (int ii = 0; ii < 100; ii++) {
            int idx = rando.getInt(maxParts);
            lives.add(idx);
            buffer.setAlive(idx, true);
        }

        final int[] applied = new int[] { 0 };
        buffer.apply(Collections.singletonList(new Effector() {
            @Override public void apply (int index, float[] data, int start, float now, float dt) {
                assertEquals(index, start/ParticleBuffer.NUM_FIELDS);
                assertTrue(lives.contains(index));
                applied[0] += 1;
            }
        }), 0, 0);
        assertEquals(lives.size(), applied[0]);
    }

    @Test
    public void testAddParticles () {
        int maxParts = 4096;
        ParticleBuffer buffer = new ParticleBuffer(maxParts);

        buffer.add(100, 0, new ArrayList<Initializer>());
        final int[] applied = new int[] { 0 };
        buffer.apply(Collections.singletonList(new Effector() {
            @Override public void apply (int index, float[] data, int start, float now, float dt) {
                applied[0] += 1;
            }
        }), 0, 0);
        assertEquals(100, applied[0]);
    }
}
