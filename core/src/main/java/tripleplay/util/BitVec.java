//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

/**
 * A bit vector. We'd use Java's {@code BitSet}, but GWT doesn't support it. Note that we also use
 * {@code int} instead of {@code long} to be GWT/JavaScript-friendly. TODO: maybe use super-source
 * to use longs by default but int for GWT?
 */
public final class BitVec
{
    /** Creates a bit vector with an initial capacity of 16 words. */
    public BitVec () {
        this(16);
    }

    /** Creates a bit vector with the specified initial capacity (in words). */
    public BitVec (int words) {
        _words = new int[words];
    }

    /** Returns whether the {@code value}th bit it set. */
    public boolean isSet (int value) {
        int word = value / 32;
        return _words.length > word && (_words[word] & (1 << (value % 32))) != 0;
    }

    /** Sets the {@code value}th bit. */
    public void set (int value) {
        int word = value / 32;
        if (_words.length <= word) {
            int[] words = new int[_words.length*2];
            java.lang.System.arraycopy(_words, 0, words, 0, _words.length);
            _words = words;
        }
        _words[word] |= (1 << (value % 32));
    }

    /** Clears the {@code value}th bit. */
    public void clear (int value) {
        int word = value / 32;
        if (_words.length > word) {
            _words[word] &= ~(1 << (value % 32));
        }
    }

    /** Clears all bits in this vector. */
    public void clear () {
        for (int ii = 0; ii < _words.length; ii++) _words[ii] = 0;
    }

    protected int[] _words;
}
