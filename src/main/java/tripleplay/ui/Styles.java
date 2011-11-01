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

    /** Creates a styles instance with the supplied style bindings in the DEFAULT mode. */
    public static Styles make (Style.Binding<?>... bindings) {
        return none().add(Style.Mode.DEFAULT, bindings);
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles in the default mode. The receiver is not modified.
     */
    public Styles add (Style.Binding<?>... bindings) {
        return add(Style.Mode.DEFAULT, bindings);
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles in the disabled mode. The receiver is not modified.
     */
    public Styles addDisabled (Style.Binding<?>... bindings) {
        return add(Style.Mode.DISABLED, bindings);
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles in the selected mode. The receiver is not modified.
     */
    public Styles addSelected (Style.Binding<?>... bindings) {
        return add(Style.Mode.SELECTED, bindings);
    }

    /**
     * Returns a new instance where the supplied bindings overwrite any previous bindings for the
     * specified styles in the disabled selected mode. The receiver is not modified.
     */
    public Styles addDisabledSelected (Style.Binding<?>... bindings) {
        return add(Style.Mode.DISABLED_SELECTED, bindings);
    }

    /**
     * Returns a new instance where no binding exists for the specified style in the specified
     * state. The receiver is not modified.
     */
    public <V> Styles clear (Style.Mode mode, Style<V> style) {
        int index = Arrays.binarySearch(_bindings, new Binding<V>(style));
        if (index < 0) return this;
        @SuppressWarnings("unchecked") Binding<V> binding = (Binding<V>)_bindings[index];
        Binding<?>[] nbindings = new Binding<?>[_bindings.length];
        System.arraycopy(_bindings, 0, nbindings, 0, nbindings.length);
        nbindings[index] = binding.clear(mode);
        return new Styles(nbindings);
    }

    /**
     * Returns a new styles instance which merges these styles with the supplied styles. Where both
     * instances define a particular style, the supplied {@code styles} will take precedence.
     */
    public Styles merge (Styles styles) {
        return merge(styles._bindings);
    }

    <V> V get (Binding<V> key, Element<?> elem) {
        int index = Arrays.binarySearch(_bindings, key);
        if (index < 0) return null;
        @SuppressWarnings("unchecked") Binding<V> binding = (Binding<V>)_bindings[index];
        return binding.get(elem);
    }

   /**
    * Returns a new instance where the supplied bindings overwrite any previous bindings for the
    * specified styles (in the specified mode). The receiver is not modified.
    */
   private Styles add (Style.Mode mode, Style.Binding<?>... bindings) {
       Binding<?>[] nbindings = new Binding<?>[bindings.length];
       for (int ii = 0; ii < bindings.length; ii++) {
           nbindings[ii] = newBinding(bindings[ii], mode);
       }
       // note that we take advantage of the fact that merge can handle unsorted bindings
       return merge(nbindings);
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

    static <V> V resolveStyle (Element<?> element, Style<V> style) {
        // first check for the style configured directly on the element
        Binding<V> key = new Binding<V>(style);
        V value = element.styles().<V>get(key, element);
        if (value != null) return value;

        // now check for the style in the appropriate stylesheets
        Elements<?> group = (element instanceof Group) ? (Group)element : element.parent();
        for (; group != null; group = group.parent()) {
            Stylesheet sheet = group.stylesheet();
            if (sheet == null) continue;
            value = sheet.<V>get(key, element.getClass(), element);
            if (value != null) return value;
        }

        // if we haven't found the style anywhere, return the global default
        return style.getDefault(element);
    }

    static <V> Binding<V> newBinding (Style.Binding<V> binding, Style.Mode mode) {
        return new Binding<V>(binding, mode);
    }

    static class Binding<V> implements Comparable<Binding<V>> {
        public final Style<V> style;

        public Binding (Style<V> style) {
            this.style = style;
        }

        public Binding (Style.Binding<V> binding, Style.Mode mode) {
            this(binding.style);
            switch (mode) {
            case DEFAULT: _defaultV = binding.value; break;
            case DISABLED: _disabledV = binding.value; break;
            case SELECTED: _selectedV = binding.value; break;
            case DISABLED_SELECTED: _disSelectedV = binding.value; break;
            }
        }

        public Binding (Style<V> style, V defaultV, V disabledV, V selectedV, V disSelectedV) {
            this(style);
            _defaultV = defaultV;
            _disabledV = disabledV;
            _selectedV = selectedV;
            _disSelectedV = disSelectedV;
        }

        public V get (Element<?> elem) {
            // prioritize as: disabled_selected, disabled, selected, default
            if (elem.isEnabled()) {
                if (elem.isSelected() && _selectedV != null) return _selectedV;
            } else {
                if (elem.isSelected()) {
                    if (_disSelectedV != null) return _disSelectedV;
                } else {
                    if (_disabledV != null) return _disabledV;
                }
            }
            return _defaultV;
        }

        public Binding<V> merge (Binding<V> other) {
            return new Binding<V>(style,
                                  merge(_defaultV, other._defaultV),
                                  merge(_disabledV, other._disabledV),
                                  merge(_selectedV, other._selectedV),
                                  merge(_disSelectedV, other._disSelectedV));
        }

        public Binding<V> clear (Style.Mode mode) {
            switch (mode) {
            case DEFAULT: return new Binding<V>(style, null, _disabledV, _selectedV, _disSelectedV);
            case DISABLED: return new Binding<V>(style, _defaultV, null, _selectedV, _disSelectedV);
            case SELECTED: return new Binding<V>(style, _defaultV, _disabledV, null, _disSelectedV);
            case DISABLED_SELECTED:
                return new Binding<V>(style, _defaultV, _disabledV, _selectedV, null);
            default: return this;
            }
        }

        @Override public int compareTo (Binding<V> other) {
            if (this.style == other.style) return 0;
            int hc = this.style.hashCode(), ohc = other.style.hashCode();
            assert(hc != ohc);
            return (hc < ohc) ? -1 : 1;
        }

        private V merge (V ours, V theirs) {
            return (theirs == null) ? ours : theirs;
        }

        protected V _defaultV, _disabledV, _selectedV, _disSelectedV;
    }

    protected Binding<?>[] _bindings;

    protected static final Styles _noneSingleton = new Styles(new Binding<?>[0]);
}
