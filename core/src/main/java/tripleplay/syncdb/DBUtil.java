//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.syncdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link SyncDB} related utility methods.
 */
public class DBUtil
{
    /** The separator used in a map key. This character must not appear in a normal key. */
    public static final String MAP_KEY_SEP = "~";

    /** The separator used in a subdb key. This character may appear in a normal key, but if the
     * client plans to manage subdb conflict resolution in the aggregate, it is best to avoid using
     * this separator elsewhere. */
    public static final String SUBDB_KEY_SEP = "!";

    /**
     * Decodes a set encoded via {@link #encodeSet}.
     */
    public static <E> Set<E> decodeSet (String data, Codec<E> codec) {
        return decode(data, codec, new HashSet<E>());
    }

    /**
     * Encodes the supplied set as a string, using the supplied codec for its elements. The encoded
     * set uses {@code \t} to separate; that character should not appear in {@code codec}'s output.
     */
    public static <E> String encodeSet (Set<E> set, Codec<E> codec) {
        return encode(set, codec);
    }

    /**
     * Decodes a list encoded via {@link #encodeList}.
     */
    public static <E> List<E> decodeList (String data, Codec<E> codec) {
        return decode(data, codec, new ArrayList<E>());
    }

    /**
     * Encodes the supplied list as a string, using the supplied codec for its elements. The encoded
     * list uses {@code \t} to separate; that character should not appear in {@code codec}'s output.
     */
    public static <E> String encodeList (List<E> list, Codec<E> codec) {
        return encode(list, codec);
    }

    /**
     * Computes the storage (fully qualified) key for the supplied subdb property key.
     */
    public static String subDBKey (String prefix, String key) {
        return prefix + SUBDB_KEY_SEP + key;
    }

    /**
     * Extracts and returns the subdb prefix from the supplied fully-qualified key. Returns null if
     * the supplied key is not a subdb key.
     */
    public static String subDB (String fqKey) {
        int sidx = fqKey.indexOf(SUBDB_KEY_SEP);
        return (sidx < 0) ? null : fqKey.substring(0, sidx);
    }

    /**
     * Computes the storage (fully qualified) key for the supplied map key.
     */
    public static <K> String mapKey (String prefix, K key, Codec<K> codec) {
        return prefix + MAP_KEY_SEP + codec.encode(key);
    }

    protected static <E> String encode (Iterable<E> values, Codec<E> codec) {
        StringBuilder buf = new StringBuilder();
        for (E elem : values) {
            if (buf.length() > 0) buf.append("\t");
            buf.append(codec.encode(elem));
        }
        return buf.toString();
    }

    protected static <E, C extends Collection<E>> C decode (String data, Codec<E> codec, C into) {
        if (data != null && data.length() > 0) {
            for (String edata : data.split("\t")) into.add(codec.decode(edata));
        }
        return into;
    }
}
