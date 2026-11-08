package io.jettra.collections.set.primitive;

import io.jettra.collections.api.LongIterator;
import io.jettra.collections.api.LongSet;
import io.jettra.collections.util.HashUtils;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.function.LongConsumer;
import java.util.function.LongPredicate;
import java.util.stream.LongStream;

/**
 * Open-addressing primitive long hash set.
 * Flat long[] table, zero boxing, backward-shift deletion.
 */
public class LongHashSet implements LongSet {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.7f;
    private static final long EMPTY = 0L;

    private long[] table;
    private int mask;
    private int size;
    private boolean hasZero;
    private int threshold;
    private final float loadFactor;

    public LongHashSet() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public LongHashSet(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public LongHashSet(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor) || loadFactor >= 1.0f) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        int capacity = HashUtils.nextPowerOfTwo(Math.max(4, initialCapacity));
        this.loadFactor = loadFactor;
        this.table = new long[capacity];
        this.mask = capacity - 1;
        this.threshold = (int) (capacity * loadFactor);
        this.size = 0;
        this.hasZero = false;
    }

    public static LongHashSet of(long... values) {
        LongHashSet set = new LongHashSet(values.length);
        for (long v : values) set.add(v);
        return set;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(long value) {
        if (value == EMPTY) return hasZero;
        long[] tab = table;
        int m = mask;
        int idx = HashUtils.hash(value) & m;
        long curr = tab[idx];
        while (curr != EMPTY) {
            if (curr == value) return true;
            idx = (idx + 1) & m;
            curr = tab[idx];
        }
        return false;
    }

    @Override
    public boolean add(long value) {
        if (value == EMPTY) {
            if (!hasZero) {
                hasZero = true;
                size++;
                if (size > threshold) rehash(table.length << 1);
                return true;
            }
            return false;
        }

        long[] tab = table;
        int m = mask;
        int idx = HashUtils.hash(value) & m;
        long curr = tab[idx];

        while (curr != EMPTY) {
            if (curr == value) return false;
            idx = (idx + 1) & m;
            curr = tab[idx];
        }

        tab[idx] = value;
        size++;
        if (size > threshold) rehash(table.length << 1);
        return true;
    }

    @Override
    public boolean remove(long value) {
        if (value == EMPTY) {
            if (hasZero) {
                hasZero = false;
                size--;
                return true;
            }
            return false;
        }

        long[] tab = table;
        int m = mask;
        int idx = HashUtils.hash(value) & m;
        long curr = tab[idx];

        while (curr != EMPTY) {
            if (curr == value) {
                shiftKeys(idx);
                size--;
                return true;
            }
            idx = (idx + 1) & m;
            curr = tab[idx];
        }
        return false;
    }

    private void shiftKeys(int pos) {
        long[] tab = table;
        int m = mask;
        int last = pos;
        long current;

        while (true) {
            pos = (pos + 1) & m;
            current = tab[pos];
            if (current == EMPTY) {
                tab[last] = EMPTY;
                return;
            }
            int slot = HashUtils.hash(current) & m;
            if (last <= pos ? (last >= slot || slot > pos) : (last >= slot && slot > pos)) {
                tab[last] = current;
                last = pos;
            }
        }
    }

    private void rehash(int newCapacity) {
        long[] oldTable = table;
        long[] newTable = new long[newCapacity];
        int newMask = newCapacity - 1;

        for (long v : oldTable) {
            if (v != EMPTY) {
                int idx = HashUtils.hash(v) & newMask;
                while (newTable[idx] != EMPTY) {
                    idx = (idx + 1) & newMask;
                }
                newTable[idx] = v;
            }
        }

        this.table = newTable;
        this.mask = newMask;
        this.threshold = (int) (newCapacity * loadFactor);
    }

    @Override
    public void clear() {
        Arrays.fill(table, EMPTY);
        hasZero = false;
        size = 0;
    }

    @Override
    public long[] toArray() {
        long[] result = new long[size];
        int idx = 0;
        if (hasZero) result[idx++] = 0L;
        for (long v : table) {
            if (v != EMPTY) result[idx++] = v;
        }
        return result;
    }

    @Override
    public LongIterator longIterator() {
        return new LongIterator() {
            private int visited = 0;
            private int tableIndex = 0;
            private boolean zeroReturned = !hasZero;

            @Override
            public boolean hasNext() {
                return visited < size;
            }

            @Override
            public long nextLong() {
                if (!hasNext()) throw new NoSuchElementException();
                if (!zeroReturned) {
                    zeroReturned = true;
                    visited++;
                    return 0L;
                }
                while (tableIndex < table.length) {
                    long val = table[tableIndex++];
                    if (val != EMPTY) {
                        visited++;
                        return val;
                    }
                }
                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public void forEach(LongConsumer action) {
        if (hasZero) action.accept(0L);
        for (long v : table) {
            if (v != EMPTY) action.accept(v);
        }
    }

    @Override
    public long sum() {
        long total = 0;
        for (long v : table) {
            if (v != EMPTY) total += v;
        }
        return total;
    }

    @Override
    public OptionalLong min() {
        if (size == 0) return OptionalLong.empty();
        long min = hasZero ? 0L : Long.MAX_VALUE;
        for (long v : table) {
            if (v != EMPTY && v < min) min = v;
        }
        return OptionalLong.of(min);
    }

    @Override
    public OptionalLong max() {
        if (size == 0) return OptionalLong.empty();
        long max = hasZero ? 0L : Long.MIN_VALUE;
        for (long v : table) {
            if (v != EMPTY && v > max) max = v;
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
        if (hasZero && predicate.test(0L)) return true;
        for (long v : table) {
            if (v != EMPTY && predicate.test(v)) return true;
        }
        return false;
    }

    @Override
    public boolean allSatisfy(LongPredicate predicate) {
        if (hasZero && !predicate.test(0L)) return false;
        for (long v : table) {
            if (v != EMPTY && !predicate.test(v)) return false;
        }
        return true;
    }

    @Override
    public boolean noneSatisfy(LongPredicate predicate) {
        return !anySatisfy(predicate);
    }

    @Override
    public int count(LongPredicate predicate) {
        int count = (hasZero && predicate.test(0L)) ? 1 : 0;
        for (long v : table) {
            if (v != EMPTY && predicate.test(v)) count++;
        }
        return count;
    }

    @Override
    public LongStream toStream() {
        return Arrays.stream(toArray());
    }

    @Override
    public LongSet select(LongPredicate predicate) {
        LongHashSet result = new LongHashSet();
        if (hasZero && predicate.test(0L)) result.add(0L);
        for (long v : table) {
            if (v != EMPTY && predicate.test(v)) result.add(v);
        }
        return result;
    }

    @Override
    public LongSet reject(LongPredicate predicate) {
        LongHashSet result = new LongHashSet();
        if (hasZero && !predicate.test(0L)) result.add(0L);
        for (long v : table) {
            if (v != EMPTY && !predicate.test(v)) result.add(v);
        }
        return result;
    }

    @Override
    public LongSet toImmutable() {
        long[] copy = toArray();
        Arrays.sort(copy);
        return new LongSet() {
            @Override public int size() { return copy.length; }
            @Override public boolean contains(long value) { return Arrays.binarySearch(copy, value) >= 0; }
            @Override public boolean add(long value) { throw new UnsupportedOperationException(); }
            @Override public boolean remove(long value) { throw new UnsupportedOperationException(); }
            @Override public void clear() { throw new UnsupportedOperationException(); }
            @Override public long[] toArray() { return copy.clone(); }
            @Override public LongIterator longIterator() {
                return new LongIterator() {
                    private int c = 0;
                    @Override public boolean hasNext() { return c < copy.length; }
                    @Override public long nextLong() { return copy[c++]; }
                };
            }
            @Override public void forEach(LongConsumer action) {
                for (long v : copy) action.accept(v);
            }
            @Override public long sum() {
                long s = 0;
                for (long v : copy) s += v;
                return s;
            }
            @Override public OptionalLong min() { return copy.length == 0 ? OptionalLong.empty() : OptionalLong.of(copy[0]); }
            @Override public OptionalLong max() { return copy.length == 0 ? OptionalLong.empty() : OptionalLong.of(copy[copy.length - 1]); }
            @Override public OptionalDouble average() { return copy.length == 0 ? OptionalDouble.empty() : OptionalDouble.of((double) sum() / copy.length); }
            @Override public boolean anySatisfy(LongPredicate predicate) {
                for (long v : copy) if (predicate.test(v)) return true;
                return false;
            }
            @Override public boolean allSatisfy(LongPredicate predicate) {
                for (long v : copy) if (!predicate.test(v)) return false;
                return true;
            }
            @Override public boolean noneSatisfy(LongPredicate predicate) { return !anySatisfy(predicate); }
            @Override public int count(LongPredicate predicate) {
                int cnt = 0;
                for (long v : copy) if (predicate.test(v)) cnt++;
                return cnt;
            }
            @Override public LongStream toStream() { return Arrays.stream(copy); }
            @Override public LongSet select(LongPredicate predicate) {
                LongHashSet res = new LongHashSet();
                for (long v : copy) if (predicate.test(v)) res.add(v);
                return res.toImmutable();
            }
            @Override public LongSet reject(LongPredicate predicate) {
                LongHashSet res = new LongHashSet();
                for (long v : copy) if (!predicate.test(v)) res.add(v);
                return res.toImmutable();
            }
            @Override public LongSet toImmutable() { return this; }
            @Override public String toString() { return Arrays.toString(copy); }
        };
    }

    @Override
    public String toString() {
        return Arrays.toString(toArray());
    }
}
