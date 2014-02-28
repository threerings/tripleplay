//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.HashMap;
import java.util.Map;

import playn.core.PlayN;

/**
 * Provides logging services that are routed to the appropriate logging destination on the client
 * or server. A useful usage pattern is for a library or game to define a shared {@code Log} class
 * like so:
 *
 * <pre>{@code
 * import tripleplay.util.Logger;
 * public class Log {
 *   public static final Logger log = new Logger("libident");
 * }
 * }</pre>
 *
 * and then import {@code Log.log} statically into classes so that they can invoke {@code
 * log.info}, etc.
 */
public class Logger
{
    /** Tags a log message with a particular severity level. */
    public enum Level { DEBUG, INFO, WARNING, ERROR, OFF; }

    /**
     * Wires the logging front-end to the logging back-end. See {@link #setImpl}.
     */
    public interface Impl {
        /** Logs the supplied message at the supplied level. */
        void log (Level level, String ident, String message, Throwable t);
    }

    /** Manages the target log levels for a given ident. */
    public static class Levels {
        /** Configures the default log level. Messages with severity lower than this level will not
         * be logged unless a specific level is set for their identifier. */
        public Levels setDefault (Level level) {
            _defaultLevel = level;
            return this;
        }

        /** Configures the log level for messages with the supplied identifier. Messages with the
         * supplied identifier with severity lower than this level will not be logged regardless of
         * the default log level. Pass null to clear any level cutoff for {@code ident}. */
        public Levels set (String ident, Level level) {
            _levels.put(ident, level);
            return this;
        }

        /** Configures the log level for messages from the supplied logger. Messages from the
         * supplied logger with severity lower than this level will not be logged regardless of
         * the default log level. Pass null to clear any level cutoff for {@code logger}. */
        public Levels set (Logger logger, Level level) {
            _levels.put(logger._ident, level);
            return this;
        }

        /** Returns the current default log level. */
        public Level defaultLevel () {
            return _defaultLevel;
        }

        /** Returns the current log level for the specified identifier, or null if no level is
         * configured for that identifier. */
        public Level level (String ident) {
            return _levels.get(ident);
        }

        /** Returns true if a message with the specified level and ident should be logged. */
        public boolean shouldLog (Level level, String ident) {
            Level ilevel = _levels.get(ident);
            if (ilevel != null) return level.ordinal() >= ilevel.ordinal();
            return level.ordinal() >= _defaultLevel.ordinal();
        }

        protected Level _defaultLevel = Level.DEBUG;
        protected Map<String,Level> _levels = new HashMap<String,Level>();
    }

    /**
     * A logging back-end that writes to PlayN.
     */
    public static class PlayNImpl implements Impl {
        public void log (Level level, String ident, String message, Throwable t) {
            String msg = ident + ": " + message;
            switch (level) {
            case DEBUG:
                if (t != null) PlayN.log().debug(msg, t);
                else PlayN.log().debug(msg);
                break;
            default:
            case INFO:
                if (t != null) PlayN.log().info(msg, t);
                else PlayN.log().info(msg);
                break;
            case WARNING:
                if (t != null) PlayN.log().warn(msg, t);
                else PlayN.log().warn(msg);
                break;
            case ERROR:
                if (t != null) PlayN.log().error(msg, t);
                else PlayN.log().error(msg);
                break;
            }
        }
    }

    /** Log levels can be configured via this instance. */
    public static Levels levels = new Levels();

    /**
     * Configures the logging back-end. This should be called before any code that makes use of the
     * logging services. The default back-end logs to {@code stderr}, which is useful when running
     * in unit tests. {@code null} may be supplied to restore the default (stderr) back-end.
     */
    public static void setImpl (Impl impl) {
        _impl = (impl == null) ? DEFAULT : impl;
    }

    /**
     * Formats and returns the supplied message and key/value arguments as
     * {@code message [key=value, key=value, ...]}.
     */
    public static String format (Object message, Object... args) {
        return format(new StringBuilder().append(message).append(" ["), args).append("]").toString();
    }

    /**
     * Formats the supplied key/value arguments into the supplied string builder as {@code
     * key=value, key=value, ...}.
     * @return the supplied string builder.
     */
    public static StringBuilder format (StringBuilder into, Object... args) {
        for (int ii = 0, ll = args.length/2; ii < ll; ii++) {
            if (ii > 0) {
                into.append(", ");
            }
            into.append(args[2*ii]).append("=").append(args[2*ii+1]);
        }
        return into;
    }

    /**
     * Creates a logger with the specified ident string.
     */
    public Logger (String ident) {
        _ident = ident;
    }

    /**
     * Tests if this logger will output messages of the given level.
     */
    public boolean shouldLog (Level level) {
        return levels.shouldLog(level, _ident);
    }

    /**
     * Logs a debug message.
     *
     * @param message the text of the message.
     * @param args a series of zero or more key/value pairs followed by an optional {@link
     * Throwable} cause.
     */
    public void debug (String message, Object... args) {
        if (levels.shouldLog(Level.DEBUG, _ident)) {
            log(Level.DEBUG, _ident, message, args);
        }
    }

    /**
     * Logs an info message.
     *
     * @param message the text of the message.
     * @param args a series of zero or more key/value pairs followed by an optional {@link
     * Throwable} cause.
     */
    public void info (String message, Object... args) {
        if (levels.shouldLog(Level.INFO, _ident)) {
            log(Level.INFO, _ident, message, args);
        }
    }

    /**
     * Logs a warning message.
     *
     * @param message the text of the message.
     * @param args a series of zero or more key/value pairs followed by an optional {@link
     * Throwable} cause.
     */
    public void warning (String message, Object... args) {
        if (levels.shouldLog(Level.WARNING, _ident)) {
            log(Level.WARNING, _ident, message, args);
        }
    }

    /**
     * Logs an error message.
     *
     * @param message the text of the message.
     * @param args a series of zero or more key/value pairs followed by an optional {@link
     * Throwable} cause.
     */
    public void error (String message, Object... args) {
        if (levels.shouldLog(Level.ERROR, _ident)) {
            log(Level.ERROR, _ident, message, args);
        }
    }

    protected final String _ident;

    protected void log (Level level, String ident, String message, Object... args) {
        StringBuilder sb = new StringBuilder().append(message);
        if (args.length > 1) {
            sb.append(" [");
            format(sb, args);
            sb.append("]");
        }
        Object error = (args.length % 2 == 1) ? args[args.length-1] : null;
        _impl.log(level, ident, sb.toString(), (Throwable)error);
    }

    protected static final Impl DEFAULT = new Impl() {
        @Override public void log (Level level, String ident, String message, Throwable t) {
            System.out.println(ident + ": " + message);
            if (t != null) {
                t.printStackTrace(System.out);
            }
        }
    };

    protected static Impl _impl = DEFAULT;
}
