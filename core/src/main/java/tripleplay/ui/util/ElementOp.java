//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui.util;

import tripleplay.ui.Element;

/**
 * Defines a method that performs an operation on an element.
 * @param <T> the leaf type of Element
 */
public interface ElementOp<T extends Element<?>>
{
    /**
     * Defines element operations for changing the enabled state of an element. Usage:
     * <pre><code>
     *     Hierarchy.of(elem).apply(SetEnabled.to(false));
     * </code></pre>
     */
    public class SetEnabled implements ElementOp<Element<?>>
    {
        /** Enables an element. */
        public static final ElementOp<Element<?>> TRUE = new SetEnabled(true);

        /** Disables an element. */
        public static final ElementOp<Element<?>> FALSE = new SetEnabled(false);

        /**
         * Gets the operation that will set the enabling of an element to the given state.
         */
        public static ElementOp<Element<?>> to (boolean enabled) {
            return enabled ? TRUE : FALSE;
        }

        /** The enabled value of the op. */
        public final boolean value;

        /** Creates a new operation. */
        public SetEnabled (boolean value) {
            this.value = value;
        }

        @Override public void perform (Element<?> elem) {
            elem.setEnabled(value);
        }
    }

    /**
     * Performs an arbitrary operation on the given element.
     */
    void perform (T elem);
}
