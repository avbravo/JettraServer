package io.jettra.collections.set.primitive;

import io.jettra.collections.api.IntIterator;
import io.jettra.collections.api.IntSet;
import io.jettra.collections.util.HashUtils;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;

/**
 * Open-addressing primitive int hash set.
 * Features:
 * - Single flat int[] table with backward-shift deletion (zero tombstones).
 * - MurmurHash3 mixing for optimal distribution and cache line locality.
 * - Zero wrapper objects (Integer) and zero node entries (Map$Node).
 * - Massive memory savings (approx 85% less memory than java.util.HashSet).
 */
public class IntHashSet implements IntSet {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.7f;
    private static final int EMPTY = 0;

    private int[] table;
    private int mask;
    private int size;
    private boolean hasZero;
    private int threshold;
    private final float loadFactor;

    public IntHashSet() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public IntHashSet(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public IntHashSet(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
        }
        if (loadFactor <= 0 || Float.isNaN(loadFactor) || loadFactor >= 1.0f) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        int capacity = HashUtils.nextPowerOfTwo(Math.max(4, initialCapacity));
        this.loadFactor = loadFactor;
        this.table = new int[capacity];
        this.mask = capacity - 1;
        this.threshold = (int) (capacity * loadFactor);
        this.size = 0;
        this.hasZero = false;
    }

    public static IntHashSet of(int... values) {
        IntHashSet set = new IntHashSet(values.length);
        for (int v : values) {
            set.add(v);
        }
        return set;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(int value) {
        if (value == EMPTY) {
            return hasZero;
        }
        int[] tab = table;
        int m = mask;
        int idx = HashUtils.hash(value) & m;
        int curr = tab[idx];
        while (curr != EMPTY) {
            if (curr == value) {
                return true;
            }
            idx = (idx + 1) & m;
            curr = tab[idx];
        }
        return false;
    }

    @Override
    public boolean add(int value) {
        if (value == EMPTY) {
            if (!hasZero) {
                hasZero = true;
                size++;
                if (size > threshold) {
                    rehash(table.length << 1);
                }
                return true;
            }
            return false;
        }

        int[] tab = table;
        int m = mask;
        int idx = HashUtils.hash(value) & m;
        int curr = tab[idx];

        while (curr != EMPTY) {
            if (curr == value) {
                return false;
            }
            idx = (idx + 1) & m;
            curr = tab[idx];
        }

        tab[idx] = value;
        size++;
        if (size > threshold) {
            rehash(table.length << 1);
        }
        return true;
    }

    @Override
    public boolean remove(int value) {
        if (value == EMPTY) {
            if (hasZero) {
                hasZero = false;
                size--;
                return true;
            }
            return false;
        }

        int[] tab = table;
        int m = mask;
        int idx = HashUtils.hash(value) & m;
        int curr = tab[idx];

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

    /**
     * Backward-shift deletion for open addressing linear probing.
     * Guarantees table integrity without tombstones.
     */
    private void shiftKeys(int pos) {
        int[] tab = table;
        int m = mask;
        int last = pos;
        int current;

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
        int[] oldTable = table;
        int[] newTable = new int[newCapacity];
        int newMask = newCapacity - 1;

        for (int v : oldTable) {
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
    public int[] toArray() {
        int[] result = new int[size];
        int idx = 0;
        if (hasZero) {
            result[idx++] = 0;
        }
        for (int v : table) {
            if (v != EMPTY) {
                result[idx++] = v;
            }
        }
        return result;
    }

    @Override
    public IntIterator intIterator() {
        return new IntIterator() {
            private int visited = 0;
            private int tableIndex = 0;
            private boolean zeroReturned = !hasZero;

            @Override
            public boolean hasNext() {
                return visited < size;
            }

            @Override
            public int nextInt() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                if (!zeroReturned) {
                    zeroReturned = true;
                    visited++;
                    return 0;
                }
                while (tableIndex < table.length) {
                    int val = table[tableIndex++];
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
    public void forEach(IntConsumer action) {
        if (hasZero) {
            action.accept(0);
        }
        for (int v : table) {
            if (v != EMPTY) {
                action.accept(v);
            }
        }
    }

    @Override
    public long sum() {
        long total = 0;
        for (int v : table) {
            if (v != EMPTY) {
                total += v;
            }
        }
        return total;
    }

    @Override
    public OptionalInt min() {
        if (size == 0) return OptionalInt.empty();
        int min = hasZero ? 0 : Integer.MAX_VALUE;
        for (int v : table) {
            if (v != EMPTY && v < min) {
                min = v;
            }
        }
        return OptionalInt.of(min);
    }

    @Override
    public OptionalInt max() {
        if (size == 0) return OptionalInt.empty();
        int max = hasZero ? 0 : Integer.MIN_VALUE;
        for (int v : table) {
            if (v != EMPTY && v > max) {
                max = v;
            }
        }
        return OptionalInt.of(max);
    }

    @Override
    public OptionalDouble average() {
        if (size == 0) return OptionalDouble.empty();
        return OptionalDouble.of((double) sum() / size);
    }

    @Override
    public boolean anySatisfy(IntPredicate predicate) {
        if (hasZero && predicate.test(0)) return true;
        for (int v : table) {
            if (v != EMPTY && predicate.test(v)) return true;
        }
        return false;
    }

    @Override
    public boolean allSatisfy(IntPredicate predicate) {
        if (hasZero && !predicate.test(0)) return false;
        for (int v : table) {
            if (v != EMPTY && !predicate.test(v)) return false;
        }
        return true;
    }

    @Override
    public boolean noneSatisfy(IntPredicate predicate) {
        return !anySatisfy(predicate);
    }

    @Override
    public int count(IntPredicate predicate) {
        int count = (hasZero && predicate.test(0)) ? 1 : 0;
        for (int v : table) {
            if (v != EMPTY && predicate.test(v)) count++;
        }
        return count;
    }

    @Override
    public IntStream toStream() {
        return Arrays.stream(toArray());
    }

    @Override
    public IntSet select(IntPredicate predicate) {
        IntHashSet result = new IntHashSet();
        if (hasZero && predicate.test(0)) result.add(0);
        for (int v : table) {
            if (v != EMPTY && predicate.test(v)) result.add(v);
        }
        return result;
    }

    @Override
    public IntSet reject(IntPredicate predicate) {
        IntHashSet result = new IntHashSet();
        if (hasZero && !predicate.test(0)) result.add(0);
        for (int v : table) {
            if (v != EMPTY && !predicate.test(v)) result.add(v);
        }
        return result;
    }

    @Override
    public IntSet toImmutable() {
        return new ImmutableIntHashSet(toArray());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        if (hasZero) {
            sb.append(0);
            first = false;
        }
        for (int v : table) {
            if (v != EMPTY) {
                if (!first) sb.append(", ");
                sb.append(v);
                first = false;
            }
        }
        return sb.append('}').toString();
    }
}
