package io.jettra.collections.map;

import io.jettra.collections.util.HashUtils;

import java.util.*;

/**
 * High-performance, open-addressing Hash Map for Objects.
 * Keys and values alternate directly within a single contiguous flat Object[] table:
 *   table[2 * slot]     = Key
 *   table[2 * slot + 1] = Value
 *
 * Eliminates 100% of java.util.HashMap$Node objects, slashing RAM consumption by up to 75%.
 */
public class UnifiedMap<K, V> extends AbstractMap<K, V> implements Map<K, V> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final Object NULL_KEY = new Object();

    private Object[] table;
    private int mask; // Capacity - 1 (Capacity is slots, table.length is 2 * Capacity)
    private int size;
    private int threshold;
    private final float loadFactor;

    public UnifiedMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
    }

    public UnifiedMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    public UnifiedMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) throw new IllegalArgumentException("Illegal capacity: " + initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor) || loadFactor >= 1.0f) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        int capacity = HashUtils.nextPowerOfTwo(Math.max(4, initialCapacity));
        this.loadFactor = loadFactor;
        this.table = new Object[capacity << 1];
        this.mask = capacity - 1;
        this.threshold = (int) (capacity * loadFactor);
        this.size = 0;
    }

    public static <K, V> UnifiedMap<K, V> newMap() {
        return new UnifiedMap<>();
    }

    public static <K, V> UnifiedMap<K, V> newMap(int initialCapacity) {
        return new UnifiedMap<>(initialCapacity);
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
    public boolean containsKey(Object key) {
        Object k = maskNull(key);
        Object[] tab = table;
        int m = mask;
        int slot = HashUtils.hash(k) & m;
        Object curr = tab[slot << 1];

        while (curr != null) {
            if (curr.equals(k)) {
                return true;
            }
            slot = (slot + 1) & m;
            curr = tab[slot << 1];
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        Object[] tab = table;
        for (int i = 0; i < tab.length; i += 2) {
            if (tab[i] != null && Objects.equals(tab[i + 1], value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V get(Object key) {
        Object k = maskNull(key);
        Object[] tab = table;
        int m = mask;
        int slot = HashUtils.hash(k) & m;
        Object curr = tab[slot << 1];

        while (curr != null) {
            if (curr.equals(k)) {
                return (V) tab[(slot << 1) + 1];
            }
            slot = (slot + 1) & m;
            curr = tab[slot << 1];
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V put(K key, V value) {
        Object k = maskNull(key);
        Object[] tab = table;
        int m = mask;
        int slot = HashUtils.hash(k) & m;
        Object curr = tab[slot << 1];

        while (curr != null) {
            if (curr.equals(k)) {
                V oldVal = (V) tab[(slot << 1) + 1];
                tab[(slot << 1) + 1] = value;
                return oldVal;
            }
            slot = (slot + 1) & m;
            curr = tab[slot << 1];
        }

        tab[slot << 1] = k;
        tab[(slot << 1) + 1] = value;
        size++;
        if (size > threshold) {
            rehash((mask + 1) << 1);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        Object k = maskNull(key);
        Object[] tab = table;
        int m = mask;
        int slot = HashUtils.hash(k) & m;
        Object curr = tab[slot << 1];

        while (curr != null) {
            if (curr.equals(k)) {
                V oldVal = (V) tab[(slot << 1) + 1];
                shiftSlots(slot);
                size--;
                return oldVal;
            }
            slot = (slot + 1) & m;
            curr = tab[slot << 1];
        }
        return null;
    }

    private void shiftSlots(int pos) {
        Object[] tab = table;
        int m = mask;
        int last = pos;
        Object current;

        while (true) {
            pos = (pos + 1) & m;
            current = tab[pos << 1];
            if (current == null) {
                tab[last << 1] = null;
                tab[(last << 1) + 1] = null;
                return;
            }
            int slot = HashUtils.hash(current) & m;
            if (last <= pos ? (last >= slot || slot > pos) : (last >= slot && slot > pos)) {
                tab[last << 1] = current;
                tab[(last << 1) + 1] = tab[(pos << 1) + 1];
                last = pos;
            }
        }
    }

    private void rehash(int newCapacity) {
        Object[] oldTable = table;
        Object[] newTable = new Object[newCapacity << 1];
        int newMask = newCapacity - 1;

        for (int i = 0; i < oldTable.length; i += 2) {
            Object k = oldTable[i];
            if (k != null) {
                int slot = HashUtils.hash(k) & newMask;
                while (newTable[slot << 1] != null) {
                    slot = (slot + 1) & newMask;
                }
                newTable[slot << 1] = k;
                newTable[(slot << 1) + 1] = oldTable[i + 1];
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
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<>() {
                    private int visited = 0;
                    private int slot = 0;

                    @Override
                    public boolean hasNext() {
                        return visited < size;
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public Entry<K, V> next() {
                        if (!hasNext()) throw new NoSuchElementException();
                        while (slot <= mask) {
                            int idx = slot << 1;
                            slot++;
                            if (table[idx] != null) {
                                visited++;
                                K k = (K) unmaskNull(table[idx]);
                                V v = (V) table[idx + 1];
                                return new AbstractMap.SimpleEntry<>(k, v);
                            }
                        }
                        throw new NoSuchElementException();
                    }
                };
            }

            @Override
            public int size() {
                return size;
            }
        };
    }
    @Override
    public Collection<V> values() {
        return new AbstractCollection<>() {
            @Override
            public Iterator<V> iterator() {
                return new Iterator<>() {
                    private int visited = 0;
                    private int slot = 0;

                    @Override
                    public boolean hasNext() {
                        return visited < size;
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public V next() {
                        if (!hasNext()) throw new NoSuchElementException();
                        while (slot <= mask) {
                            int idx = slot << 1;
                            slot++;
                            if (table[idx] != null) {
                                visited++;
                                return (V) table[idx + 1];
                            }
                        }
                        throw new NoSuchElementException();
                    }
                };
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    @Override
    public Set<K> keySet() {
        return new AbstractSet<>() {
            @Override
            public Iterator<K> iterator() {
                return new Iterator<>() {
                    private int visited = 0;
                    private int slot = 0;

                    @Override
                    public boolean hasNext() {
                        return visited < size;
                    }

                    @Override
                    @SuppressWarnings("unchecked")
                    public K next() {
                        if (!hasNext()) throw new NoSuchElementException();
                        while (slot <= mask) {
                            int idx = slot << 1;
                            slot++;
                            if (table[idx] != null) {
                                visited++;
                                return (K) unmaskNull(table[idx]);
                            }
                        }
                        throw new NoSuchElementException();
                    }
                };
            }

            @Override
            public int size() {
                return size;
            }
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void forEach(java.util.function.BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int visited = 0;
        for (int i = 0; i < table.length && visited < size; i += 2) {
            if (table[i] != null) {
                visited++;
                action.accept((K) unmaskNull(table[i]), (V) table[i + 1]);
            }
        }
    }

    private static Object maskNull(Object o) {
        return o == null ? NULL_KEY : o;
    }

    private static Object unmaskNull(Object o) {
        return o == NULL_KEY ? null : o;
    }
}
