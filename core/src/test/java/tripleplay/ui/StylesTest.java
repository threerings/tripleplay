//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.java.JavaPlatform;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests aspects of the {@link Styles} class.
 */
public class StylesTest
{
    // static {
    //     JavaPlatform.Config config = new JavaPlatform.Config();
    //     config.headless = true;
    //     JavaPlatform.register(config);
    // }

    @Test public void testEmpty () {
        Styles s = Styles.none();
        checkIsNull(s, Style.COLOR);
    }

    @Test public void testNonReceiverMod () {
        Styles s = Styles.none();
        checkIsNull(s, Style.COLOR);
        Styles s1 = s.add(Style.COLOR.is(0xFFAABBCC));
        checkIsNull(s, Style.COLOR);
        checkEquals(0xFFAABBCC, s1, Style.COLOR);
    }

    @Test public void testAddsGets () {
        Styles s = Styles.make(Style.COLOR.is(0xFFAABBCC),
                               Style.SHADOW.is(0xFF333333),
                               Style.HIGHLIGHT.is(0xFFAAAAAA));
        checkEquals(0xFFAABBCC, s, Style.COLOR);
        checkEquals(0xFF333333, s, Style.SHADOW);
        checkEquals(0xFFAAAAAA, s, Style.HIGHLIGHT);
    }

    @Test public void testOverwrite () {
        Styles s = Styles.make(Style.COLOR.is(0xFFAABBCC),
                               Style.SHADOW.is(0xFF333333));
        checkEquals(0xFFAABBCC, s, Style.COLOR);
        checkEquals(0xFF333333, s, Style.SHADOW);

        Styles ns = s.add(Style.COLOR.is(0xFFBBAACC));
        checkEquals(0xFFBBAACC, ns, Style.COLOR);

        ns = s.add(Style.COLOR.is(0xFFBBAACC), Style.HIGHLIGHT.is(0xFFAAAAAA));
        checkEquals(0xFFBBAACC, ns, Style.COLOR);
        checkEquals(0xFFAAAAAA, ns, Style.HIGHLIGHT);

        ns = s.add(Style.HIGHLIGHT.is(0xFFAAAAAA), Style.COLOR.is(0xFFBBAACC));
        checkEquals(0xFFBBAACC, ns, Style.COLOR);
        checkEquals(0xFFAAAAAA, ns, Style.HIGHLIGHT);
    }

    @Test public void testClear () {
        Styles s = Styles.make(Style.COLOR.is(0xFFAABBCC),
                               Style.SHADOW.is(0xFF333333));
        checkEquals(0xFFAABBCC, s, Style.COLOR);
        checkEquals(0xFF333333, s, Style.SHADOW);

        s = s.clear(Style.Mode.DEFAULT, Style.COLOR);
        checkEquals(null, s, Style.COLOR);
    }

    protected static <V> void checkIsNull (Styles s, Style<V> style) {
        assertNull(s.get(style, new Label()));
    }

    protected static <V> void checkEquals (V value, Styles s, Style<V> style) {
        assertEquals(value, s.get(style, new Label()));
    }
}
