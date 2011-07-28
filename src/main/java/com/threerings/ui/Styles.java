//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import java.util.Arrays;

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
     * Creates a styles instance with the supplied style bindings in the specified state.
     */
    public static Styles make (Element.State state, Style.Binding<?>... bindings) {
        return none().add(state, bindings);
    }

    /** Creates a styles instance with the supplied style bindings in the DEFAULT state. */
    public static Styles make (Style.Binding<?>... bindings) {
        return make(Element.State.DEFAULT, bindings);
    }

    /** Creates a styles instance with the supplied style bindings in the DISABLED state. */
    public static Styles makeDisabled (Style.Binding<?>... bindings) {
        return make(Element.State.DISABLED, bindings);
    }

    /** Creates a styles instance with the supplied style bindings in the DOWN state. */
    public static Styles makeDown (Style.Binding<?>... bindings) {
        return make(Element.State.DOWN, bindings);
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles (in the specified state). The receiver is not modified.
     */
    public Styles add (Element.State state, Style.Binding<?>... bindings) {
        Binding[] nbindings = new Binding[bindings.length];
        for (int ii = 0; ii < bindings.length; ii++) {
            nbindings[ii] = new Binding(state, bindings[ii].style, bindings[ii].value);
        }
        // note that we take advantage of the fact that merge can handle unsorted bindings
        return merge(new Styles(nbindings));
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles in the default state. The receiver is not modified.
     */
    public Styles add (Style.Binding<?>... bindings) {
        return add(Element.State.DEFAULT, bindings);
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles in the disabled state. The receiver is not modified.
     */
    public Styles addDisabled (Style.Binding<?>... bindings) {
        return add(Element.State.DISABLED, bindings);
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles in the down state. The receiver is not modified.
     */
    public Styles addDown (Style.Binding<?>... bindings) {
        return add(Element.State.DOWN, bindings);
    }

    /**
     * Returns a new instance where no binding exists for the specified style in the specified
     * state. The receiver is not modified.
     */
    public Styles clear (Element.State state, Style<?> style) {
        Key key = new Key(state, style);
        int index = Arrays.binarySearch(_bindings, key);
        if (index < 0) return this;
        Binding[] nbindings = new Binding[_bindings.length-1];
        System.arraycopy(_bindings, 0, nbindings, 0, index);
        System.arraycopy(_bindings, index+1, nbindings, index, nbindings.length-index);
        return new Styles(nbindings);
    }

    /**
     * Returns the binding for the specified style in the specified state, or null.
     */
    public <V> V get (Element.State state, Style<V> style) {
        return this.<V>get(new Key(state, style));
    }

    /**
     * Returns the binding for the specified style (in the default state), or null.
     */
    public <V> V get (Style<V> style) {
        return get(Element.State.DEFAULT, style);
    }

    /**
     * Returns a new styles instance which merges these styles with the supplied styles. Where both
     * instances define a particular style, the supplied {@code styles} will take precedence.
     */
    public Styles merge (Styles styles) {
        // determine which of the to-be-merged styles also exist in our styles
        Binding[] obindings = styles._bindings;
        int[] dupidx = new int[obindings.length];
        int dups = 0;
        for (int ii = 0; ii < obindings.length; ii++) {
            int idx = Arrays.binarySearch(_bindings, obindings[ii]);
            if (idx >= 0) dups++;
            dupidx[ii] = idx;
        }

        // copy the old bindings, overwrite any duplicated bindings, tack the rest on the end
        Binding[] nbindings = new Binding[_bindings.length + obindings.length - dups];
        System.arraycopy(_bindings, 0, nbindings, 0, _bindings.length);
        int idx = _bindings.length;
        for (int ii = 0; ii < obindings.length; ii++) {
            if (dupidx[ii] >= 0) nbindings[dupidx[ii]] = obindings[ii];
            else nbindings[idx++] = obindings[ii];
        }
        Arrays.sort(nbindings);

        return new Styles(nbindings);
    }

    <V> V get (Key key) {
        int index = Arrays.binarySearch(_bindings, key);
        if (index < 0) return null;
        @SuppressWarnings("unchecked") V value = (V)_bindings[index].value;
        return value;
    }

    private Styles (Binding[] bindings) {
        _bindings = bindings;
    }

    static <V> V resolveStyle (Element element, Element.State state, Style<V> style) {
        // first check for the style configured directly on the element
        Key key = new Key(state, style);
        V value = element.styles().<V>get(key);
        if (value != null) return value;

        // now check for the style in the appropriate stylesheets
        Group group = (element instanceof Group) ? (Group)element : element.parent();
        for (; group != null; group = group.parent()) {
            Stylesheet sheet = group.stylesheet();
            if (sheet == null) continue;
            value = sheet.<V>get(element.getClass(), key);
            if (value != null) return value;
        }

        // TODO
        return style.getDefault(state);
    }

    static class Key implements Comparable<Key> {
        public Element.State state;
        public Style<?> style;

        public Key (Element.State state, Style<?> style) {
            this.state = state;
            this.style = style;
        }

        @Override public int compareTo (Key other) {
            if (this.style == other.style) {
                return state.compareTo(other.state);
            } else {
                int hc = style.hashCode(), ohc = other.style.hashCode();
                assert(hc != ohc);
                return (hc < ohc) ? -1 : 1;
            }
        }

        @Override public boolean equals (Object other) {
            Key okey = (Key)other;
            return (okey.style == style) && (okey.state == state);
        }

        @Override public int hashCode () {
            return style.hashCode() ^ state.hashCode();
        }
    }

    static class Binding extends Key {
        public Object value;

        public Binding (Element.State state, Style<?> style, Object value) {
            super(state, style);
            this.value = value;
        }
    }

    protected Binding[] _bindings;

    protected static final Styles _noneSingleton = new Styles(new Binding[0]);
}
