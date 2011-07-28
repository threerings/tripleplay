//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

/**
 * An immutable collection of styles. Used in builder-style to add, replace or remove styles.
 * Configure a group of styles and then apply them to an element via {@link Element#setStyles} or
 * {@link Element#addStyles}.
 */
public final class Styles
{
    /**
     * Returns the empty styles instance.
     */
    public static Styles none () {
        return _noneSingleton;
    }

    /**
     * Returns the binding for the specified style (in the default state), or null.
     */
    public <V> V get (Style<V> style) {
        return get(Element.State.DEFAULT, style);
    }

    /**
     * Returns the binding for the specified style in the specified state, or null.
     */
    public <V> V get (Element.State state, Style<V> style) {
        return get(new Key<V>(style, state));
    }

    /**
     * Returns a new instance where the supplied binding overwrites any previous binding for the
     * specified style (in the default state). The receiver is not modified.
     */
    public <V> Styles set (Style<V> style, V value) {
        return set(Element.State.DEFAULT, style, value);
    }

    /**
     * Returns a new instance where no binding exists for the specified style (in the default
     * state). The receiver is not modified.
     */
    public <V> Styles clear (Style<V> style) {
        return clear(Element.State.DEFAULT, style);
    }

    /**
     * Returns a new instance where the supplied binding overwrites any previous binding for the
     * specified style (in the specified state). The receiver is not modified.
     */
    public <V> Styles set (Element.State state, Style<V> style, V value) {
        Key<V> key = new Key<V>(style, state);
        Key<?>[] nkeys;
        Object[] nvalues;
        int index = findIndex(key);
        if (index < 0) {
            int iidx = -(index+1);
            nkeys = new Key<?>[_keys.length+1];
            System.arraycopy(_keys, 0, nkeys, 0, iidx);
            nkeys[iidx] = key;
            System.arraycopy(_keys, iidx, nkeys, iidx+1, _keys.length-iidx);
            nvalues = new Object[nkeys.length];
            System.arraycopy(_values, 0, nvalues, 0, iidx);
            nvalues[iidx] = value;
            System.arraycopy(_values, iidx, nvalues, iidx+1, _values.length-iidx);
        } else {
            nkeys = new Key<?>[_keys.length];
            System.arraycopy(_keys, 0, nkeys, 0, nkeys.length);
            nkeys[index] = key;
            nvalues = new Object[nkeys.length];
            System.arraycopy(_values, 0, nvalues, 0, nvalues.length);
            nvalues[index] = value;
        }
        return new Styles(nkeys, nvalues);
    }

    /**
     * Returns a new instance where no binding exists for the specified style (in the specified
     * state). The receiver is not modified.
     */
    public <V> Styles clear (Element.State state, Style<V> style) {
        Key<V> key = new Key<V>(style, state);
        int index = findIndex(key);
        if (index < 0) return this;
        Key<?>[] nkeys = new Key<?>[_keys.length-1];
        Object[] nvalues = new Object[nkeys.length];
        System.arraycopy(_keys, 0, nkeys, 0, index);
        System.arraycopy(_keys, index+1, nkeys, index, nkeys.length-index);
        System.arraycopy(_values, 0, nvalues, 0, index);
        System.arraycopy(_values, index+1, nvalues, index, nvalues.length-index);
        return new Styles(nkeys, nvalues);
    }

    <V> V get (Key<V> key) {
        int index = findIndex(key);
        if (index < 0) return null;
        @SuppressWarnings("unchecked") V value = (V)_values[index];
        return value;
    }

    private int findIndex (Key<?> key) {
        int low = 0, high = _keys.length-1;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            int cmp = key.compareTo(_keys[mid]);
            if (cmp > 0) {
                low = mid + 1;
            } else if (cmp < 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1); // key not found.
    }

    private Styles (Key<?>[] keys, Object[] values) {
        _keys = keys;
        _values = values;
    }

    static <V> V resolveStyle (Element element, Element.State state, Style<V> style) {
        // first check for the style configured directly on the element
        Key<V> key = new Key<V>(style, state);
        V value = element.styles().get(key);
        if (value != null) return value;

        // now check for the style in the appropriate stylesheets
        Group group = (element instanceof Group) ? (Group)element : element.parent();
        for (; group != null; group = group.parent()) {
            Stylesheet sheet = group.stylesheet();
            if (sheet == null) continue;
            value = sheet.get(element.getClass(), key);
            if (value != null) return value;
        }

        // TODO
        return style.getDefault(state);
    }

    static class Key<V> implements Comparable<Key<?>> {
        public Style<V> style;
        public Element.State state;

        public Key (Style<V> style, Element.State state) {
            this.style = style;
            this.state = state;
        }

        @Override public int compareTo (Key<?> other) {
            if (this.style == other.style) {
                return state.compareTo(other.state);
            } else {
                int hc = style.hashCode(), ohc = other.style.hashCode();
                assert(hc != ohc);
                return (hc < ohc) ? -1 : 1;
            }
        }

        @Override public boolean equals (Object other) {
            Key<?> okey = (Key<?>)other;
            return (okey.style == style) && (okey.state == state);
        }

        @Override public int hashCode () {
            return style.hashCode() ^ state.hashCode();
        }
    }

    protected Key<?>[] _keys;
    protected Object[] _values;

    protected static final Styles _noneSingleton = new Styles(new Key<?>[0], new Object[0]);
}
