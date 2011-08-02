//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

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
        Styles s1 = s.add(Style.COLOR.is(0xFFAABBCC));
        assertNull(s.get(Style.COLOR));
        assertEquals((Integer)0xFFAABBCC, s1.get(Style.COLOR));
    }

    @Test public void testAddsGets () {
        Styles s = Styles.make(Style.COLOR.is(0xFFAABBCC),
                               Style.SHADOW.is(0xFF333333),
                               Style.HIGHLIGHT.is(0xFFAAAAAA));
        assertEquals((Integer)0xFFAABBCC, s.get(Style.COLOR));
        assertEquals((Integer)0xFF333333, s.get(Style.SHADOW));
        assertEquals((Integer)0xFFAAAAAA, s.get(Style.HIGHLIGHT));
    }

    @Test public void testOverwrite () {
        Styles s = Styles.make(Style.COLOR.is(0xFFAABBCC),
                               Style.SHADOW.is(0xFF333333));
        assertEquals((Integer)0xFFAABBCC, s.get(Style.COLOR));
        assertEquals((Integer)0xFF333333, s.get(Style.SHADOW));

        Styles ns = s.add(Style.COLOR.is(0xFFBBAACC));
        assertEquals((Integer)0xFFBBAACC, ns.get(Style.COLOR));

        ns = s.add(Style.COLOR.is(0xFFBBAACC), Style.HIGHLIGHT.is(0xFFAAAAAA));
        assertEquals((Integer)0xFFBBAACC, ns.get(Style.COLOR));
        assertEquals((Integer)0xFFAAAAAA, ns.get(Style.HIGHLIGHT));

        ns = s.add(Style.HIGHLIGHT.is(0xFFAAAAAA), Style.COLOR.is(0xFFBBAACC));
        assertEquals((Integer)0xFFBBAACC, ns.get(Style.COLOR));
        assertEquals((Integer)0xFFAAAAAA, ns.get(Style.HIGHLIGHT));
    }

    @Test public void testClear () {
        Styles s = Styles.make(Style.COLOR.is(0xFFAABBCC),
                               Style.SHADOW.is(0xFF333333));
        assertEquals((Integer)0xFFAABBCC, s.get(Style.COLOR));
        assertEquals((Integer)0xFF333333, s.get(Style.SHADOW));

        s = s.clear(Element.State.DEFAULT, Style.COLOR);
        assertEquals(null, s.get(Style.COLOR));
    }
}
