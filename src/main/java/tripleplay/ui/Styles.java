//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

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
        Binding<?>[] nbindings = new Binding<?>[bindings.length];
        for (int ii = 0; ii < bindings.length; ii++) {
            nbindings[ii] = newBinding(bindings[ii], state);
        }
        // note that we take advantage of the fact that merge can handle unsorted bindings
        return merge(nbindings);
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
    public <V> Styles clear (Element.State state, Style<V> style) {
        int index = Arrays.binarySearch(_bindings, new Binding<V>(style));
        if (index < 0) return this;
        @SuppressWarnings("unchecked") Binding<V> binding = (Binding<V>)_bindings[index];
        Binding<?>[] nbindings = new Binding<?>[_bindings.length];
        System.arraycopy(_bindings, 0, nbindings, 0, nbindings.length);
        nbindings[index] = binding.clear(state);
        return new Styles(nbindings);
    }

    /**
     * Returns the binding for the specified style in the specified state, or null.
     */
    public <V> V get (Element.State state, Style<V> style) {
        return this.<V>get(new Binding<V>(style), state);
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
        return merge(styles._bindings);
    }

    <V> V get (Binding<V> key, Element.State state) {
        int index = Arrays.binarySearch(_bindings, key);
        if (index < 0) return null;
        @SuppressWarnings("unchecked") Binding<V> binding = (Binding<V>)_bindings[index];
        return binding.get(state);
    }

    private Styles merge (Binding<?>[] obindings) {
        // determine which of the to-be-merged styles also exist in our styles
        int[] dupidx = new int[obindings.length];
        int dups = 0;
        for (int ii = 0; ii < obindings.length; ii++) {
            int idx = Arrays.binarySearch(_bindings, obindings[ii]);
            if (idx >= 0) dups++;
            dupidx[ii] = idx;
        }

        // copy the old bindings, merge any duplicated bindings, tack the rest on the end
        Binding<?>[] nbindings = new Binding<?>[_bindings.length + obindings.length - dups];
        System.arraycopy(_bindings, 0, nbindings, 0, _bindings.length);
        int idx = _bindings.length;
        for (int ii = 0; ii < obindings.length; ii++) {
            int didx = dupidx[ii];
            if (didx >= 0) {
                @SuppressWarnings("unchecked") Binding<Object> nb =
                    (Binding<Object>)nbindings[didx], ob = (Binding<Object>)obindings[ii];
                nbindings[didx] = nb.merge(ob);
            } else nbindings[idx++] = obindings[ii];
        }
        Arrays.sort(nbindings);

        return new Styles(nbindings);
    }

    private Styles (Binding<?>[] bindings) {
        _bindings = bindings;
    }

    static <V> V resolveStyle (Element element, Element.State state, Style<V> style) {
        // first check for the style configured directly on the element
        Binding<V> key = new Binding<V>(style);
        V value = element.styles().<V>get(key, state);
        if (value != null) return value;

        // now check for the style in the appropriate stylesheets
        Group group = (element instanceof Group) ? (Group)element : element.parent();
        for (; group != null; group = group.parent()) {
            Stylesheet sheet = group.stylesheet();
            if (sheet == null) continue;
            value = sheet.<V>get(element.getClass(), key, state);
            if (value != null) return value;
        }

        // if we haven't found the style anywhere, return the global default
        return style.getDefault(state);
    }

    static <V> Binding<V> newBinding (Style.Binding<V> binding, Element.State state) {
        return new Binding<V>(binding, state);
    }

    static class Binding<V> implements Comparable<Binding<V>> {
        public final Style<V> style;

        public Binding (Style<V> style) {
            this.style = style;
        }

        public Binding (Style.Binding<V> binding, Element.State state) {
            this(binding.style);
            switch (state) {
            case DEFAULT: _defaultV = binding.value; break;
            case DISABLED: _disabledV = binding.value; break;
            case DOWN: _downV = binding.value; break;
            }
        }

        public Binding (Style<V> style, V defaultV, V disabledV, V downV) {
            this(style);
            _defaultV = defaultV;
            _disabledV = disabledV;
            _downV = downV;
        }

        public V get (Element.State state) {
            V value = lookup(state);
            // if we seek a binding for the non-default state and have none, fall back to a binding
            // for the default state (we want this to happen as early as possible)
            if (value == null && state != Element.State.DEFAULT) {
                value = lookup(Element.State.DEFAULT);
            }
            return value;
        }

        public Binding<V> merge (Binding<V> other) {
            return new Binding<V>(style,
                                  merge(_defaultV, other._defaultV),
                                  merge(_disabledV, other._disabledV),
                                  merge(_downV, other._downV));
        }

        public Binding<V> clear (Element.State state) {
            switch (state) {
            case DEFAULT: return new Binding<V>(style, null, _disabledV, _downV);
            case DISABLED: return new Binding<V>(style, _defaultV, null, _downV);
            case DOWN: return new Binding<V>(style, _defaultV, _disabledV, null);
            default: return this;
            }
        }

        @Override public int compareTo (Binding<V> other) {
            if (this.style == other.style) return 0;
            int hc = this.style.hashCode(), ohc = other.style.hashCode();
            assert(hc != ohc);
            return (hc < ohc) ? -1 : 1;
        }

        private V lookup (Element.State state) {
            switch (state) {
            case DEFAULT: return _defaultV;
            case DISABLED: return _disabledV;
            case DOWN: return _downV;
            default: return null;
            }
        }

        private V merge (V ours, V theirs) {
            return (theirs == null) ? ours : theirs;
        }

        // we have so few states that it's cheaper to just switch
        protected V _defaultV, _disabledV, _downV;
    }

    protected Binding<?>[] _bindings;

    protected static final Styles _noneSingleton = new Styles(new Binding<?>[0]);
}
