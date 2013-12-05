//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import org.junit.*;
import static org.junit.Assert.*;

public class BitVecTest
{
    @Test public void test () {
        BitVec vec = new BitVec(4);
        for (int ii = 0; ii < 10000; ii++) assertFalse(vec.isSet(ii));
        for (int ii = 0; ii < 10000; ii++) vec.set(ii);
        for (int ii = 0; ii < 10000; ii++) assertTrue(vec.isSet(ii));
        for (int ii = 0; ii < 10000; ii++) vec.clear(ii);
        for (int ii = 0; ii < 10000; ii++) assertFalse(vec.isSet(ii));
    }
}
