//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import react.Closeable;

/**
 * Maintains a reference to a resource. Handles destroying the resource before releasing the
 * reference.
 */
public abstract class Ref<T>
{
    /** Creates a reference to a {@link Closeable} target. */
    public static <T extends Closeable> Ref<T> create (T target) {
        Ref<T> ref = new Ref<T>() {
            @Override protected void onClear (T target) {
                target.close();
            }
        };
        ref.set(target);
        return ref;
    }

    /** Returns the current value of this reference, which may be null. */
    public T get () {
        return _target;
    }

    /** Sets the current value of this reference, clearing any previously referenced object.
     * @return {@code target} to enabled code like: {@code F foo = fooref.set(new F())}. */
    public T set (T target) {
        clear();
        _target = target;
        return target;
    }

    /** Clears the target of this reference. Automatically calls {@link #onClear} if the reference
     * contains a non-null target. */
    public void clear () {
        if (_target != null) {
            T toBeCleared = _target;
            _target = null;
            onClear(toBeCleared);
        }
    }

    /** Performs any cleanup on the supplied target (which has been just cleared). */
    protected abstract void onClear (T target);

    protected T _target;
}
