//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
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
        assertEquals("This is a test." + NEWLINE, buf.toString());
        Logger.setImpl(null);
    }

    @Test
    public void testParams () {
        StringWriter buf = configWriterImpl();
        Logger log = new Logger("test");
        log.info("Foo.", "bar", "baz", "one", 1, "null", null);
        assertEquals("Foo. [bar=baz, one=1, null=null]" + NEWLINE, buf.toString());
        Logger.setImpl(null);
    }

    @Test
    public void testDefaultLevel () {
        StringWriter buf = configWriterImpl();
        Logger.levels.setDefault(Logger.Level.WARNING);
        Logger log = new Logger("test");
        log.debug("Debug");
        log.info("Info");
        log.warning("Warning");
        assertEquals("Warning" + NEWLINE, buf.toString());
        Logger.levels.setDefault(Logger.Level.DEBUG);
        Logger.setImpl(null);
    }

    @Test
    public void testLevels () {
        StringWriter buf = configWriterImpl();
        Logger.levels.set("test", Logger.Level.WARNING);
        Logger log = new Logger("test");
        log.debug("Debug");
        log.info("Info");
        log.warning("Warning");
        assertEquals("Warning" + NEWLINE, buf.toString());
        Logger.levels.set("test", null);
        Logger.setImpl(null);
    }

    @Test
    public void testException () {
        StringWriter buf = configWriterImpl();
        Logger log = new Logger("test");
        log.info("Foo.", "bar", "baz", new Exception());
        String[] lines = buf.toString().split(NEWLINE);
        assertEquals("Foo. [bar=baz]", lines[0]);
        assertEquals("java.lang.Exception", lines[1]);
        Logger.setImpl(null);
    }

    protected static StringWriter configWriterImpl () {
        StringWriter buf = new StringWriter();
        final PrintWriter out = new PrintWriter(buf);
        Logger.setImpl(new Logger.Impl() {
            public void log (Logger.Level level, String ident, String message, Throwable t) {
                out.println(message);
                if (t != null) t.printStackTrace(out);
            }
        });
        return buf;
    }

    protected static final String NEWLINE = System.getProperty("line.separator");
}
