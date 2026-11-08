package io.jettra.collections.list.primitive;

import io.jettra.collections.api.IntIterator;
import io.jettra.collections.api.IntList;
import io.jettra.collections.api.IntToIntFunction;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

/**
 * Immutable, exact-fit primitive int list.
 * Thread-safe and zero allocation overhead.
 */
public final class ImmutableIntList implements IntList {

    private final int[] items;

    public ImmutableIntList(int[] items) {
        this.items = items.clone();
    }

    public static ImmutableIntList of(int... values) {
        return new ImmutableIntList(values);
    }

    @Override
    public int size() {
        return items.length;
    }

    @Override
    public boolean contains(int value) {
        return indexOf(value) >= 0;
    }

    @Override
    public boolean add(int value) {
        throw new UnsupportedOperationException("Cannot modify ImmutableIntList");
    }

    @Override
    public void addAtIndex(int index, int element) {
        throw new UnsupportedOperationException("Cannot modify ImmutableIntList");
    }

    @Override
    public int get(int index) {
        return items[index];
    }

    @Override
    public int set(int index, int element) {
        throw new UnsupportedOperationException("Cannot modify ImmutableIntList");
    }

    @Override
    public int removeAtIndex(int index) {
        throw new UnsupportedOperationException("Cannot modify ImmutableIntList");
    }

    @Override
    public boolean remove(int value) {
        throw new UnsupportedOperationException("Cannot modify ImmutableIntList");
    }

    @Override
    public int indexOf(int value) {
        for (int i = 0; i < items.length; i++) {
            if (items[i] == value) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(int value) {
        for (int i = items.length - 1; i >= 0; i--) {
            if (items[i] == value) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot modify ImmutableIntList");
    }

    @Override
    public int[] toArray() {
        return items.clone();
    }

    @Override
    public IntIterator intIterator() {
        return new IntIterator() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < items.length;
            }

            @Override
            public int nextInt() {
                if (cursor >= items.length) {
                    throw new NoSuchElementException();
                }
                return items[cursor++];
            }
        };
    }

    @Override
    public void forEach(java.util.function.IntConsumer action) {
        for (int item : items) {
            action.accept(item);
        }
    }

    @Override
    public long sum() {
        long total = 0;
        for (int item : items) {
            total += item;
        }
        return total;
    }

    @Override
    public OptionalInt min() {
        if (items.length == 0) return OptionalInt.empty();
        int min = items[0];
        for (int i = 1; i < items.length; i++) {
            if (items[i] < min) min = items[i];
        }
        return OptionalInt.of(min);
    }

    @Override
    public OptionalInt max() {
        if (items.length == 0) return OptionalInt.empty();
        int max = items[0];
        for (int i = 1; i < items.length; i++) {
            if (items[i] > max) max = items[i];
        }
        return OptionalInt.of(max);
    }

    @Override
    public OptionalDouble average() {
        if (items.length == 0) return OptionalDouble.empty();
        return OptionalDouble.of((double) sum() / items.length);
    }

    @Override
    public boolean anySatisfy(java.util.function.IntPredicate predicate) {
        for (int item : items) {
            if (predicate.test(item)) return true;
        }
        return false;
    }

    @Override
    public boolean allSatisfy(java.util.function.IntPredicate predicate) {
        for (int item : items) {
            if (!predicate.test(item)) return false;
        }
        return true;
    }

    @Override
    public boolean noneSatisfy(java.util.function.IntPredicate predicate) {
        return !anySatisfy(predicate);
    }

    @Override
    public int count(java.util.function.IntPredicate predicate) {
        int count = 0;
        for (int item : items) {
            if (predicate.test(item)) count++;
        }
        return count;
    }

    @Override
    public IntStream toStream() {
        return Arrays.stream(items);
    }

    @Override
    public IntList sortThis() {
        throw new UnsupportedOperationException("Cannot modify ImmutableIntList");
    }

    @Override
    public IntList reverseThis() {
        throw new UnsupportedOperationException("Cannot modify ImmutableIntList");
    }

    @Override
    public int binarySearch(int key) {
        return Arrays.binarySearch(items, key);
    }

    @Override
    public IntList select(java.util.function.IntPredicate predicate) {
        IntArrayList result = new IntArrayList();
        for (int item : items) {
            if (predicate.test(item)) result.add(item);
        }
        return result.toImmutable();
    }

    @Override
    public IntList reject(java.util.function.IntPredicate predicate) {
        IntArrayList result = new IntArrayList();
        for (int item : items) {
            if (!predicate.test(item)) result.add(item);
        }
        return result.toImmutable();
    }

    @Override
    public IntList collect(IntToIntFunction function) {
        int[] transformed = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            transformed[i] = function.applyAsInt(items[i]);
        }
        return new ImmutableIntList(transformed);
    }

    @Override
    public IntList toImmutable() {
        return this;
    }

    @Override
    public String toString() {
        return Arrays.toString(items);
    }
}
