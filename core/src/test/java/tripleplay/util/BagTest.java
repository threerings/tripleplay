//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.HashSet;
import java.util.Set;

import org.junit.*;
import static org.junit.Assert.*;

public class BagTest
{
    @Test public void testAddContainsRemove () {
        Bag<Integer> bag = Bag.create();
        for (int ii = 15; ii < 35; ii++) {
            bag.add(ii);
        }
        assertEquals(35-15, bag.size());
        for (int ii = 0; ii < 50; ii++) {
            assertEquals(ii >= 15 && ii < 35, bag.contains(ii));
        }
        for (int ii = 0; ii < 50; ii++) {
            assertEquals(ii >= 15 && ii < 35, bag.remove(ii));
        }
        assertEquals(0, bag.size());
        bag.add(3);
        bag.add(5);
        bag.add(9);
        assertEquals((Integer)9, bag.removeLast());
        assertEquals((Integer)5, bag.removeLast());
        assertEquals((Integer)3, bag.removeLast());
        assertEquals(0, bag.size());
    }

    @Test public void testIterator () {
        Bag<Integer> bag = Bag.create();
        Set<Integer> values = new HashSet<Integer>();
        values.add(5);
        values.add(10);
        values.add(25);

        for (Integer elem : values) bag.add(elem);
        assertEquals(values.size(), bag.size());
        for (Integer elem : bag) assertTrue(values.remove(elem));
        assertTrue(values.isEmpty());
    }
}
