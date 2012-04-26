//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests aspects of the {@link Logger} class.
 */
public class LoggerTest
{
    @Test
    public void testOutput () {
        StringWriter buf = configWriterImpl();
        Logger log = new Logger("test");
        log.info("This is a test.");
        assertEquals("test: This is a test." + NEWLINE, buf.toString());
    }

    @Test
    public void testParams () {
        StringWriter buf = configWriterImpl();
        Logger log = new Logger("test");
        log.info("Foo.", "bar", "baz", "one", 1, "null", null);
        assertEquals("test: Foo. [bar=baz, one=1, null=null]" + NEWLINE, buf.toString());
    }

    @Test
    public void testException () {
        StringWriter buf = configWriterImpl();
        Logger log = new Logger("test");
        log.info("Foo.", "bar", "baz", new Exception());
        String[] lines = buf.toString().split(NEWLINE);
        assertEquals("test: Foo. [bar=baz]", lines[0]);
        assertEquals("java.lang.Exception", lines[1]);
    }

    protected static StringWriter configWriterImpl () {
        StringWriter buf = new StringWriter();
        final PrintWriter out = new PrintWriter(buf);
        Logger.setImpl(new Logger.Impl() {
            public void debug (String ident, String message, Throwable t) {
                out.println(message);
                if (t != null) t.printStackTrace(out);
            }
            public void info (String ident, String message, Throwable t) {
                out.println(message);
                if (t != null) t.printStackTrace(out);
            }
            public void warning (String ident, String message, Throwable t) {
                out.println(message);
                if (t != null) t.printStackTrace(out);
            }
        });
        return buf;
    }

    protected static final String NEWLINE = System.getProperty("line.separator");
}
