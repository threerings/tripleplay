//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.syncdb;

/**
 * Handles encoding/decoding properties to/from strings.
 */
public interface Codec<T>
{
    /** A codec for enums which encodes to/from {@link Enum#name}. */
    class EnumC<E extends Enum<E>> implements Codec<E> {
        public static <E extends Enum<E>> EnumC<E> create (Class<E> eclass) {
            return new EnumC<E>(eclass);
        }

        public EnumC (Class<E> eclass) {
            _eclass = eclass;
        }
        @Override public String encode (E value) {
            return value.name();
        }
        @Override public E decode (String data) {
            return Enum.valueOf(_eclass, data);
        }
        protected final Class<E> _eclass;
    }

    /** A codec for strings. The identity codec. */
    Codec<String> STRING = new Codec<String>() {
        public String encode (String value) {
            return value;
        }
        public String decode (String data) {
            return data;
        }
    };

    /** A codec for ints. */
    Codec<Integer> INT = new Codec<Integer>() {
        public String encode (Integer value) {
            return String.valueOf(value);
        }
        public Integer decode (String data) {
            return Integer.parseInt(data);
        }
    };

    /** A codec for longs. */
    Codec<Long> LONG = new Codec<Long>() {
        public String encode (Long value) {
            return String.valueOf(value);
        }
        public Long decode (String data) {
            return Long.parseLong(data);
        }
    };

    /** A codec for booleans. Encodes to the string {@code true} or {@code false}. */
    Codec<Boolean> BOOLEAN = new Codec<Boolean>() {
        public String encode (Boolean value) {
            return String.valueOf(value);
        }
        public Boolean decode (String data) {
            return Boolean.parseBoolean(data);
        }
    };

    /** Encodes the supplied value to a string. */
    String encode (T value);

    /** Decodes the supplied string into a value. */
    T decode (String data);
}
