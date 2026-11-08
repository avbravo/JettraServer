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
 * High-performance, resizable primitive int array list.
 * Stores data directly in a contiguous primitive int[] array,
 * eliminating 100% of object wrapper overhead (Integer boxing).
 */
public class IntArrayList implements IntList {

    private static final int DEFAULT_CAPACITY = 10;
    private static final int[] EMPTY_ELEMENTS = new int[0];

    protected int[] items;
    protected int size;

    public IntArrayList() {
        this(DEFAULT_CAPACITY);
    }

    public IntArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.items = initialCapacity == 0 ? EMPTY_ELEMENTS : new int[initialCapacity];
        this.size = 0;
    }

    public static IntArrayList of(int... values) {
        IntArrayList list = new IntArrayList(values.length);
        list.addAll(values);
        return list;
    }

    public void addAll(int... values) {
        if (values == null || values.length == 0) {
            return;
        }
        ensureCapacity(size + values.length);
        System.arraycopy(values, 0, items, size, values.length);
        size += values.length;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(int value) {
        return indexOf(value) >= 0;
    }

    @Override
    public boolean add(int value) {
        ensureCapacity(size + 1);
        items[size++] = value;
        return true;
    }

    @Override
    public void addAtIndex(int index, int element) {
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
    public int get(int index) {
        checkIndex(index);
        return items[index];
    }

    @Override
    public int set(int index, int element) {
        checkIndex(index);
        int oldValue = items[index];
        items[index] = element;
        return oldValue;
    }

    @Override
    public int removeAtIndex(int index) {
        checkIndex(index);
        int oldValue = items[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(items, index + 1, items, index, numMoved);
        }
        size--;
        return oldValue;
    }

    @Override
    public boolean remove(int value) {
        int idx = indexOf(value);
        if (idx >= 0) {
            removeAtIndex(idx);
            return true;
        }
        return false;
    }

    @Override
    public int indexOf(int value) {
        for (int i = 0; i < size; i++) {
            if (items[i] == value) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(int value) {
        for (int i = size - 1; i >= 0; i--) {
            if (items[i] == value) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public int[] toArray() {
        return Arrays.copyOf(items, size);
    }

    @Override
    public IntIterator intIterator() {
        return new IntIterator() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public int nextInt() {
                if (cursor >= size) {
                    throw new NoSuchElementException();
                }
                return items[cursor++];
            }
        };
    }

    @Override
    public void forEach(java.util.function.IntConsumer action) {
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
    public OptionalInt min() {
        if (size == 0) {
            return OptionalInt.empty();
        }
        int min = items[0];
        for (int i = 1; i < size; i++) {
            if (items[i] < min) {
                min = items[i];
            }
        }
        return OptionalInt.of(min);
    }

    @Override
    public OptionalInt max() {
        if (size == 0) {
            return OptionalInt.empty();
        }
        int max = items[0];
        for (int i = 1; i < size; i++) {
            if (items[i] > max) {
                max = items[i];
            }
        }
        return OptionalInt.of(max);
    }

    @Override
    public OptionalDouble average() {
        if (size == 0) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of((double) sum() / size);
    }

    @Override
    public boolean anySatisfy(java.util.function.IntPredicate predicate) {
        for (int i = 0; i < size; i++) {
            if (predicate.test(items[i])) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean allSatisfy(java.util.function.IntPredicate predicate) {
        for (int i = 0; i < size; i++) {
            if (!predicate.test(items[i])) {
                return false;
            }
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
        for (int i = 0; i < size; i++) {
            if (predicate.test(items[i])) {
                count++;
            }
        }
        return count;
    }

    @Override
    public IntStream toStream() {
        return Arrays.stream(items, 0, size);
    }

    @Override
    public IntList sortThis() {
        Arrays.sort(items, 0, size);
        return this;
    }

    @Override
    public IntList reverseThis() {
        int half = size / 2;
        for (int i = 0; i < half; i++) {
            int temp = items[i];
            items[i] = items[size - 1 - i];
            items[size - 1 - i] = temp;
        }
        return this;
    }

    @Override
    public int binarySearch(int key) {
        return Arrays.binarySearch(items, 0, size, key);
    }

    @Override
    public IntList select(java.util.function.IntPredicate predicate) {
        IntArrayList result = new IntArrayList();
        for (int i = 0; i < size; i++) {
            if (predicate.test(items[i])) {
                result.add(items[i]);
            }
        }
        return result;
    }

    @Override
    public IntList reject(java.util.function.IntPredicate predicate) {
        IntArrayList result = new IntArrayList();
        for (int i = 0; i < size; i++) {
            if (!predicate.test(items[i])) {
                result.add(items[i]);
            }
        }
        return result;
    }

    @Override
    public IntList collect(IntToIntFunction function) {
        IntArrayList result = new IntArrayList(size);
        for (int i = 0; i < size; i++) {
            result.add(function.applyAsInt(items[i]));
        }
        return result;
    }

    @Override
    public IntList toImmutable() {
        return new ImmutableIntList(toArray());
    }

    public void trimToSize() {
        if (size < items.length) {
            items = Arrays.copyOf(items, size);
        }
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity > items.length) {
            int newCapacity = Math.max(items.length + (items.length >> 1), minCapacity);
            if (newCapacity < DEFAULT_CAPACITY) {
                newCapacity = DEFAULT_CAPACITY;
            }
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
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(items[i]);
        }
        return sb.append(']').toString();
    }
}
