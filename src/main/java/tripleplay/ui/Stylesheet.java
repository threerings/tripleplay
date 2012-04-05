//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides style defaults per element type for a sub-tree of the interface hierarchy.
 */
public class Stylesheet
{
    /** Builds stylesheets, obtain via {@link #builder}. */
    public static class Builder {
        /** Adds styles for the supplied element class. If styles exist for said class, the
         * supplied styles will be merged with the existing styles (with the new styles taking
         * precedence).
         * @throws NullPointerException if styles are added after {@link #create} is called. */
        public Builder add (Class<?> eclass, Styles styles) {
            Styles ostyles = _styles.get(eclass);
            _styles.put(eclass, ostyles == null ? styles : ostyles.merge(styles));
            return this;
        }

        /** Adds styles for the supplied element class (in the DEFAULT mode).
         * @throws NullPointerException if styles are added after {@link #create} is called. */
        public Builder add (Class<?> eclass, Style.Binding<?>... styles) {
            return add(eclass, Styles.make(styles));
        }

        /** Adds styles for the supplied element class (in the specified mode).
         * @throws NullPointerException if styles are added after {@link #create} is called. */
        public Builder add (Class<?> eclass, Style.Mode mode, Style.Binding<?>... styles) {
            return add(eclass, Styles.none().add(mode, styles));
        }

        /** Creates a stylesheet with the previously configured style mappings. */
        public Stylesheet create () {
            Stylesheet sheet = new Stylesheet(_styles);
            _styles = null; // prevent further modification
            return sheet;
        }

        protected Map<Class<?>, Styles> _styles = new HashMap<Class<?>, Styles>();
    }

    /**
     * Returns a stylesheet builder.
     */
    public static Builder builder () {
        return new Builder();
    }

    /**
     * Looks up the style for the supplied key and (concrete) element class. If the style is
     * inherited, the style may be fetched from the configuration for a supertype of the supplied
     * element type. Returns null if no configuration can be found.
     */
    <V> V get (Styles.Binding<V> key, Class<?> eclass, Element<?> elem) {
        Styles styles = _styles.get(eclass);
        V value = (styles == null) ? null : styles.<V>get(key, elem);
        if (value != null) return value;

        // if the style is not inherited, or we're already checking for Element.class, then we've
        // done all the searching we can
        if (!key.style.inherited || eclass == Element.class) return null;

        // otherwise check our parent class
        Class<?> parent = eclass.getSuperclass();
        if (parent == null) {
            // TEMP: avoid confusion while PlayN POM disables class metadata by default
            throw new RuntimeException(
                "Your PlayN application must not be compiled with -XdisableClassMetadata. " +
                "It breaks TriplePlay stylesheets.");
        }
        return this.<V>get(key, parent, elem);
    }

    private Stylesheet (Map<Class<?>, Styles> styles) {
        _styles = styles;
    }

    protected final Map<Class<?>, Styles> _styles;
}
