//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.RandomAccess;

/**
 * Provides utility routines to simplify obtaining randomized values.
 */
public class Randoms
{
    /**
     * A factory to create a new Randoms object.
     */
    public static Randoms with (Random rand) {
        return new Randoms(rand);
    }

    /**
     * Returns a pseudorandom, uniformly distributed {@code int} value between {@code 0}
     * (inclusive) and {@code high} (exclusive).
     *
     * @param high the high value limiting the random number sought.
     *
     * @throws IllegalArgumentException if {@code high} is not positive.
     */
    public int getInt (int high) {
        return _r.nextInt(high);
    }

    /**
     * Returns a pseudorandom, uniformly distributed {@code int} value between {@code low}
     * (inclusive) and {@code high} (exclusive).
     *
     * @throws IllegalArgumentException if {@code high - low} is not positive.
     */
    public int getInRange (int low, int high) {
        return low + _r.nextInt(high - low);
    }

    /**
     * Returns a pseudorandom, uniformly distributed {@code float} value between {@code 0.0}
     * (inclusive) and the {@code high} (exclusive).
     *
     * @param high the high value limiting the random number sought.
     */
    public float getFloat (float high) {
        return _r.nextFloat() * high;
    }

    /**
     * Returns a pseudorandom, uniformly distributed {@code float} value between {@code low}
     * (inclusive) and {@code high} (exclusive).
     */
    public float getInRange (float low, float high) {
        return low + (_r.nextFloat() * (high - low));
    }

    /**
     * Returns true approximately one in {@code n} times.
     *
     * @throws IllegalArgumentException if {@code n} is not positive.
     */
    public boolean getChance (int n) {
        return (0 == _r.nextInt(n));
    }

    /**
     * Has a probability {@code p} of returning true.
     */
    public boolean getProbability (float p) {
        return _r.nextFloat() < p;
    }

    /**
     * Returns {@code true} or {@code false} with approximately even probability.
     */
    public boolean getBoolean () {
        return _r.nextBoolean();
    }

    /**
     * Returns a pseudorandom, normally distributed {@code float} value around the {@code mean}
     * with the standard deviation {@code dev}.
     */
    public float getNormal (float mean, float dev) {
        return (float)_r.nextGaussian() * dev + mean;
    }

    /**
     * Shuffle the specified list using our Random.
     */
    public <T> void shuffle (List<T> list) {
        // we can't use Collections.shuffle here because GWT doesn't implement it
        int size = list.size();
        if (list instanceof RandomAccess) {
            for (int ii = size; ii > 1; ii--) {
                swap(list, ii-1, _r.nextInt(ii));
            }

        } else {
            Object[] array = list.toArray();
            for (int ii = size; ii > 1; ii--) {
                swap(array, ii-1, _r.nextInt(ii));
            }
            ListIterator<T> it = list.listIterator();
            for (int ii = 0; ii < size; ii++) {
                it.next();
                @SuppressWarnings("unchecked") T elem = (T)array[ii];
                it.set(elem);
            }
        }
    }

    /**
     * Pick a random element from the specified Iterator, or return {@code ifEmpty} if it is empty.
     *
     * <p><b>Implementation note:</b> because the total size of the Iterator is not known,
     * the random number generator is queried after the second element and every element
     * thereafter.
     *
     * @throws NullPointerException if the iterator is null.
     */
    public <T> T pick (Iterator<? extends T> iterator, T ifEmpty) {
        if (!iterator.hasNext()) {
            return ifEmpty;
        }
        T pick = iterator.next();
        for (int count = 2; iterator.hasNext(); count++) {
            T next = iterator.next();
            if (0 == _r.nextInt(count)) {
                pick = next;
            }
        }
        return pick;
    }

    /**
     * Pick a random element from the specified Iterable, or return {@code ifEmpty} if it is empty.
     *
     * <p><b>Implementation note:</b> optimized implementations are used if the Iterable
     * is a List or Collection. Otherwise, it behaves as if calling {@link #pick(Iterator, Object)}
     * with the Iterable's Iterator.
     *
     * @throws NullPointerException if the iterable is null.
     */
    public <T> T pick (Iterable<? extends T> iterable, T ifEmpty) {
        return pickPluck(iterable, ifEmpty, false);
    }

