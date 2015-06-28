//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

/**
 * Inflates data encoded by {@link Deflater}.
 */
public class Inflater extends Conflater
{
    public Inflater (String data) {
        if (data == null) throw new NullPointerException();
        _data = data;
    }

    public boolean popBool () {
        return popChar() == 't';
    }

    public char popChar () {
        return _data.charAt(_pos++);
    }

    public int popNibble () {
        return fromHexString(_data, pos(1), 1);
    }

    public int popByte () {
        return fromHexString(_data, pos(2), 2);
    }

    public int popShort () {
        return fromHexString(_data, pos(4), 4);
    }

    public int popInt () {
        return fromHexString(_data, pos(8), 8);
    }

    public int popVarInt () {
        int value = 0;
        char c;
        boolean neg = _data.charAt(_pos) == NEG_MARKER;
        if (neg) _pos++;
        do {
            value *= BASE;
            c = _data.charAt(_pos++);
            value += (c >= CONT0) ? (c - CONT0) : (c - ABS0);
        } while (c >= CONT0);
        return neg ? (-1*value) : value;
    }

    public long popVarLong () {
        long value = 0;
        char c;
        boolean neg = _data.charAt(_pos) == NEG_MARKER;
        if (neg) _pos++;
        do {
            value *= BASE;
            c = _data.charAt(_pos++);
            value += (c >= CONT0) ? (c - CONT0) : (c - ABS0);
        } while (c >= CONT0);
        return neg ? (-1*value) : value;
    }

    public String popFLString (int length) {
        return _data.substring(pos(length), _pos);
    }

    public String popString () {
        return _data.substring(pos(popShort()), _pos);
    }

    public <E extends Enum<E>> E popEnum (Class<E> eclass) {
        return Enum.valueOf(eclass, popString());
    }

    public boolean eos () {
        return _pos >= _data.length();
    }

    protected int pos (int incr) {
        int pos = _pos;
        _pos += incr;
        return pos;
    }

    protected final String _data;
    protected int _pos;
}
