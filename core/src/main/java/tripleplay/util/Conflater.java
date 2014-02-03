//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

abstract class Conflater {

    protected static String toHexString (int value, int ncount) {
        char[] data = new char[ncount];
        for (int ii = ncount-1; ii >= 0; ii--) {
            data[ii] = HEX_CHARS.charAt(value & 0xF);
            value >>>= 4;
        }
        return new String(data);
    }

    protected static int fromHexString (String buf, int offset, int ncount) {
        int value = 0;
        for (int ii = offset, ll = offset+ncount; ii < ll; ii++) {
            value <<= 4;
            char c = buf.charAt(ii);
            int nibble = (c >= 'A') ? (10+c-'A') : (c-'0');
            value |= nibble;
        }
        if (ncount == 2 && value > Byte.MAX_VALUE) value -= 256;
        else if (ncount == 4 && value > Short.MAX_VALUE) value -= 65536;
        return value;
    }

    protected static final String HEX_CHARS = "0123456789ABCDEF";

    // used for variable length int encoding
    protected static final String VARABS =
        "\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNO"; // ! intentionally omitted
    protected static final String VARCONT =
        "PQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}"; // ~ intentionally omitted
    protected static final int BASE = VARABS.length();
    protected static final char ABS0 = VARABS.charAt(0), CONT0 = VARCONT.charAt(0);
    protected static final char NEG_MARKER = '!';
}