    /**
     * Pick a random <em>key</em> from the specified mapping of weight values, or return {@code
     * ifEmpty} if no mapping has a weight greater than {@code 0}. Each weight value is evaluated
     * as a double.
     *
     * <p><b>Implementation note:</b> a random number is generated for every entry with a
     * non-zero weight after the first such entry.
     *
     * @throws NullPointerException if the map is null.
     * @throws IllegalArgumentException if any weight is less than 0.
     */
    public <T> T pick (Map<? extends T, ? extends Number> weightMap, T ifEmpty) {
        T pick = ifEmpty;
        double total = 0.0;
        for (Map.Entry<? extends T, ? extends Number> entry : weightMap.entrySet()) {
            double weight = entry.getValue().doubleValue();
            if (weight > 0.0) {
                total += weight;
                if ((total == weight) || ((_r.nextDouble() * total) < weight)) {
                    pick = entry.getKey();
                }
            } else if (weight < 0.0) {
                throw new IllegalArgumentException("Weight less than 0: " + entry);
            } // else: weight == 0.0 is OK
        }
        return pick;
    }

    /**
     * Pluck (remove) a random element from the specified Iterable, or return {@code ifEmpty} if it
     * is empty.
     *
     * <p><b>Implementation note:</b> optimized implementations are used if the Iterable
     * is a List or Collection. Otherwise, two Iterators are created from the Iterable
     * and a random number is generated after the second element and all beyond.
     *
     * @throws NullPointerException if the iterable is null.
     * @throws UnsupportedOperationException if the iterable is unmodifiable or its Iterator
     * does not support {@link Iterator#remove()}.
     */
    public <T> T pluck (Iterable<? extends T> iterable, T ifEmpty) {
        return pickPluck(iterable, ifEmpty, true);
    }

    /**
     * Construct a Randoms.
     */
    protected Randoms (Random rand) {
        _r = rand;
    }

    /**
     * Shared code for pick and pluck.
     */
    protected <T> T pickPluck (Iterable<? extends T> iterable, T ifEmpty, boolean remove) {
        if (iterable instanceof Collection) {
            // optimized path for Collection
            Collection<? extends T> coll = (Collection<? extends T>)iterable;
            int size = coll.size();
            if (size == 0) {
                return ifEmpty;
            }
            if (coll instanceof List) {
                // extra-special optimized path for Lists
                List<? extends T> list = (List<? extends T>)coll;
                int idx = _r.nextInt(size);
                if (remove) {
                  return list.remove(idx);
                } else {
                  return list.get(idx);
                }
            }
            // for other Collections, we must iterate
            Iterator<? extends T> it = coll.iterator();
            for (int idx = _r.nextInt(size); idx > 0; idx--) {
                it.next();
            }
            try {
                return it.next();
            } finally {
                if (remove) {
                    it.remove();
                }
            }
        }

        if (!remove) {
            return pick(iterable.iterator(), ifEmpty);
        }

        // from here on out, we're doing a pluck with a complicated two-iterator solution
        Iterator<? extends T> it = iterable.iterator();
        if (!it.hasNext()) {
            return ifEmpty;
        }
        Iterator<? extends T> lagIt = iterable.iterator();
        T pick = it.next();
        lagIt.next();
        for (int count = 2, lag = 1; it.hasNext(); count++, lag++) {
            T next = it.next();
            if (0 == _r.nextInt(count)) {
                pick = next;
                // catch up lagIt so that it has just returned 'pick' as well
                for ( ; lag > 0; lag--) {
                    lagIt.next();
                }
            }
        }
        lagIt.remove(); // remove 'pick' from the lagging iterator
        return pick;
    }

    /** Helper for {@link #shuffle}. */
    protected static <T> void swap (List<T> list, int ii, int jj) {
        list.set(ii, list.set(jj, list.get(ii)));
    }

    /** Helper for {@link #shuffle}. */
    protected static void swap (Object[] array, int ii, int jj) {
        Object tmp = array[ii];
        array[ii] = array[jj];
        array[jj] = tmp;
    }

    /** The random number generator. */
    protected final Random _r;
}
