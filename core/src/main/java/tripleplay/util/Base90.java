//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

/**
 * Encodes and decodes ints and longs to strings in base 90.
 */
public class Base90
{
    public static String encodeInt (int value) {
        StringBuilder buf = new StringBuilder();
        if (value < 0) {
            buf.append(NEG_MARKER);
            value -= Integer.MIN_VALUE;
        }
        do {
            buf.append(CHARS.charAt(value % BASE));
            value /= BASE;
        } while (value > 0);
        return buf.toString();
    }

    public static int decodeInt (String data) {
        boolean neg = false;
        if (data.length() > 0 && data.charAt(0) == NEG_MARKER) {
            neg = true;
            data = data.substring(1);
        }
        int value = 0;
        for (int ii = data.length()-1; ii >= 0; ii--) {
            value *= BASE;
            value += data.charAt(ii) - FIRST;
        }
        if (neg) value += Integer.MIN_VALUE;
        return value;
    }

    public static String encodeLong (long value) {
        StringBuilder buf = new StringBuilder();
        if (value < 0) {
            buf.append(NEG_MARKER);
            value -= Long.MIN_VALUE;
        }
        while (value > 0) {
            buf.append(CHARS.charAt((int)(value % BASE)));
            value /= BASE;
        }
        return buf.toString();
    }

    public static long decodeLong (String data) {
        boolean neg = false;
        if (data.length() > 0 && data.charAt(0) == NEG_MARKER) {
            neg = true;
            data = data.substring(1);
        }
        long value = 0;
        for (int ii = data.length()-1; ii >= 0; ii--) {
            value *= BASE;
            value += data.charAt(ii) - FIRST;
        }
        if (neg) value += Long.MIN_VALUE;
        return value;
    }

    /** Used to encode ints and longs. */
    protected static final String CHARS =
        "\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNO" + // ! intentionally omitted
        "PQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}";  // ~ intentionally omitted

    /** Used when encoding and decoding. */
    protected static final int BASE = CHARS.length();

    /** Used when decoding. */
    protected static final char FIRST = CHARS.charAt(0);

    /** A character used to mark negative values. */
    protected static final char NEG_MARKER = '!';
}
