package io.jettra.collections.set;

import io.jettra.collections.util.HashUtils;

import java.util.*;

/**
 * Memory-efficient, open-addressing Hash Set for Objects.
 * Unlike java.util.HashSet (which delegates to HashMap and allocates a 32-byte Node object per element),
 * UnifiedSet stores elements DIRECTLY in a contiguous flat Object[] table.
 *
 * Memory comparison:
 * - java.util.HashSet: ~36-40 bytes container overhead per element.
 * - UnifiedSet: ~5.3 bytes container overhead per element (85% reduction!).
 */
public class UnifiedSet<E> extends AbstractSet<E> implements Set<E> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final Object NULL_KEY = new Object();

    private Object[] table;
    private int mask;
    private int size;
    private int threshold;
    private final float loadFactor;

    public UnifiedSet() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public UnifiedSet(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public UnifiedSet(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor) || loadFactor >= 1.0f) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        int capacity = HashUtils.nextPowerOfTwo(Math.max(4, initialCapacity));
        this.loadFactor = loadFactor;
        this.table = new Object[capacity];
        this.mask = capacity - 1;
        this.threshold = (int) (capacity * loadFactor);
        this.size = 0;
    }

    @SafeVarargs
    public static <E> UnifiedSet<E> of(E... elements) {
        UnifiedSet<E> set = new UnifiedSet<>(elements.length);
        for (E e : elements) {
            set.add(e);
        }
        return set;
    }

    public static <E> UnifiedSet<E> newSet(Collection<? extends E> collection) {
        UnifiedSet<E> set = new UnifiedSet<>(collection.size());
        set.addAll(collection);
        return set;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean contains(Object o) {
        Object key = maskNull(o);
        Object[] tab = table;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        Object curr = tab[idx];

        while (curr != null) {
            if (curr.equals(key)) {
                return true;
            }
            idx = (idx + 1) & m;
            curr = tab[idx];
        }
        return false;
    }

    @Override
    public boolean add(E e) {
        Object key = maskNull(e);
        Object[] tab = table;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        Object curr = tab[idx];

        while (curr != null) {
            if (curr.equals(key)) {
                return false;
            }
            idx = (idx + 1) & m;
            curr = tab[idx];
        }

        tab[idx] = key;
        size++;
        if (size > threshold) {
            rehash(table.length << 1);
        }
        return true;
    }

    @Override
    public boolean remove(Object o) {
        Object key = maskNull(o);
        Object[] tab = table;
        int m = mask;
        int idx = HashUtils.hash(key) & m;
        Object curr = tab[idx];

        while (curr != null) {
            if (curr.equals(key)) {
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
        Object[] tab = table;
        int m = mask;
        int last = pos;
        Object current;

        while (true) {
            pos = (pos + 1) & m;
            current = tab[pos];
            if (current == null) {
                tab[last] = null;
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
        Object[] oldTable = table;
        Object[] newTable = new Object[newCapacity];
        int newMask = newCapacity - 1;

        for (Object key : oldTable) {
            if (key != null) {
                int idx = HashUtils.hash(key) & newMask;
                while (newTable[idx] != null) {
                    idx = (idx + 1) & newMask;
                }
                newTable[idx] = key;
            }
        }

        this.table = newTable;
        this.mask = newMask;
        this.threshold = (int) (newCapacity * loadFactor);
    }

    @Override
    public void clear() {
        Arrays.fill(table, null);
        size = 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private int visited = 0;
            private int tableIndex = 0;
            private int lastReturnedIndex = -1;

            @Override
            public boolean hasNext() {
                return visited < size;
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                while (tableIndex < table.length) {
                    Object item = table[tableIndex++];
                    if (item != null) {
                        visited++;
                        lastReturnedIndex = tableIndex - 1;
                        return (E) unmaskNull(item);
                    }
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                if (lastReturnedIndex < 0) {
                    throw new IllegalStateException();
                }
                // Note: full remove in iterator with linear probing requires shifting
                UnifiedSet.this.remove(unmaskNull(table[lastReturnedIndex]));
                lastReturnedIndex = -1;
            }
        };
    }

    private static Object maskNull(Object o) {
        return o == null ? NULL_KEY : o;
    }

    private static Object unmaskNull(Object o) {
        return o == NULL_KEY ? null : o;
    }
}
