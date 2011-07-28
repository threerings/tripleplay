//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import forplay.java.JavaPlatform;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests aspects of the {@link Styles} class.
 */
public class StylesTest
{
    static {
        JavaPlatform.register();
    }

    @Test public void testEmpty () {
        Styles s = Styles.none();
        assertNull(s.get(Style.COLOR));
    }

    @Test public void testNonReceiverMod () {
        Styles s = Styles.none();
        assertNull(s.get(Style.COLOR));
        Styles s1 = s.set(Style.COLOR, 0xFFAABBCC);
        assertNull(s.get(Style.COLOR));
        assertEquals((Integer)0xFFAABBCC, s1.get(Style.COLOR));
    }

    @Test public void testSetsGets () {
        Styles s = Styles.none();
        s = s.set(Style.COLOR, 0xFFAABBCC);
        s = s.set(Style.SHADOW, 0xFF333333);
        s = s.set(Style.HIGHLIGHT, 0xFFAAAAAA);
        assertEquals((Integer)0xFFAABBCC, s.get(Style.COLOR));
        assertEquals((Integer)0xFF333333, s.get(Style.SHADOW));
        assertEquals((Integer)0xFFAAAAAA, s.get(Style.HIGHLIGHT));
    }
}
