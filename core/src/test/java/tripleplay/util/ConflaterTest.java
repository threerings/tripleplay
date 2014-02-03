//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import org.junit.*;
import static org.junit.Assert.*;

public class ConflaterTest
{
    @Test public void testInflaterDeflater () {
        Deflater d = new Deflater();
        for (int ii = 0; ii <= 15; ii++) d.addNibble(ii);
        d.addBool(true);
        for (int ii = Byte.MIN_VALUE; ii <= Byte.MAX_VALUE; ii++) d.addByte(ii);
        d.addFLString("FIXED");
        for (int ii = Short.MIN_VALUE; ii <= Short.MAX_VALUE; ii += 128) d.addShort(ii);
        d.addString("four");
        for (int ii = Integer.MIN_VALUE; ii <= 0; ii += 65536) {
            d.addInt(ii);
            if (ii > Integer.MIN_VALUE) d.addVarInt(ii);
        }
        for (int ii = Integer.MAX_VALUE; ii >= 0; ii -= 65536) {
            d.addInt(ii).addVarInt(ii);
        }
        d.addBool(false);

        Inflater i = new Inflater(d.encoded());
        for (int ii = 0; ii <= 15; ii++) assertEquals(ii, i.popNibble());
        assertEquals(true, i.popBool());
        for (int ii = Byte.MIN_VALUE; ii <= Byte.MAX_VALUE; ii++) assertEquals(ii, i.popByte());
        assertEquals("FIXED", i.popFLString("FIXED".length()));
        for (int ii = Short.MIN_VALUE; ii <= Short.MAX_VALUE; ii += 128)
            assertEquals(ii, i.popShort());
        assertEquals("four", i.popString());
        for (int ii = Integer.MIN_VALUE; ii <= 0; ii += 65536) {
            assertEquals(ii, i.popInt());
            if (ii > Integer.MIN_VALUE) assertEquals(ii, i.popVarInt());
        }
        for (int ii = Integer.MAX_VALUE; ii >= 0; ii -= 65536) {
            assertEquals(ii, i.popInt());
            assertEquals(ii, i.popVarInt());
        }
        assertEquals(false, i.popBool());
    }
}
