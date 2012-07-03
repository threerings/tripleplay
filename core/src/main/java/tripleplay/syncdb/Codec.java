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
    Codec<String> STRING = new Codec<String>() {
        public String encode (String value) {
            return value;
        }
        public String decode (String data) {
            return data;
        }
    };

    Codec<Integer> INT = new Codec<Integer>() {
        public String encode (Integer value) {
            return String.valueOf(value);
        }
        public Integer decode (String data) {
            return Integer.parseInt(data);
        }
    };

    Codec<Long> LONG = new Codec<Long>() {
        public String encode (Long value) {
            return String.valueOf(value);
        }
        public Long decode (String data) {
            return Long.parseLong(data);
        }
    };

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
