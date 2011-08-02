//
// Triple Play - utilities for use in ForPlay-based games
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
        /** Adds styles for the supplied element class.
         * @throws IllegalStateException if styles already exist for said class.
         * @throws NullPointerException if styles are added after {@link #create} is called. */
        public Builder add (Class<?> eclass, Styles styles) {
            Styles ostyles = _styles.put(eclass, styles);
            if (ostyles != null) {
                throw new IllegalStateException("Already have style mappings for " + eclass);
            }
            return this;
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
    <V> V get (Class<?> eclass, Styles.Key key) {
        Styles styles = _styles.get(eclass);
        V value = (styles == null) ? null : styles.<V>get(key);
        if (value != null) return value;

        // if we found no mapping and the style is inherited (and we're not already at the root --
        // Element), then check the parent type of this element for a mapping
        return (key.style.inherited && eclass != Element.class) ?
            this.<V>get(eclass.getSuperclass(), key) : null;
    }

    private Stylesheet (Map<Class<?>, Styles> styles) {
        _styles = styles;
    }

    protected final Map<Class<?>, Styles> _styles;
}
