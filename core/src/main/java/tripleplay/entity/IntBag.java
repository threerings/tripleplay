//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.entity;

/** An unordered bag of ints. Used internally by entity things. */
public class IntBag implements System.Entities
{
    public IntBag () {
        _elems = new int[16];
    }

    public int size () { return _size; }
    public boolean isEmpty () { return _size == 0; }
    public int get (int index) { return _elems[index]; }

    public boolean contains (int elem) {
        for (int ii = 0, ll = _size; ii < ll; ii++) {
            if (elem == _elems[ii]) return true;
        }
        return false;
    }

    public int add (int elem) {
        if (_size == _elems.length) expand(_elems.length*3/2+1);
        _elems[_size++] = elem;
        return _size;
    }

    public int removeAt (int index) {
        int elem = _elems[index];
        _elems[index] = _elems[--_size];
        return elem;
    }
    public int remove (int elem) {
        for (int ii = 0, ll = _size; ii < ll; ii++) {
            int ee = _elems[ii];
            if (ee == elem) {
                _elems[ii] = _elems[--_size];
                return ii;
            }
        }
        return -1;
    }
    public int removeLast () { return _elems[--_size]; }
    public void removeAll () { _size = 0; }

    @Override public String toString () {
        StringBuilder buf = new StringBuilder("{");
        for (int ii = 0, ll = _size; ii < ll; ii++) {
            if (ii > 0) buf.append(",");
            buf.append(_elems[ii]);
        }
        return buf.append("}").toString();
    }

    private void expand (int capacity) {
        int[] elems = new int[capacity];
        java.lang.System.arraycopy(_elems, 0, elems, 0, _elems.length);
        _elems = elems;
    }

    protected int[] _elems;
    protected int _size;
}
