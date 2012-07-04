//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.syncdb;

import org.junit.*;
import static org.junit.Assert.*;

public class ProtocolTest
{
    @Test public void testPayloadInts () {
        Protocol.PayloadWriter out = new Protocol.PayloadWriter();
        for (int ii = 0; ii < 2*Short.MAX_VALUE; ii += 17) {
            out.writeInt(ii);
        }
        Protocol.PayloadReader in = new Protocol.PayloadReader(out.payload());
        for (int ii = 0; ii < 2*Short.MAX_VALUE; ii += 17) {
            assertEquals(ii, in.readInt());
        }
    }

    @Test public void testPayloadStrings () {
        Protocol.PayloadWriter out = new Protocol.PayloadWriter();
        out.writeString("one");
        out.writeInt(42);
        out.writeString("two");
        out.writeString("");
        Protocol.PayloadReader in = new Protocol.PayloadReader(out.payload());
        assertEquals("one", in.readString());
        assertEquals(42, in.readInt());
        assertEquals("two", in.readString());
        assertEquals("", in.readString());
    }
}
