package io.jettra.collections.list.primitive;

import io.jettra.collections.api.LongIterator;
import io.jettra.collections.api.LongList;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.function.LongUnaryOperator;
import java.util.stream.LongStream;

/**
 * Immutable primitive long list with zero wrapper overhead.
 */
public final class ImmutableLongList implements LongList {

    private final long[] items;

    public ImmutableLongList(long[] items) {
        this.items = items.clone();
    }

    public static ImmutableLongList of(long... values) {
        return new ImmutableLongList(values);
    }

    @Override
    public int size() {
        return items.length;
    }

    @Override
    public boolean contains(long value) {
        return indexOf(value) >= 0;
    }

    @Override
    public boolean add(long value) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public void addAtIndex(int index, long element) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public long get(int index) {
        return items[index];
    }

    @Override
    public long set(int index, long element) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public long removeAtIndex(int index) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public boolean remove(long value) {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public int indexOf(long value) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == value) return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(long value) {
        for (int i = items.length - 1; i >= 0; i--) {
            if (items[i] == value) return i;
        }
        return -1;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public long[] toArray() {
        return items.clone();
    }

    @Override
    public LongIterator longIterator() {
        return new LongIterator() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < items.length;
            }

            @Override
            public long nextLong() {
                if (cursor >= items.length) throw new NoSuchElementException();
                return items[cursor++];
            }
        };
    }

    @Override
    public void forEach(LongConsumer action) {
        for (long item : items) {
            action.accept(item);
        }
    }

    @Override
    public long sum() {
        long total = 0;
        for (long item : items) total += item;
        return total;
    }

    @Override
    public OptionalLong min() {
        if (items.length == 0) return OptionalLong.empty();
        long min = items[0];
        for (int i = 1; i < items.length; i++) {
            if (items[i] < min) min = items[i];
        }
        return OptionalLong.of(min);
    }

    @Override
    public OptionalLong max() {
        if (items.length == 0) return OptionalLong.empty();
        long max = items[0];
        for (int i = 1; i < items.length; i++) {
            if (items[i] > max) max = items[i];
        }
        return OptionalLong.of(max);
    }

    @Override
    public OptionalDouble average() {
        if (items.length == 0) return OptionalDouble.empty();
        return OptionalDouble.of((double) sum() / items.length);
    }

    @Override
    public boolean anySatisfy(LongPredicate predicate) {
        for (long item : items) {
            if (predicate.test(item)) return true;
        }
        return false;
    }

    @Override
    public boolean allSatisfy(LongPredicate predicate) {
        for (long item : items) {
            if (!predicate.test(item)) return false;
        }
        return true;
    }

    @Override
    public boolean noneSatisfy(LongPredicate predicate) {
        return !anySatisfy(predicate);
    }

    @Override
    public int count(LongPredicate predicate) {
        int count = 0;
        for (long item : items) {
            if (predicate.test(item)) count++;
        }
        return count;
    }

    @Override
    public LongStream toStream() {
        return Arrays.stream(items);
    }

    @Override
    public LongList sortThis() {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public LongList reverseThis() {
        throw new UnsupportedOperationException("Immutable");
    }

    @Override
    public int binarySearch(long key) {
        return Arrays.binarySearch(items, key);
    }

    @Override
    public LongList select(LongPredicate predicate) {
        LongArrayList result = new LongArrayList();
        for (long item : items) {
            if (predicate.test(item)) result.add(item);
        }
        return result.toImmutable();
    }

    @Override
    public LongList reject(LongPredicate predicate) {
        LongArrayList result = new LongArrayList();
        for (long item : items) {
            if (!predicate.test(item)) result.add(item);
        }
        return result.toImmutable();
    }

    @Override
    public LongList collect(LongUnaryOperator operator) {
        long[] res = new long[items.length];
        for (int i = 0; i < items.length; i++) {
            res[i] = operator.applyAsLong(items[i]);
        }
        return new ImmutableLongList(res);
    }

    @Override
    public LongList toImmutable() {
        return this;
    }

    @Override
    public String toString() {
        return Arrays.toString(items);
    }
}
