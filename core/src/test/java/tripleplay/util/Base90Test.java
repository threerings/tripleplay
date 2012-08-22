//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import org.junit.*;
import static org.junit.Assert.*;

public class Base90Test
{
    @Test public void testZero () {
        assertEquals(0, Base90.decodeInt(Base90.encodeInt(0)));
        assertEquals(1, Base90.encodeInt(0).length());
    }

    @Test public void testInts () {
        assertEquals(Integer.MIN_VALUE, Base90.decodeInt(Base90.encodeInt(Integer.MIN_VALUE)));
        assertEquals(Integer.MAX_VALUE, Base90.decodeInt(Base90.encodeInt(Integer.MAX_VALUE)));
        for (int ii = 0; ii < Short.MAX_VALUE; ii++) {
            assertEquals(ii, Base90.decodeInt(Base90.encodeInt(ii)));
            assertEquals(-ii, Base90.decodeInt(Base90.encodeInt(-ii)));
        }
        for (int ii = 0; ii > 0 && ii <= Integer.MAX_VALUE; ii += Short.MAX_VALUE) {
            assertEquals(ii, Base90.decodeInt(Base90.encodeInt(ii)));
        }
        for (int ii = Integer.MIN_VALUE; ii < 0; ii += Short.MAX_VALUE) {
            assertEquals(ii, Base90.decodeInt(Base90.encodeInt(ii)));
        }
    }

    @Test public void testLongs () {
        assertEquals(Long.MIN_VALUE, Base90.decodeLong(Base90.encodeLong(Long.MIN_VALUE)));
        assertEquals(Long.MAX_VALUE, Base90.decodeLong(Base90.encodeLong(Long.MAX_VALUE)));
        for (long ii = 0; ii < Short.MAX_VALUE; ii++) {
            assertEquals(ii, Base90.decodeLong(Base90.encodeLong(ii)));
            assertEquals(-ii, Base90.decodeLong(Base90.encodeLong(-ii)));
        }
        long incr = Long.MAX_VALUE/Short.MAX_VALUE;
        for (long ii = 0; ii > 0 && ii <= Long.MAX_VALUE; ii += incr) {
            assertEquals(ii, Base90.decodeLong(Base90.encodeLong(ii)));
        }
        for (long ii = Long.MIN_VALUE; ii < 0; ii += incr) {
            assertEquals(ii, Base90.decodeLong(Base90.encodeLong(ii)));
        }
    }
}
