//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;

import org.junit.Test;
import static org.junit.Assert.*;

import tripleplay.ui.util.Scale9.Axis;

public class Scale9Test
{
    static float DELTA = (float)1e-7;

    @Test public void testAxis () {
        Axis axis = checkCoords(new Axis(1));
        assertEquals(1f/3, axis.size(0), DELTA);
        assertEquals(1-2f/3, axis.size(1), DELTA);
        assertEquals(1f/3, axis.size(2), DELTA);
    }

    @Test public void testAxisResize () {
        Axis axis1 = checkCoords(new Axis(1).resize(0, .25f).resize(2, .25f));
        Axis axis2 = checkCoords(new Axis(1).resize(1, .5f));
        for (Axis axis : new Axis[] {axis1, axis2}) {
            assertEquals(.25f, axis.size(0), DELTA);
            assertEquals(.5f, axis.size(1), DELTA);
            assertEquals(.25f, axis.size(2), DELTA);
        }
    }

    @Test public void testAxisDest () {
        Axis axis = checkCoords(new Axis(1, new Axis(1)));
        assertEquals(1f/3, axis.size(0), DELTA);
        assertEquals(1-2f/3, axis.size(1), DELTA);
        assertEquals(1f/3, axis.size(2), DELTA);

        axis = checkCoords(new Axis(2, new Axis(1)));
        assertEquals(1f/3, axis.size(0), DELTA);
        assertEquals(2-2f/3, axis.size(1), DELTA);
        assertEquals(1f/3, axis.size(2), DELTA);

        axis = checkCoords(new Axis(.5f, new Axis(1)));
        assertEquals(1f/3, axis.size(0), DELTA);
        assertEquals(.5-2f/3, axis.size(1), DELTA);
        assertEquals(1f/3, axis.size(2), DELTA);
    }

    @Test public void testAxisClamp () {
        Axis axis = checkCoords(Scale9.clamp(new Axis(1), 1));
        assertEquals(1f/3, axis.size(0), DELTA);
        assertEquals(1-2f/3, axis.size(1), DELTA);
        assertEquals(1f/3, axis.size(2), DELTA);

        axis = checkCoords(Scale9.clamp(new Axis(2).resize(1, 1.5f), 1));
        assertEquals(.25f, axis.size(0), DELTA);
        assertEquals(.5f, axis.size(1), DELTA);
        assertEquals(.25f, axis.size(2), DELTA);

        axis = checkCoords(Scale9.clamp(new Axis(1), .5f));
        assertEquals(.25f, axis.size(0), DELTA);
        assertEquals(0, axis.size(1), DELTA);
        assertEquals(.25f, axis.size(2), DELTA);
    }

    Axis checkCoords (Axis axis) {
        assertEquals(axis.coord(0), 0, DELTA);
        assertEquals(axis.coord(1), axis.size(0), DELTA);
        assertEquals(axis.coord(2), axis.size(0) + axis.size(1), DELTA);
        return axis;
    }
}
