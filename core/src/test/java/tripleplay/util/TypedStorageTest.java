//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import org.junit.*;
import static org.junit.Assert.*;

import react.Function;
import react.Functions;
import react.RSet;

import playn.core.Platform;
import playn.core.StubPlatform;

public class TypedStorageTest
{
    @Test public void testSetFor () {
        Platform pf = new StubPlatform();
        TypedStorage ts = new TypedStorage(pf.storage());
        Function<String,String> id = Functions.identity();

        RSet<String> strings = ts.setFor("strings", id, id);
        assertTrue(strings.isEmpty());
        strings.add("one");

        // (each call to setFor creates a new set from the curren state of storage)
        assertTrue(ts.setFor("strings", id, id).contains("one"));

        strings.remove("one");
        assertTrue(ts.setFor("strings", id, id).isEmpty());

        strings.add("");
        assertTrue(ts.setFor("strings", id, id).contains(""));
        strings.add("two");
        assertTrue(ts.setFor("strings", id, id).contains(""));
        strings.remove("two");
        assertTrue(ts.setFor("strings", id, id).contains(""));
        strings.remove("");
        assertFalse(ts.setFor("strings", id, id).contains(""));
    }
}
