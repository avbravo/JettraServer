package io.jettra.collections.set.primitive;

import io.jettra.collections.api.IntIterator;
import io.jettra.collections.api.IntSet;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

/**
 * Immutable primitive int set backed by a sorted, deduplicated flat primitive int[] array.
 * Eliminates all hash table padding overhead, yielding the absolute minimal RAM footprint (4 bytes per element).
 * Binary search provides O(log N) lookup and CPU cache-line contiguous traversal.
 */
public final class ImmutableIntHashSet implements IntSet {

    private final int[] elements;

    public ImmutableIntHashSet(int[] elements) {
        int[] sorted = elements.clone();
        Arrays.sort(sorted);
        // Deduplicate
        if (sorted.length == 0) {
            this.elements = sorted;
        } else {
            int uniqueCount = 1;
            for (int i = 1; i < sorted.length; i++) {
                if (sorted[i] != sorted[i - 1]) {
                    uniqueCount++;
                }
            }
            if (uniqueCount == sorted.length) {
                this.elements = sorted;
            } else {
                int[] unique = new int[uniqueCount];
                unique[0] = sorted[0];
                int idx = 1;
                for (int i = 1; i < sorted.length; i++) {
                    if (sorted[i] != sorted[i - 1]) {
                        unique[idx++] = sorted[i];
                    }
                }
                this.elements = unique;
            }
        }
    }

    public static ImmutableIntHashSet of(int... values) {
        return new ImmutableIntHashSet(values);
    }

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public boolean contains(int value) {
        return Arrays.binarySearch(elements, value) >= 0;
    }

    @Override
    public boolean add(int value) {
        throw new UnsupportedOperationException("ImmutableIntHashSet cannot be modified");
    }

    @Override
    public boolean remove(int value) {
        throw new UnsupportedOperationException("ImmutableIntHashSet cannot be modified");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("ImmutableIntHashSet cannot be modified");
    }

    @Override
    public int[] toArray() {
        return elements.clone();
    }

    @Override
    public IntIterator intIterator() {
        return new IntIterator() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < elements.length;
            }

            @Override
            public int nextInt() {
                if (cursor >= elements.length) {
                    throw new NoSuchElementException();
                }
                return elements[cursor++];
            }
        };
    }

    @Override
    public void forEach(IntConsumer action) {
        for (int element : elements) {
            action.accept(element);
        }
    }

    @Override
    public long sum() {
        long total = 0;
        for (int element : elements) {
            total += element;
        }
        return total;
    }

    @Override
    public OptionalInt min() {
        return elements.length == 0 ? OptionalInt.empty() : OptionalInt.of(elements[0]);
    }

    @Override
    public OptionalInt max() {
        return elements.length == 0 ? OptionalInt.empty() : OptionalInt.of(elements[elements.length - 1]);
    }

    @Override
    public OptionalDouble average() {
        return elements.length == 0 ? OptionalDouble.empty() : OptionalDouble.of((double) sum() / elements.length);
    }

    @Override
    public boolean anySatisfy(IntPredicate predicate) {
        for (int element : elements) {
            if (predicate.test(element)) return true;
        }
        return false;
    }

    @Override
    public boolean allSatisfy(IntPredicate predicate) {
        for (int element : elements) {
            if (!predicate.test(element)) return false;
        }
        return true;
    }

    @Override
    public boolean noneSatisfy(IntPredicate predicate) {
        return !anySatisfy(predicate);
    }

    @Override
    public int count(IntPredicate predicate) {
        int count = 0;
        for (int element : elements) {
            if (predicate.test(element)) count++;
        }
        return count;
    }

    @Override
    public IntStream toStream() {
        return Arrays.stream(elements);
    }

    @Override
    public IntSet select(IntPredicate predicate) {
        IntHashSet result = new IntHashSet();
        for (int element : elements) {
            if (predicate.test(element)) result.add(element);
        }
        return result.toImmutable();
    }

    @Override
    public IntSet reject(IntPredicate predicate) {
        IntHashSet result = new IntHashSet();
        for (int element : elements) {
            if (!predicate.test(element)) result.add(element);
        }
        return result.toImmutable();
    }

    @Override
    public IntSet toImmutable() {
        return this;
    }

    @Override
    public String toString() {
        return Arrays.toString(elements);
    }
}
