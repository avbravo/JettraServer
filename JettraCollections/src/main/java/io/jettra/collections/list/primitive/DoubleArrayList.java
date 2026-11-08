package io.jettra.collections.list.primitive;

import io.jettra.collections.api.DoubleIterator;
import io.jettra.collections.api.DoubleList;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.function.DoubleConsumer;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

/**
 * Resizable primitive double array list with zero wrapper overhead.
 */
public class DoubleArrayList implements DoubleList {

    private static final int DEFAULT_CAPACITY = 10;
    private static final double[] EMPTY_ELEMENTS = new double[0];

    protected double[] items;
    protected int size;

    public DoubleArrayList() {
        this(DEFAULT_CAPACITY);
    }

    public DoubleArrayList(int initialCapacity) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal Capacity: " + initialCapacity);
        }
        this.items = initialCapacity == 0 ? EMPTY_ELEMENTS : new double[initialCapacity];
        this.size = 0;
    }

    public static DoubleArrayList of(double... values) {
        DoubleArrayList list = new DoubleArrayList(values.length);
        list.addAll(values);
        return list;
    }

    public void addAll(double... values) {
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
    public boolean contains(double value) {
        return indexOf(value) >= 0;
    }

    @Override
    public boolean add(double value) {
        ensureCapacity(size + 1);
        items[size++] = value;
        return true;
    }

    @Override
    public void addAtIndex(int index, double element) {
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
    public double get(int index) {
        checkIndex(index);
        return items[index];
    }

    @Override
    public double set(int index, double element) {
        checkIndex(index);
        double oldValue = items[index];
        items[index] = element;
        return oldValue;
    }

    @Override
    public double removeAtIndex(int index) {
        checkIndex(index);
        double oldValue = items[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(items, index + 1, items, index, numMoved);
        }
        size--;
        return oldValue;
    }

    @Override
    public int indexOf(double value) {
        for (int i = 0; i < size; i++) {
            if (Double.doubleToLongBits(items[i]) == Double.doubleToLongBits(value)) return i;
        }
        return -1;
    }

    @Override
    public void clear() {
        size = 0;
    }

    @Override
    public double[] toArray() {
        return Arrays.copyOf(items, size);
    }

    @Override
    public DoubleIterator doubleIterator() {
        return new DoubleIterator() {
            private int cursor = 0;

            @Override
            public boolean hasNext() {
                return cursor < size;
            }

            @Override
            public double nextDouble() {
                if (cursor >= size) throw new NoSuchElementException();
                return items[cursor++];
            }
        };
    }

    @Override
    public void forEach(DoubleConsumer action) {
        for (int i = 0; i < size; i++) {
            action.accept(items[i]);
        }
    }

    @Override
    public double sum() {
        double total = 0;
        for (int i = 0; i < size; i++) total += items[i];
        return total;
    }

    @Override
    public OptionalDouble min() {
        if (size == 0) return OptionalDouble.empty();
        double min = items[0];
        for (int i = 1; i < size; i++) {
            if (items[i] < min) min = items[i];
        }
        return OptionalDouble.of(min);
    }

    @Override
    public OptionalDouble max() {
        if (size == 0) return OptionalDouble.empty();
        double max = items[0];
        for (int i = 1; i < size; i++) {
            if (items[i] > max) max = items[i];
        }
        return OptionalDouble.of(max);
    }

    @Override
    public OptionalDouble average() {
        if (size == 0) return OptionalDouble.empty();
        return OptionalDouble.of(sum() / size);
    }

    @Override
    public DoubleStream toStream() {
        return Arrays.stream(items, 0, size);
    }

    @Override
    public DoubleList sortThis() {
        Arrays.sort(items, 0, size);
        return this;
    }

    @Override
    public DoubleList reverseThis() {
        int half = size / 2;
        for (int i = 0; i < half; i++) {
            double temp = items[i];
            items[i] = items[size - 1 - i];
            items[size - 1 - i] = temp;
        }
        return this;
    }

    @Override
    public DoubleList select(DoublePredicate predicate) {
        DoubleArrayList result = new DoubleArrayList();
        for (int i = 0; i < size; i++) {
            if (predicate.test(items[i])) result.add(items[i]);
        }
        return result;
    }

    @Override
    public DoubleList reject(DoublePredicate predicate) {
        DoubleArrayList result = new DoubleArrayList();
        for (int i = 0; i < size; i++) {
            if (!predicate.test(items[i])) result.add(items[i]);
        }
        return result;
    }

    @Override
    public DoubleList collect(DoubleUnaryOperator operator) {
        DoubleArrayList result = new DoubleArrayList(size);
        for (int i = 0; i < size; i++) {
            result.add(operator.applyAsDouble(items[i]));
        }
        return result;
    }

    @Override
    public DoubleList toImmutable() {
        double[] copy = toArray();
        return new DoubleList() {
            @Override public int size() { return copy.length; }
            @Override public double get(int index) { return copy[index]; }
            @Override public double set(int index, double element) { throw new UnsupportedOperationException(); }
            @Override public boolean add(double element) { throw new UnsupportedOperationException(); }
            @Override public void addAtIndex(int index, double element) { throw new UnsupportedOperationException(); }
            @Override public double removeAtIndex(int index) { throw new UnsupportedOperationException(); }
            @Override public boolean contains(double value) {
                for (double v : copy) if (Double.doubleToLongBits(v) == Double.doubleToLongBits(value)) return true;
                return false;
            }
            @Override public int indexOf(double value) {
                for (int i = 0; i < copy.length; i++) if (Double.doubleToLongBits(copy[i]) == Double.doubleToLongBits(value)) return i;
                return -1;
            }
            @Override public void clear() { throw new UnsupportedOperationException(); }
            @Override public double[] toArray() { return copy.clone(); }
            @Override public DoubleIterator doubleIterator() {
                return new DoubleIterator() {
                    private int c = 0;
                    @Override public boolean hasNext() { return c < copy.length; }
                    @Override public double nextDouble() { return copy[c++]; }
                };
            }
            @Override public void forEach(DoubleConsumer action) {
                for (double v : copy) action.accept(v);
            }
            @Override public double sum() {
                double t = 0;
                for (double v : copy) t += v;
                return t;
            }
            @Override public OptionalDouble min() {
                if (copy.length == 0) return OptionalDouble.empty();
                double m = copy[0];
                for (double v : copy) if (v < m) m = v;
                return OptionalDouble.of(m);
            }
            @Override public OptionalDouble max() {
                if (copy.length == 0) return OptionalDouble.empty();
                double m = copy[0];
                for (double v : copy) if (v > m) m = v;
                return OptionalDouble.of(m);
            }
            @Override public OptionalDouble average() {
                return copy.length == 0 ? OptionalDouble.empty() : OptionalDouble.of(sum() / copy.length);
            }
            @Override public DoubleList sortThis() { throw new UnsupportedOperationException(); }
            @Override public DoubleList reverseThis() { throw new UnsupportedOperationException(); }
            @Override public DoubleList select(DoublePredicate predicate) {
                DoubleArrayList res = new DoubleArrayList();
                for (double v : copy) if (predicate.test(v)) res.add(v);
                return res.toImmutable();
            }
            @Override public DoubleList reject(DoublePredicate predicate) {
                DoubleArrayList res = new DoubleArrayList();
                for (double v : copy) if (!predicate.test(v)) res.add(v);
                return res.toImmutable();
            }
            @Override public DoubleList collect(DoubleUnaryOperator operator) {
                double[] res = new double[copy.length];
                for (int i = 0; i < copy.length; i++) res[i] = operator.applyAsDouble(copy[i]);
                return of(res).toImmutable();
            }
            @Override public DoubleList toImmutable() { return this; }
            @Override public DoubleStream toStream() { return Arrays.stream(copy); }
            @Override public String toString() { return Arrays.toString(copy); }
        };
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
