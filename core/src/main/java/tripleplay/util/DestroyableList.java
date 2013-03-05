//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Maintains a list of {@link Destroyable}s. When an object is removed from the list or the list is
 * cleared, it is {@link Destroyable#destroy}ed.
 *
 * <p> Note that this is not a proper {@code java.util.List} because that interface exposes a ton
 * of different ways to manipulate the list, all of which make it very hard to reason about when or
 * whether your destroyables are destroyed. This list supports addition, removal, iteration and
 * random access. That's about it.
 */
public class DestroyableList<E extends Destroyable> implements Iterable<E>
{
    /**
     * Constructs an empty list with an initial capacity of eight.
     */
    public static <E extends Destroyable> DestroyableList<E> create () {
        return new DestroyableList<E>(8);
    }

    /**
     * Constructs an empty list with the specified initial capacity.
     */
    public static <E extends Destroyable> DestroyableList<E> createWithCapacity (int initCap) {
        return new DestroyableList<E>(initCap);
    }

    protected DestroyableList (int initCap) {
        if (initCap < 0) throw new IllegalArgumentException("Illegal Capacity: "+ initCap);
        _data = new Object[initCap];
    }

    /** Returns the size of the list. */
    public int size() {
        return _size;
    }

    /** Returns true if the list is empty, false if it contains elements. */
    public boolean isEmpty () {
        return _size == 0;
    }

    /** Returns the element at {@code index}. */
    public E get (int index) {
        if (index >= _size) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        @SuppressWarnings("unchecked") E elem = (E) _data[index];
        return elem;
    }

    /** Adds (non-null) {@code elem} to the end of the list.
     * @return {@code elem} for convenience. */
    public E add (E elem) {
        if (elem == null) throw new NullPointerException();
        ensureCapacity(_size + 1);
        _data[_size++] = elem;
        return elem;
    }

    /** Inserts (non-null) {@code elem} into the list at {@code index}.
     * @return {@code elem} for convenience.  */
    public E add (int index, E elem) {
        if (index > _size || index < 0) throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
        if (elem == null) throw new NullPointerException();
        ensureCapacity(_size+1);
        System.arraycopy(_data, index, _data, index+1, _size-index);
        _data[index] = elem;
        _size++;
        return elem;
    }

    /** Removes and destroys the element at {@code index}.
     * @return the removed and destroyed element. */
    public E remove (int index) {
        E oldValue = get(index);
        _modCount++;
        int numMoved = _size - index - 1;
        if (numMoved > 0) System.arraycopy(_data, index+1, _data, index, numMoved);
        _data[--_size] = null;
        oldValue.destroy();
        return oldValue;
    }

    /** Removes and destroys (non-null) {@code elem}.
     * @return true if the element was found and destroyed, false otherwise. */
    public boolean remove (E elem) {
        if (elem == null) throw new NullPointerException();
        Object[] data = _data;
        for (int ii = 0, ll = _size; ii < ll; ii++) {
            if (elem.equals(data[ii])) {
                remove(ii);
                return true;
            }
        }
        return false;
    }

    /** Removes and destroys all elements in this list. */
    public void clear () {
        _modCount++;
        Object[] data = _data;
        for (int ii = 0, ll = _size; ii < ll; ii++) {
            @SuppressWarnings("unchecked") E elem = (E)data[ii];
            elem.destroy();
            data[ii] = null;
        }
        _size = 0;
    }

    @Override public Iterator<E> iterator () {
        return new Iterator<E>() {
            @Override public boolean hasNext () {
                return _cursor != _size;
            }

            @Override public E next () {
                checkForComodification();
                int ii = _cursor;
                if (ii >= _size) throw new NoSuchElementException();
                Object[] data = _data;
                if (ii >= data.length) throw new ConcurrentModificationException();
                _cursor = ii + 1;
                @SuppressWarnings("unchecked") E elem = (E) data[_lastRet = ii];
                return elem;
            }

            @Override public void remove () {
                if (_lastRet < 0) throw new IllegalStateException();
                checkForComodification();
                try {
                    DestroyableList.this.remove(_lastRet);
                    _cursor = _lastRet;
                    _lastRet = -1;
                    _exModCount = _modCount;
                } catch (IndexOutOfBoundsException ex) {
                    throw new ConcurrentModificationException();
                }
            }

            final void checkForComodification() {
                if (_modCount != _exModCount) throw new ConcurrentModificationException();
            }

            protected int _cursor;       // index of next element to return
            protected int _lastRet = -1; // index of last element returned; -1 if no such
            protected int _exModCount = _modCount;
        };
    }

    protected final void ensureCapacity (int minCapacity) {
        _modCount++;
        int oldCapacity = _data.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3)/2 + 1;
            if (newCapacity < minCapacity) newCapacity = minCapacity;
            // minCapacity is usually close to size, so this is a win:
            Object[] newData = new Object[newCapacity];
            System.arraycopy(_data, 0, newData, 0, oldCapacity);
            _data = newData;
        }
    }

    protected String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + _size;
    }

    protected transient Object[] _data;
    protected int _size;
    protected int _modCount;
}
