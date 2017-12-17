//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.Iterator;
import react.Function;

/**
 * An unordered collection of elements which may contain duplicates. Elements must not be null. The
 * elements will be reordered during normal operation of the bag. This is optimized for fast
 * additions, removals and iteration. It is not optimized for programmer ass coverage; see the
 * warnings below.
 *
 * <p><em>Note:</em> extra bounds checking is <em>not performed</em> which means that some invalid
 * operations will succeeed and return null rather than throwing {@code IndexOutOfBoundsException}.
 * Be careful.</p>
 *
 * <p><em>Note:</em> the iterator returned by {@link #iterator} does not make concurrent
 * modification checks, so concurrent modifications will cause unspecified behavior. Don't do
 * that.</p>
 */
public class Bag<E> implements Iterable<E>
{
    /** Creates an empty bag. This allows one to avoid repeating the parameter type:
     * {@code Bag<Foo> bag = Bag.create();}
     */
    public static <E> Bag<E> create () {
        return new Bag<E>();
    }

    /** Creates an empty bag with the specified initial capacity. This allows one to avoid
     * repeating the parameter type: {@code Bag<Foo> bag = Bag.create();}
     */
    public static <E> Bag<E> create (int initialCapacity) {
        return new Bag<E>(initialCapacity);
    }

    /** Creates a bag with a default initial capacity of 16. */
    public Bag () {
        this(16);
    }

    /** Creates a bag with the specified initial capacity. */
    public Bag (int initialCapacity) {
        _elems = new Object[initialCapacity];
    }

    /** Returns the number of elements in this bag.*/
    public int size () {
        return _size;
    }

    /** Returns whether this bag is empty. */
    public boolean isEmpty () {
        return _size == 0;
    }

    /** Returns the element at {@code index}. */
    public final E get (int index) {
        @SuppressWarnings("unchecked") E elem = (E)_elems[index];
        return elem;
    }

    /** Returns whether this bag contains {@code elem}. Equality is by reference. */
    public boolean contains (E elem) {
        Object[] elems = _elems;
        for (int ii = 0, ll = _size; ii < ll; ii++) {
            if (elem == elems[ii]) return true;
        }
        return false;
    }

    /** Returns whether this bag contains at least one element matching {@code pred}. */
    public boolean contains (Function<E,Boolean> pred) {
        Object[] elems = _elems;
        for (int ii = 0, ll = _size; ii < ll; ii++) {
            if (pred.apply(get(ii))) return true;
        }
        return false;
    }

    /** Adds {@code elem} to this bag. The element will always be added to the end of the bag.
     * @return the index at which the element was added.
     */
    public int add (E elem) {
        if (_size == _elems.length) expand(_elems.length*3/2+1);
        _elems[_size++] = elem;
        return _size;
    }

    /** Removes the element at the specified index.
     * @return the removed element. */
    public E removeAt (int index) {
        @SuppressWarnings("unchecked") E elem = (E)_elems[index];
        _elems[index] = _elems[--_size];
        _elems[_size] = null;
        return elem;
    }

    /** Removes the first occurrance of {@code elem} from the bag. Equality is by reference.
     * @return true if {@code elem} was found and removed, false if not.
     */
    public boolean remove (E elem) {
        Object[] elems = _elems;
        for (int ii = 0, ll = _size; ii < ll; ii++) {
            Object ee = elems[ii];
            if (ee == elem) {
                elems[ii] = elems[--_size];
                elems[_size] = null;
                return true;
            }
        }
        return false;
    }

    /** Removes all elements that match {@code pred}.
      * @return true if at least one element was found and removed, false otherwise.
      */
    public boolean removeWhere (Function<E,Boolean> pred) {
        Object[] elems = _elems;
        int removed = 0;
        for (int ii = 0, ll = _size; ii < ll; ii++) {
            if (pred.apply(get(ii))) {
                // back ii up so that we recheck the element we're swapping into place here
                elems[ii--] = elems[--_size];
                elems[_size] = null;
                removed += 1;
            }
        }
        return removed > 0;
    }

    /** Removes and returns the last element of the bag.
     * @throws ArrayIndexOutOfBoundsException if the bag is empty. */
    public E removeLast () {
        @SuppressWarnings("unchecked") E elem = (E)_elems[--_size];
        _elems[_size] = null;
        return elem;
    }

    /** Removes all elements from this bag. */
    public void removeAll () {
        Object[] elems = _elems;
        for (int ii = 0; ii < _size; ii++) elems[ii] = null;
        _size = 0;
    }

    @Override public Iterator<E> iterator () {
        return new Iterator<E>() {
            @Override public boolean hasNext() {
                return _pos < _size;
            }
            @Override public E next () {
                return get(_pos++);
            }
            @Override public void remove () {
                Bag.this.removeAt(--_pos);
            }
            protected int _pos;
        };
    }

    @Override public String toString () {
        StringBuilder buf = new StringBuilder("{");
        for (int ii = 0, ll = _size; ii < ll; ii++) {
            if (ii > 0) buf.append(",");
            buf.append(_elems[ii]);
        }
        return buf.append("}").toString();
    }

    private void expand (int capacity) {
        Object[] elems = new Object[capacity];
        System.arraycopy(_elems, 0, elems, 0, _elems.length);
        _elems = elems;
    }

    protected Object[] _elems;
    protected int _size;
}
