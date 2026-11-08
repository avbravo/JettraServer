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
 * Resizable primitive long array list with zero wrapper overhead.
 */
public class LongArrayList implements LongList {

    private static final int DEFAULT_CAPACITY = 10;
    private static final long[] EMPTY_ELEMENTS = new long[0];

    protected long[] items;
    protected int size;

    public LongArrayList() {
        this(DEFAULT_CAPACITY);
    }

    public LongArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.items = initialCapacity == 0 ? EMPTY_ELEMENTS : new long[initialCapacity];
        this.size = 0;
    }

    public static LongArrayList of(long... values) {
        LongArrayList list = new LongArrayList(values.length);
        list.addAll(values);
        return list;
    }

    public void addAll(long... values) {
        if (values == null || values.length == 0) return;
        ensureCapacity(size + values.length);
        System.arraycopy(values, 0, items, size, values.length);
        size += values.length;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(long value) {
        return indexOf(value) >= 0;
    }

    @Override
    public boolean add(long value) {
        ensureCapacity(size + 1);
        items[size++] = value;
        return true;
    }

    @Override
    public void addAtIndex(int index, long element) {
        checkIndexForAdd(index);
        ensureCapacity(size + 1);
        int numMoved = size - index;
        if (numMoved > 0) {
            System.arraycopy(items, index, items, index + 1, numMoved);
        }
        items[index] = element;
        size++;
    }

    @Override
    public long get(int index) {
        checkIndex(index);
        return items[index];
    }

    @Override
    public long set(int index, long element) {
        checkIndex(index);
        long oldValue = items[index];
        items[index] = element;
        return oldValue;
    }

    @Override
    public long removeAtIndex(int index) {
        checkIndex(index);
        long oldValue = items[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(items, index + 1, items, index, numMoved);
        }
        size--;
        return oldValue;
    }

    @Override
    public boolean remove(long value) {
        int idx = indexOf(value);
        if (idx >= 0) {
            removeAtIndex(idx);
            return true;
        }
        return false;
    }

    @Override
    public int indexOf(long value) {
        for (int i = 0; i < size; i++) {
            if (items[i] == value) return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(long value) {
        for (int i = size - 1; i >= 0; i--) {
            if (items[i] == value) return i;
        }
        return -1;
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public long[] toArray() {
        return Arrays.copyOf(items, size);
    }

    @Override
    public LongIterator longIterator() {
        return new LongIterator() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public long nextLong() {
                if (cursor >= size) throw new NoSuchElementException();
                return items[cursor++];
            }
        };
    }

    @Override
    public void forEach(LongConsumer action) {
        for (int i = 0; i < size; i++) {
            action.accept(items[i]);
        }
    }

    @Override
    public long sum() {
        long total = 0;
        for (int i = 0; i < size; i++) {
            total += items[i];
        }
        return total;
    }

    @Override
    public OptionalLong min() {
        if (size == 0) return OptionalLong.empty();
        long min = items[0];
        for (int i = 1; i < size; i++) {
            if (items[i] < min) min = items[i];
        }
        return OptionalLong.of(min);
    }

    @Override
    public OptionalLong max() {
        if (size == 0) return OptionalLong.empty();
        long max = items[0];
        for (int i = 1; i < size; i++) {
            if (items[i] > max) max = items[i];
        }
        return OptionalLong.of(max);
    }

    @Override
    public OptionalDouble average() {
        if (size == 0) return OptionalDouble.empty();
        return OptionalDouble.of((double) sum() / size);
    }

    @Override
    public boolean anySatisfy(LongPredicate predicate) {
        for (int i = 0; i < size; i++) {
            if (predicate.test(items[i])) return true;
        }
        return false;
    }

    @Override
    public boolean allSatisfy(LongPredicate predicate) {
        for (int i = 0; i < size; i++) {
            if (!predicate.test(items[i])) return false;
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
        for (int i = 0; i < size; i++) {
            if (predicate.test(items[i])) count++;
        }
        return count;
    }

    @Override
    public LongStream toStream() {
        return Arrays.stream(items, 0, size);
    }

    @Override
    public LongList sortThis() {
        Arrays.sort(items, 0, size);
        return this;
    }

    @Override
    public LongList reverseThis() {
        int half = size / 2;
        for (int i = 0; i < half; i++) {
            long temp = items[i];
            items[i] = items[size - 1 - i];
            items[size - 1 - i] = temp;
        }
        return this;
    }

    @Override
    public int binarySearch(long key) {
        return Arrays.binarySearch(items, 0, size, key);
    }

    @Override
    public LongList select(LongPredicate predicate) {
        LongArrayList result = new LongArrayList();
        for (int i = 0; i < size; i++) {
            if (predicate.test(items[i])) result.add(items[i]);
        }
        return result;
    }

    @Override
    public LongList reject(LongPredicate predicate) {
        LongArrayList result = new LongArrayList();
        for (int i = 0; i < size; i++) {
            if (!predicate.test(items[i])) result.add(items[i]);
        }
        return result;
    }

    @Override
    public LongList collect(LongUnaryOperator operator) {
        LongArrayList result = new LongArrayList(size);
        for (int i = 0; i < size; i++) {
            result.add(operator.applyAsLong(items[i]));
        }
        return result;
    }

    @Override
    public LongList toImmutable() {
        return new ImmutableLongList(toArray());
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > items.length) {
            int newCapacity = Math.max(items.length + (items.length >> 1), minCapacity);
            if (newCapacity < DEFAULT_CAPACITY) newCapacity = DEFAULT_CAPACITY;
            items = Arrays.copyOf(items, newCapacity);
        }
    }

    private void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    private void checkIndexForAdd(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(toArray());
    }
}
